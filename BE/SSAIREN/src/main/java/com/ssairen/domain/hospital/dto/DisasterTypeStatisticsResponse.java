package com.ssairen.domain.hospital.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Map;

/**
 * 재난 유형별 통계 응답 DTO
 */
@Schema(description = "재난 유형별 통계 응답")
public record DisasterTypeStatisticsResponse(
        @Schema(description = "재난 유형별 환자 수용 건수", example = "{\"질병\": 120, \"교통사고\": 95, \"추락\": 30}")
        Map<String, Long> byDisasterType,

        @Schema(description = "재난 세부 유형별 환자 수용 건수", example = "{\"심정지\": 45, \"뇌졸중\": 75}")
        Map<String, Long> byDisasterSubtype,

        @Schema(description = "통계 시작 날짜", example = "2025-01-01")
        LocalDate startDate,

        @Schema(description = "통계 종료 날짜", example = "2025-01-31")
        LocalDate endDate,

        @Schema(description = "총 수용 건수", example = "292")
        Long totalCount
) {
}
