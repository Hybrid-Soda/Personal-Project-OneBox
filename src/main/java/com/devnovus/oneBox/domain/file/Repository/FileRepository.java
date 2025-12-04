package com.devnovus.oneBox.domain.file.Repository;

import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import com.devnovus.oneBox.global.aop.time.ExecutionTime;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.global.exception.StorageException;
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
    public String save(UploadFileDto dto, String objectName) {
        try (InputStream inputStream = dto.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, dto.getFileSize(), -1)
                    .contentType(dto.getContentType())
                    .build();

            String eTag = minioClient.putObject(args).etag();
            log.info("FileRepository.save / objectName: {} / ETag: {}", objectName, eTag);
            return eTag;
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_NOT_SAVED);
        }
    }

    @ExecutionTime
    public InputStream download(String objectName) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            InputStream stream = minioClient.getObject(args);
            log.info("FileRepository.download / objectName: {}", objectName);
            return stream;
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_NOT_DOWNLOADED);
        }
    }

    @ExecutionTime
    public void delete(String objectName) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            minioClient.removeObject(args);
            log.info("FileRepository.delete / objectName: {}", objectName);
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_NOT_DELETED);
        }
    }

    @ExecutionTime
    public String createPreSignedUploadUrl(String objectName, String contentType) {
        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectName)
                    .extraHeaders(Map.of("Content-Type", contentType))
                    .expiry(600)
                    .build();

            String uploadUrl = minioClient.getPresignedObjectUrl(args);
            log.info("FileRepository.createPreSignedUploadUrl / objectName: {}", objectName);
            return uploadUrl;
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }

    @ExecutionTime
    public void verifyObjectExists(String objectName, long expectedSize) {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            StatObjectResponse stat = minioClient.statObject(args);

            if (stat.size() != expectedSize) {
                throw new StorageException(new IllegalStateException("uploaded size mismatch"));
            }

            log.info("FileRepository.verifyObjectExists / objectName: {} / size: {}", objectName, stat.size());
        } catch (Exception e) {
            throw new StorageException(e);
        }
    }
}
