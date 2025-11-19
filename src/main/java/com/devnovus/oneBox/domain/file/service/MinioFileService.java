package com.devnovus.oneBox.domain.file.service;

import com.devnovus.oneBox.domain.file.dto.UploadFileDto;
import io.minio.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioFileService {
    private final HttpServletResponse response;
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
            throw new RuntimeException("파일 업로드에 실패했습니다: " + e);
        }
    }

    public InputStream download(String objectName) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            return (InputStream) minioClient.getObject(args);
        } catch (Exception e) {
            throw new RuntimeException("파일 다운로드에 실패했습니다: " + e);
        }
    }

    public void delete(String objectName) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            minioClient.removeObject(args);
        } catch (Exception e) {
            throw new RuntimeException("파일 삭제에 실패했습니다: " + e);
        }
    }
}
