package com.ssairen.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 설정
 * AI 서버와의 HTTP 통신을 위한 WebClient Bean 생성
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final AiServerProperties aiServerProperties;

    /**
     * AI 서버 통신용 WebClient Bean 생성
     */
    @Bean
    public WebClient aiServerWebClient() {
        // HTTP 클라이언트 타임아웃 설정
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000) // 연결 타임아웃: 30초
                .responseTimeout(Duration.ofSeconds(aiServerProperties.getTimeout())) // 응답 타임아웃
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(aiServerProperties.getTimeout(), TimeUnit.SECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(aiServerProperties.getTimeout(), TimeUnit.SECONDS))
                );

        log.info("WebClient 초기화 완료 - AI 서버 URL: {}, 타임아웃: {}초",
                aiServerProperties.getBaseUrl(),
                aiServerProperties.getTimeout());

        return WebClient.builder()
                .baseUrl(aiServerProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
