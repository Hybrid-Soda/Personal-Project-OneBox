package com.devnovus.oneBox.domain.file.Repository;

import com.devnovus.oneBox.domain.file.dto.FileUploadDto;
import com.devnovus.oneBox.global.aop.time.ExecutionTime;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileRepository {
    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    @ExecutionTime
    public String putObject(FileUploadDto dto, String objectName) {
        try (InputStream inputStream = dto.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, dto.getFileSize(), -1)
                    .contentType(dto.getContentType())
                    .build();

            String eTag = minioClient.putObject(args).etag();
            log.info("FileRepository.putObject / objectName: {} / ETag: {}", objectName, eTag);
            return eTag;
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_NOT_SAVED);
        }
    }

    @ExecutionTime
    public InputStream getObject(String objectName) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            InputStream stream = minioClient.getObject(args);
            log.info("FileRepository.getObject / objectName: {}", objectName);
            return stream;
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_NOT_DOWNLOADED);
        }
    }

    @ExecutionTime
    public void removeObject(String objectName) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            minioClient.removeObject(args);
            log.info("FileRepository.removeObject / objectName: {}", objectName);
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_NOT_DELETED);
        }
    }

    @ExecutionTime
    public String getPreSignedObjectUrl(String objectName, String contentType) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectName)
                    .extraHeaders(Map.of("Content-Type", contentType))
                    .expiry(600)
                    .build();

            String uploadUrl = minioClient.getPresignedObjectUrl(args);
            log.info("FileRepository.getPreSignedObjectUrl / objectName: {}", objectName);
            return uploadUrl;
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FAIL_TO_GET_URL);
        }
    }

    @ExecutionTime
    public void statObject(String objectName) {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            StatObjectResponse stat = minioClient.statObject(args);
            log.info("FileRepository.statObject / objectName: {} / size: {}", objectName, stat.size());
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_NOT_SAVED);
        }
    }
}
