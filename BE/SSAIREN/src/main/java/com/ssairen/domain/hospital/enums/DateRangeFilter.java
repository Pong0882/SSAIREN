package com.ssairen.domain.hospital.enums;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 날짜 범위 필터 타입
 */
@Getter
public enum DateRangeFilter {
    ALL("전체", null),
    WEEK("최근 일주일", 7),
    MONTH("최근 한달", 30);

    private final String description;
    private final Integer days;

    DateRangeFilter(String description, Integer days) {
        this.description = description;
        this.days = days;
    }

    /**
     * 시작 날짜 계산
     * @return 필터에 따른 시작 날짜 (null이면 제한 없음)
     */
    public LocalDateTime getStartDateTime() {
        if (days == null) {
            return null;
        }
        return LocalDateTime.now().minusDays(days);
    }
}
