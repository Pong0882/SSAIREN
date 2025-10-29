package com.ssairen.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 설정
 * STOMP 프로토콜을 사용한 WebSocket 통신 설정
 */
@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커 설정
     * - /topic: 병원에게 메시지를 전송하는 prefix
     * - /app: 클라이언트가 메시지를 보낼 때 사용하는 prefix
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple Broker 활성화 - /topic, /queue prefix로 시작하는 destination
        config.enableSimpleBroker("/topic", "/queue");

        // 클라이언트에서 메시지 전송 시 prefix
        config.setApplicationDestinationPrefixes("/app");

        log.info("WebSocket 메시지 브로커 설정 완료 - SimpleBroker: /topic, /queue | App prefix: /app");
    }

    /**
     * STOMP 엔드포인트 등록
     * 클라이언트가 WebSocket에 연결할 엔드포인트 설정
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 1. SockJS를 사용하는 엔드포인트 (웹 브라우저용)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // 2. 순수 WebSocket 엔드포인트 (Postman, 네이티브 클라이언트용)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        log.info("WebSocket STOMP 엔드포인트 등록 완료 - /ws (SockJS + Native WebSocket)");
    }
}
