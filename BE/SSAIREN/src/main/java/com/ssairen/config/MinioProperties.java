package com.ssairen.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 설정 프로퍼티
 * application.yaml의 minio.* 설정값을 바인딩
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO 서버 엔드포인트 (예: http://minio:9000)
     */
    private String endpoint;

    /**
     * MinIO 액세스 키 (사용자 ID)
     */
    private String accessKey;

    /**
     * MinIO 시크릿 키 (비밀번호)
     */
    private String secretKey;

    /**
     * 기본 버킷 이름 (예: audio-files)
     */
    private String bucketName;
}
