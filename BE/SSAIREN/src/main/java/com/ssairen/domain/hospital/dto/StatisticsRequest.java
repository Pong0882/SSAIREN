package com.ssairen.domain.hospital.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "통계 조회 요청")
public record StatisticsRequest(
        @NotNull(message = "시작 날짜는 필수 입력 항목입니다.")
        @Schema(description = "통계 시작 날짜", example = "2025-01-01")
        LocalDate startDate,

        @NotNull(message = "종료 날짜는 필수 입력 항목입니다.")
        @Schema(description = "통계 종료 날짜", example = "2025-01-31")
        LocalDate endDate
) {
    public StatisticsRequest {
        // 날짜 범위 검증
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }
    }
}
