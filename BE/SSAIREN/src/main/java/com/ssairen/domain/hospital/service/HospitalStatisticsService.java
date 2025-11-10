package com.ssairen.domain.hospital.service;

import com.ssairen.domain.hospital.dto.StatisticsRequest;
import com.ssairen.domain.hospital.dto.TimeStatisticsResponse;
import com.ssairen.domain.hospital.repository.HospitalRepository;
import com.ssairen.domain.hospital.repository.HospitalSelectionRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 병원 통계 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalStatisticsService {

    private final HospitalRepository hospitalRepository;
    private final HospitalSelectionRepository hospitalSelectionRepository;

    // 요일 매핑 (PostgreSQL DOW: 0=일요일, 1=월요일, ..., 6=토요일)
    private static final Map<Integer, String> DAY_OF_WEEK_MAP = Map.of(
            0, "SUNDAY",
            1, "MONDAY",
            2, "TUESDAY",
            3, "WEDNESDAY",
            4, "THURSDAY",
            5, "FRIDAY",
            6, "SATURDAY"
    );

    /**
     * 시간별 통계 조회
     *
     * @param request 통계 조회 요청 (startDate, endDate)
     * @param hospitalId 병원 ID
     * @return 요일별, 시간대별 통계
     */
    public TimeStatisticsResponse getTimeStatistics(StatisticsRequest request, Integer hospitalId) {
        log.info("📊 Fetching time statistics for hospital ID: {}, period: {} ~ {}",
                hospitalId, request.startDate(), request.endDate());

        // 1. 병원 존재 확인
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new CustomException(ErrorCode.HOSPITAL_NOT_FOUND);
        }

        // 2. LocalDate → LocalDateTime 변환
        LocalDateTime startDateTime = request.startDate().atStartOfDay();
        LocalDateTime endDateTime = request.endDate().plusDays(1).atStartOfDay(); // 종료일 23:59:59까지 포함

        // 3. 요일별 통계 조회
        Map<String, Long> byDayOfWeek = getDayOfWeekStatistics(hospitalId, startDateTime, endDateTime);

        // 4. 시간대별 통계 조회
        Map<String, Long> byHour = getHourStatistics(hospitalId, startDateTime, endDateTime);

        // 5. 총 수용 건수
        long totalCount = hospitalSelectionRepository.countByHospitalIdAndPeriod(
                hospitalId, startDateTime, endDateTime
        );

        log.info("✅ Time statistics calculated - Total: {}, DayOfWeek: {}, Hour: {}",
                totalCount, byDayOfWeek.size(), byHour.size());

        return new TimeStatisticsResponse(
                byDayOfWeek,
                byHour,
                request.startDate(),
                request.endDate(),
                totalCount
        );
    }

    /**
     * 요일별 통계 계산
     */
    private Map<String, Long> getDayOfWeekStatistics(
            Integer hospitalId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        List<Object[]> results = hospitalSelectionRepository.countByDayOfWeek(
                hospitalId, startDateTime, endDateTime
        );

        Map<String, Long> statistics = new HashMap<>();

        // 모든 요일을 0으로 초기화
        for (String dayName : DAY_OF_WEEK_MAP.values()) {
            statistics.put(dayName, 0L);
        }

        // 쿼리 결과를 Map으로 변환
        for (Object[] result : results) {
            // PostgreSQL의 EXTRACT(DOW)는 Double 타입으로 반환됨
            int dayOfWeek = ((Number) result[0]).intValue();
            Long count = ((Number) result[1]).longValue();

            String dayName = DAY_OF_WEEK_MAP.get(dayOfWeek);
            if (dayName != null) {
                statistics.put(dayName, count);
            }
        }

        return statistics;
    }

    /**
     * 시간대별 통계 계산
     */
    private Map<String, Long> getHourStatistics(
            Integer hospitalId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        List<Object[]> results = hospitalSelectionRepository.countByHour(
                hospitalId, startDateTime, endDateTime
        );

        Map<String, Long> statistics = new HashMap<>();

        // 모든 시간대를 0으로 초기화 (0~23시)
        for (int hour = 0; hour < 24; hour++) {
            statistics.put(String.valueOf(hour), 0L);
        }

        // 쿼리 결과를 Map으로 변환
        for (Object[] result : results) {
            int hour = ((Number) result[0]).intValue();
            Long count = ((Number) result[1]).longValue();

            statistics.put(String.valueOf(hour), count);
        }

        return statistics;
    }
}
