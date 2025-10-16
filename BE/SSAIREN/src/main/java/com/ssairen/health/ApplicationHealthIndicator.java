package com.ssairen.health;

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
            // 애플리케이션 상태 체크
            boolean isHealthy = checkApplicationHealth();

            if (isHealthy) {
                return Health.up()
                    .withDetail("application", "SSAIREN")
                    .withDetail("status", "RUNNING")
                    .withDetail("timestamp", LocalDateTime.now().format(DATE_FORMATTER))
                    .withDetail("version", "0.0.1-SNAPSHOT")
                    .withDetail("description", "Application is running normally")
                    .build();
            } else {
                return Health.down()
                    .withDetail("application", "SSAIREN")
                    .withDetail("status", "DOWN")
                    .withDetail("timestamp", LocalDateTime.now().format(DATE_FORMATTER))
                    .withDetail("description", "Application health check failed")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "Health check exception")
                .withException(e)
                .build();
        }
    }

    /**
     * 애플리케이션 헬스 체크 로직
     * 실제 프로젝트에서는 여기에 비즈니스 로직 체크를 추가할 수 있습니다.
     * 예: 데이터베이스 연결, 외부 API 연결, 캐시 상태 등
     */
    private boolean checkApplicationHealth() {
        // TODO: 실제 헬스 체크 로직 구현
        // 예시:
        // - 데이터베이스 연결 확인
        // - 외부 API 응답 확인
        // - 필수 빈(Bean) 로드 확인
        // - 캐시 서버 연결 확인

        return true; // 현재는 항상 정상
    }
}