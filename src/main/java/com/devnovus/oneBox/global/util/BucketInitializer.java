package com.devnovus.oneBox.global.util;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BucketInitializer {
    @Value("${minio.bucket}")
    private String bucketName;

    private final MinioClient minioClient;

    // 애플리케이션 시작 시 버킷 유무 체크 및 생성
    @PostConstruct
    public void initBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO 버킷 '{}' 생성 완료", bucketName);
            }
        } catch (Exception e) {
            throw new IllegalStateException("MinIO 버킷 초기화 실패: " + e.getMessage(), e);
        }
    }
}
