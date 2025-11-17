package com.ssairen.global.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 애플리케이션 특화 헬스 인디케이터
 * 애플리케이션 레벨의 상태를 체크합니다.
 */
@Component("application")
public class ApplicationHealthIndicator implements HealthIndicator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Health health() {
        try {
            return Health.up()
                .withDetail("application", "SSAIREN")
                .withDetail("status", "RUNNING")
                .withDetail("timestamp", LocalDateTime.now().format(DATE_FORMATTER))
                .withDetail("version", "0.0.1-SNAPSHOT")
                .withDetail("description", "Application is running normally")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "Health check exception")
                .withException(e)
                .build();
        }
    }
}