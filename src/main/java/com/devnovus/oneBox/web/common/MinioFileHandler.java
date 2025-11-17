package com.devnovus.oneBox.web.common;

import com.devnovus.oneBox.aop.exception.ApplicationError;
import com.devnovus.oneBox.aop.exception.ApplicationException;
import com.devnovus.oneBox.web.file.dto.UploadFileDto;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MinioFileHandler {
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    public String upload(UploadFileDto dto, String ext) {
        String objectName = String.format("%d/%s.%s", dto.getUserId(), UUID.randomUUID(), ext);

        try (InputStream inputStream = dto.getInputStream()) {
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, dto.getFileSize(), -1)
                    .contentType(dto.getContentType())
                    .build();

            minioClient.putObject(args);
            return objectName;
        } catch (Exception e) {
            throw new ApplicationException(ApplicationError.FILE_UPLOAD_FAIL);
        }
    }
}
