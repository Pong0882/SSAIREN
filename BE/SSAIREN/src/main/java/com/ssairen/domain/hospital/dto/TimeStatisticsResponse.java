package com.ssairen.domain.hospital.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.Map;

@Schema(description = "시간별 통계 응답")
public record TimeStatisticsResponse(
        @Schema(description = "요일별 환자 수용 건수", example = "{\"MONDAY\": 45, \"TUESDAY\": 52}")
        Map<String, Long> byDayOfWeek,

        @Schema(description = "시간대별 환자 수용 건수 (0-23시)", example = "{\"0\": 12, \"1\": 8, \"23\": 15}")
        Map<String, Long> byHour,

        @Schema(description = "통계 시작 날짜", example = "2025-01-01")
        LocalDate startDate,

        @Schema(description = "통계 종료 날짜", example = "2025-01-31")
        LocalDate endDate,

        @Schema(description = "총 수용 건수", example = "292")
        Long totalCount
) {
}
