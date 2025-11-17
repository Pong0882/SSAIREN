package com.ssairen.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 서버 설정 프로퍼티
 * application.yaml의 ai.server.* 설정값을 바인딩
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai.server")
public class AiServerProperties {

    /**
     * AI 서버 베이스 URL (예: https://ai.ssairen.site)
     */
    private String baseUrl;

    /**
     * API 호출 타임아웃 (초)
     */
    private int timeout = 300;
}
