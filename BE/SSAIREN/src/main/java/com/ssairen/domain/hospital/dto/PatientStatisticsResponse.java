package com.ssairen.domain.hospital.dto;

import java.time.LocalDate;
import java.util.Map;

/**
 * 환자 통계 응답 DTO
 * - 성별 분포
 * - 연령대 분포
 * - 의식 상태 분포
 */
public record PatientStatisticsResponse(
        Map<String, Long> byGender,        // { "M": 120, "F": 95 }
        Map<String, Long> byAgeGroup,      // { "0-9": 5, "10-19": 12, "20-29": 25, ... }
        Map<String, Long> byMentalStatus,  // { "ALERT": 80, "VERBAL": 30, "PAIN": 15, "UNRESPONSIVE": 10 }
        LocalDate startDate,
        LocalDate endDate,
        Long totalCount
) {}
