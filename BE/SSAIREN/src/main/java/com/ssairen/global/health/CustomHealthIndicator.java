package com.ssairen.global.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * 커스텀 헬스 인디케이터
 * 애플리케이션의 메모리, 스레드, 시스템 리소스 상태를 체크합니다.
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {

    private static final double MEMORY_WARNING_THRESHOLD = 0.8; // 80%
    private static final double MEMORY_CRITICAL_THRESHOLD = 0.95; // 95%
    private static final String MESSAGE_KEY = "message";

    @Override
    public Health health() {
        try {
            // 메모리 사용률 체크
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();

            long maxMemory = heapMemoryUsage.getMax();
            long usedMemory = heapMemoryUsage.getUsed();
            double memoryUsageRatio = (double) usedMemory / maxMemory;

            // 스레드 수 체크
            int threadCount = Thread.activeCount();

            // 시스템 로드 체크
            double systemLoad = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();

            // 애플리케이션 업타임
            long uptime = ManagementFactory.getRuntimeMXBean().getUptime();

            Health.Builder builder;

            // 메모리 사용률에 따라 상태 결정
            if (memoryUsageRatio >= MEMORY_CRITICAL_THRESHOLD) {
                builder = Health.down()
                    .withDetail("status", "CRITICAL")
                        .withDetail(MESSAGE_KEY, "��������� ������������ ������ ���������������");
            } else if (memoryUsageRatio >= MEMORY_WARNING_THRESHOLD) {
                builder = Health.up()
                    .withDetail("status", "WARNING")
                        .withDetail(MESSAGE_KEY, "��������� ������������ ������������");
            } else {
                builder = Health.up()
                    .withDetail("status", "HEALTHY")
                        .withDetail(MESSAGE_KEY, "������ ������������ ���������������");
            }

            // 상세 정보 추가
            return builder
                .withDetail("memory", new MemoryInfo(
                    usedMemory / 1024 / 1024, // MB 단위
                    maxMemory / 1024 / 1024,   // MB 단위
                    String.format("%.2f%%", memoryUsageRatio * 100)
                ))
                .withDetail("threads", new ThreadInfo(
                    threadCount,
                    Runtime.getRuntime().availableProcessors()
                ))
                .withDetail("system", new SystemInfo(
                    String.format("%.2f", systemLoad),
                    formatUptime(uptime)
                ))
                .build();

        } catch (Exception e) {
            return Health.down()
                .withDetail("error", "헬스 체크 중 오류 발생")
                .withException(e)
                .build();
        }
    }

    private String formatUptime(long uptimeMillis) {
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return String.format("%dd %dh %dm", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes % 60, seconds % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }

    // 내부 클래스로 정보 구조화
    private record MemoryInfo(long usedMB, long maxMB, String usagePercent) {}
    private record ThreadInfo(int activeThreads, int availableProcessors) {}
    private record SystemInfo(String systemLoad, String uptime) {}
}