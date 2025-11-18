package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.global.exception.ApplicationError;
import com.devnovus.oneBox.global.exception.ApplicationException;
import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MinioFileHandler {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public String upload(UploadFileDto dto, String ext) {
        String objectName = makeObjectName(dto.getUserId(), ext);

        try (InputStream inputStream = dto.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, dto.getFileSize(), -1)
                    .contentType(dto.getContentType())
                    .build();

            ObjectWriteResponse res = minioClient.putObject(args);
            res.etag();
            return objectName;
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_UPLOAD_FAIL);
        }
    }

    public String getPresignedUrl(UploadFileDto dto, String ext) {
        String objectName = makeObjectName(dto.getUserId(), ext);

        try {
            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .method(Method.PUT)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(60 * 10) // 초 단위
                    .build();

            return minioClient.getPresignedObjectUrl(args);
        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            throw new RuntimeException(e);
        }
    }

    private String makeObjectName(Long userId, String ext) {
        return String.format("%d/%s.%s", userId, UUID.randomUUID(), ext);
    }
}
