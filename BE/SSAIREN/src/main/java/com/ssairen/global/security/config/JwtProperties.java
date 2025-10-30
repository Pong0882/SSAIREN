package com.ssairen.global.security.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/**
 * JWT 설정 프로퍼티
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Validated
@Getter
@Setter
public class JwtProperties {

    /**
     * JWT 서명에 사용할 비밀키
     * - 최소 32자 이상 (256비트)
     */
    @NotBlank(message = "JWT secret must not be empty")
    @Size(min = 32, message = "JWT secret must be at least 32 characters for secure encryption")
    private String secret;

    /**
     * AccessToken 만료 시간 (밀리초)
     * - 양수여야 함
     */
    @Positive(message = "Access token expiration must be positive")
    private long accessTokenExpiration;

    /**
     * RefreshToken 만료 시간 (밀리초)
     * - 양수여야 함
     */
    @Positive(message = "Refresh token expiration must be positive")
    private long refreshTokenExpiration;
}
