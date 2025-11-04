package com.ssairen.config;

import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
     * @throws CustomException MinIO 클라이언트 초기화 실패 시
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

            // 오디오 버킷 존재 여부 확인 및 생성
            String audioBucketName = minioProperties.getBucketName();
            ensureBucketExists(client, audioBucketName);

            // 영상 버킷 존재 여부 확인 및 생성
            String videoBucketName = minioProperties.getVideoBucketName();
            ensureBucketExists(client, videoBucketName);

            log.info("MinIO 클라이언트 초기화 완료");
            return client;

        } catch (CustomException e) {
            // ensureBucketExists에서 발생한 CustomException을 그대로 전파
            throw e;
        } catch (IllegalArgumentException e) {
            // MinioClient.builder()에서 발생할 수 있는 잘못된 설정값 에러
            log.error("MinIO 설정값 오류: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.MINIO_CLIENT_INITIALIZATION_FAILED, "MinIO 설정값이 올바르지 않습니다.", e);
        }
    }

    /**
     * 버킷 존재 여부 확인 및 생성
     *
     * @param client MinIO 클라이언트
     * @param bucketName 버킷 이름
     * @throws CustomException 버킷 생성 실패 시
     */
    private void ensureBucketExists(MinioClient client, String bucketName) {
        try {
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
        } catch (ErrorResponseException e) {
            log.error("MinIO 서버 에러 응답 - Bucket: {}, Code: {}", bucketName, e.errorResponse().code(), e);
            throw new CustomException(ErrorCode.MINIO_BUCKET_CREATION_FAILED, "MinIO 서버 에러: " + e.errorResponse().code(), e);
        } catch (InsufficientDataException | InternalException | InvalidResponseException | XmlParserException e) {
            log.error("MinIO 통신 에러 - Bucket: {}", bucketName, e);
            throw new CustomException(ErrorCode.MINIO_CONNECTION_FAILED, "MinIO 서버 연결 실패", e);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("MinIO 인증 에러 - Bucket: {}", bucketName, e);
            throw new CustomException(ErrorCode.MINIO_CLIENT_INITIALIZATION_FAILED, "MinIO 인증 실패", e);
        } catch (ServerException e) {
            log.error("MinIO 서버 에러 - Bucket: {}", bucketName, e);
            throw new CustomException(ErrorCode.MINIO_CONNECTION_FAILED, "MinIO 서버 에러", e);
        } catch (IOException e) {
            log.error("MinIO 네트워크 에러 - Bucket: {}", bucketName, e);
            throw new CustomException(ErrorCode.MINIO_CONNECTION_FAILED, "MinIO 네트워크 연결 실패", e);
        }
    }
}
