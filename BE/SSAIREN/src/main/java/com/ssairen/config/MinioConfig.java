package com.ssairen.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 클라이언트 설정
 * MinIO 서버 연결을 위한 Bean 생성 및 초기화
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    /**
     * MinioClient Bean 생성
     * MinIO 서버와의 연결을 관리하는 클라이언트 객체를 생성합니다.
     *
     * @return MinioClient 인스턴스
     */
    @Bean
    public MinioClient minioClient() {
        try {
            log.info("MinIO 클라이언트 초기화 시작 - Endpoint: {}, Bucket: {}",
                    minioProperties.getEndpoint(),
                    minioProperties.getBucketName());

            // MinIO 클라이언트 생성
            MinioClient client = MinioClient.builder()
                    .endpoint(minioProperties.getEndpoint())
                    .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                    .build();

            // 버킷 존재 여부 확인 및 생성
            String bucketName = minioProperties.getBucketName();
            boolean bucketExists = client.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!bucketExists) {
                log.info("버킷 '{}' 이 존재하지 않아 새로 생성합니다.", bucketName);
                client.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("버킷 '{}' 생성 완료", bucketName);
            } else {
                log.info("버킷 '{}' 이 이미 존재합니다.", bucketName);
            }

            log.info("MinIO 클라이언트 초기화 완료");
            return client;

        } catch (Exception e) {
            log.error("MinIO 클라이언트 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("MinIO 클라이언트 초기화에 실패했습니다.", e);
        }
    }
}
