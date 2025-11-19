package com.devnovus.oneBox.domain.file.Repository;

import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileRepository {
    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    public String save(UploadFileDto dto, String objectName) {
        try (InputStream inputStream = dto.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, dto.getFileSize(), -1)
                    .contentType(dto.getContentType())
                    .build();

            String eTag = minioClient.putObject(args).etag();
            log.info("MinioFileService.upload / objectName: {} / ETag: {}", objectName, eTag);
            return eTag;
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_NOT_SAVED);
        }
    }

    public InputStream download(String objectName) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            InputStream stream = minioClient.getObject(args);
            log.info("MinioFileService.download / objectName: {}", objectName);
            return stream;
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_NOT_DOWNLOADED);
        }
    }

    public void delete(String objectName) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            minioClient.removeObject(args);
            log.info("MinioFileService.delete / objectName: {}", objectName);
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_NOT_DELETED);
        }
    }
}
