package com.ssairen.global.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 설정 프로퍼티
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

    /**
     * JWT 서명에 사용할 비밀키
     */
    private String secret;

    /**
     * AccessToken 만료 시간 (밀리초)
     */
    private long accessTokenExpiration;

    /**
     * RefreshToken 만료 시간 (밀리초)
     */
    private long refreshTokenExpiration;
}
