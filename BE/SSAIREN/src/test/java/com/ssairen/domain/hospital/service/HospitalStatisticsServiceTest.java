package com.ssairen.domain.hospital.service;

import com.ssairen.domain.hospital.dto.DisasterTypeStatisticsResponse;
import com.ssairen.domain.hospital.dto.PatientStatisticsResponse;
import com.ssairen.domain.hospital.dto.StatisticsRequest;
import com.ssairen.domain.hospital.dto.TimeStatisticsResponse;
import com.ssairen.domain.hospital.repository.HospitalRepository;
import com.ssairen.domain.hospital.repository.HospitalSelectionRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalStatisticsServiceTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private HospitalSelectionRepository hospitalSelectionRepository;

    @InjectMocks
    private HospitalStatisticsService service;

    private Integer hospitalId;
    private StatisticsRequest request;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    @BeforeEach
    void setUp() {
        hospitalId = 1;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        request = new StatisticsRequest(startDate, endDate);
        startDateTime = startDate.atStartOfDay();
        endDateTime = endDate.plusDays(1).atStartOfDay();
    }

    @Test
    @DisplayName("시간별 통계 조회 - 성공")
    void getTimeStatistics_success() {
        // given
        when(hospitalRepository.existsById(hospitalId)).thenReturn(true);

        // 요일별 통계 Mock 데이터
        List<Object[]> dayOfWeekResults = Arrays.asList(
                new Object[]{0, 10L}, // SUNDAY: 10
                new Object[]{1, 15L}, // MONDAY: 15
                new Object[]{2, 20L}  // TUESDAY: 20
        );
        when(hospitalSelectionRepository.countByDayOfWeek(eq(hospitalId), any(), any()))
                .thenReturn(dayOfWeekResults);

        // 시간대별 통계 Mock 데이터
        List<Object[]> hourResults = Arrays.asList(
                new Object[]{9, 5L},  // 9시: 5건
                new Object[]{10, 8L}, // 10시: 8건
                new Object[]{14, 12L} // 14시: 12건
        );
        when(hospitalSelectionRepository.countByHour(eq(hospitalId), any(), any()))
                .thenReturn(hourResults);

        when(hospitalSelectionRepository.countByHospitalIdAndPeriod(eq(hospitalId), any(), any()))
                .thenReturn(100L);

        // when
        TimeStatisticsResponse response = service.getTimeStatistics(request, hospitalId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.totalCount()).isEqualTo(100L);
        assertThat(response.byDayOfWeek()).containsEntry("SUNDAY", 10L);
        assertThat(response.byDayOfWeek()).containsEntry("MONDAY", 15L);
        assertThat(response.byHour()).containsEntry("9", 5L);
        assertThat(response.byHour()).containsEntry("10", 8L);

        // 모든 요일이 초기화되어 있는지 확인
        assertThat(response.byDayOfWeek()).hasSize(7);
        // 모든 시간대가 초기화되어 있는지 확인
        assertThat(response.byHour()).hasSize(24);

        verify(hospitalRepository).existsById(hospitalId);
        verify(hospitalSelectionRepository).countByDayOfWeek(eq(hospitalId), any(), any());
        verify(hospitalSelectionRepository).countByHour(eq(hospitalId), any(), any());
    }

    @Test
    @DisplayName("시간별 통계 조회 - 병원 없음")
    void getTimeStatistics_hospitalNotFound() {
        // given
        when(hospitalRepository.existsById(hospitalId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> service.getTimeStatistics(request, hospitalId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HOSPITAL_NOT_FOUND);

        verify(hospitalRepository).existsById(hospitalId);
        verify(hospitalSelectionRepository, never()).countByDayOfWeek(any(), any(), any());
    }

    @Test
    @DisplayName("시간별 통계 조회 - 데이터 없음")
    void getTimeStatistics_noData() {
        // given
        when(hospitalRepository.existsById(hospitalId)).thenReturn(true);
        when(hospitalSelectionRepository.countByDayOfWeek(eq(hospitalId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(hospitalSelectionRepository.countByHour(eq(hospitalId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(hospitalSelectionRepository.countByHospitalIdAndPeriod(eq(hospitalId), any(), any()))
                .thenReturn(0L);

        // when
        TimeStatisticsResponse response = service.getTimeStatistics(request, hospitalId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.totalCount()).isEqualTo(0L);
        // 빈 데이터여도 모든 요일/시간대는 0으로 초기화
        assertThat(response.byDayOfWeek()).hasSize(7);
        assertThat(response.byHour()).hasSize(24);
        assertThat(response.byDayOfWeek().values()).allMatch(count -> count == 0L);
    }

    @Test
    @DisplayName("환자 통계 조회 - 성공")
    void getPatientStatistics_success() {
        // given
        when(hospitalRepository.existsById(hospitalId)).thenReturn(true);

        // 성별 통계
        List<Object[]> genderResults = Arrays.asList(
                new Object[]{"M", 60L},
                new Object[]{"F", 40L}
        );
        when(hospitalSelectionRepository.countByGender(eq(hospitalId), any(), any()))
                .thenReturn(genderResults);

        // 연령대 통계
        List<Object[]> ageGroupResults = Arrays.asList(
                new Object[]{"20-29", 15L},
                new Object[]{"30-39", 25L},
                new Object[]{"40-49", 30L}
        );
        when(hospitalSelectionRepository.countByAgeGroup(eq(hospitalId), any(), any()))
                .thenReturn(ageGroupResults);

        // 의식 상태 통계
        List<Object[]> mentalStatusResults = Arrays.asList(
                new Object[]{"ALERT", 70L},
                new Object[]{"VERBAL", 20L},
                new Object[]{"PAIN", 8L},
                new Object[]{"UNRESPONSIVE", 2L}
        );
        when(hospitalSelectionRepository.countByMentalStatus(eq(hospitalId), any(), any()))
                .thenReturn(mentalStatusResults);

        when(hospitalSelectionRepository.countByHospitalIdAndPeriod(eq(hospitalId), any(), any()))
                .thenReturn(100L);

        // when
        PatientStatisticsResponse response = service.getPatientStatistics(request, hospitalId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.totalCount()).isEqualTo(100L);
        assertThat(response.byGender()).containsEntry("M", 60L);
        assertThat(response.byGender()).containsEntry("F", 40L);
        assertThat(response.byAgeGroup()).containsEntry("20-29", 15L);
        assertThat(response.byMentalStatus()).containsEntry("ALERT", 70L);

        verify(hospitalRepository).existsById(hospitalId);
        verify(hospitalSelectionRepository).countByGender(eq(hospitalId), any(), any());
        verify(hospitalSelectionRepository).countByAgeGroup(eq(hospitalId), any(), any());
        verify(hospitalSelectionRepository).countByMentalStatus(eq(hospitalId), any(), any());
    }

    @Test
    @DisplayName("환자 통계 조회 - 병원 없음")
    void getPatientStatistics_hospitalNotFound() {
        // given
        when(hospitalRepository.existsById(hospitalId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> service.getPatientStatistics(request, hospitalId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HOSPITAL_NOT_FOUND);

        verify(hospitalRepository).existsById(hospitalId);
        verify(hospitalSelectionRepository, never()).countByGender(any(), any(), any());
    }

    @Test
    @DisplayName("환자 통계 조회 - 모든 연령대 초기화 확인")
    void getPatientStatistics_allAgeGroupsInitialized() {
        // given
        when(hospitalRepository.existsById(hospitalId)).thenReturn(true);
        when(hospitalSelectionRepository.countByGender(eq(hospitalId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(hospitalSelectionRepository.countByAgeGroup(eq(hospitalId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(hospitalSelectionRepository.countByMentalStatus(eq(hospitalId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(hospitalSelectionRepository.countByHospitalIdAndPeriod(eq(hospitalId), any(), any()))
                .thenReturn(0L);

        // when
        PatientStatisticsResponse response = service.getPatientStatistics(request, hospitalId);

        // then
        assertThat(response.byAgeGroup()).hasSize(9); // 0-9, 10-19, ..., 80+
        assertThat(response.byAgeGroup()).containsKeys("0-9", "10-19", "20-29", "30-39",
                "40-49", "50-59", "60-69", "70-79", "80+");
        assertThat(response.byGender()).containsEntry("M", 0L);
        assertThat(response.byGender()).containsEntry("F", 0L);
        assertThat(response.byMentalStatus()).hasSize(4);
    }

    @Test
    @DisplayName("재난 유형별 통계 조회 - 성공")
    void getDisasterTypeStatistics_success() {
        // given
        when(hospitalRepository.existsById(hospitalId)).thenReturn(true);

        // 재난 유형 통계
        List<Object[]> disasterTypeResults = Arrays.asList(
                new Object[]{"화재", 30L},
                new Object[]{"교통사고", 50L},
                new Object[]{"질병", 20L}
        );
        when(hospitalSelectionRepository.countByDisasterType(eq(hospitalId), any(), any()))
                .thenReturn(disasterTypeResults);

        // 재난 세부 유형 통계
        List<Object[]> disasterSubtypeResults = Arrays.asList(
                new Object[]{"건물화재", 20L},
                new Object[]{"차량화재", 10L},
                new Object[]{"승용차 추돌", 30L}
        );
        when(hospitalSelectionRepository.countByDisasterSubtype(eq(hospitalId), any(), any()))
                .thenReturn(disasterSubtypeResults);

        when(hospitalSelectionRepository.countByHospitalIdAndPeriod(eq(hospitalId), any(), any()))
                .thenReturn(100L);

        // when
        DisasterTypeStatisticsResponse response = service.getDisasterTypeStatistics(request, hospitalId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.totalCount()).isEqualTo(100L);
        assertThat(response.byDisasterType()).containsEntry("화재", 30L);
        assertThat(response.byDisasterType()).containsEntry("교통사고", 50L);
        assertThat(response.byDisasterSubtype()).containsEntry("건물화재", 20L);
        assertThat(response.byDisasterSubtype()).containsEntry("승용차 추돌", 30L);

        verify(hospitalRepository).existsById(hospitalId);
        verify(hospitalSelectionRepository).countByDisasterType(eq(hospitalId), any(), any());
        verify(hospitalSelectionRepository).countByDisasterSubtype(eq(hospitalId), any(), any());
    }

    @Test
    @DisplayName("재난 유형별 통계 조회 - 병원 없음")
    void getDisasterTypeStatistics_hospitalNotFound() {
        // given
        when(hospitalRepository.existsById(hospitalId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> service.getDisasterTypeStatistics(request, hospitalId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HOSPITAL_NOT_FOUND);

        verify(hospitalRepository).existsById(hospitalId);
        verify(hospitalSelectionRepository, never()).countByDisasterType(any(), any(), any());
    }

    @Test
    @DisplayName("재난 유형별 통계 조회 - NULL 값 처리")
    void getDisasterTypeStatistics_nullValues() {
        // given
        when(hospitalRepository.existsById(hospitalId)).thenReturn(true);

        // NULL 값이 포함된 통계 데이터
        List<Object[]> disasterTypeResults = Arrays.asList(
                new Object[]{null, 10L}, // NULL -> "미분류"
                new Object[]{"화재", 20L}
        );
        when(hospitalSelectionRepository.countByDisasterType(eq(hospitalId), any(), any()))
                .thenReturn(disasterTypeResults);

        List<Object[]> disasterSubtypeResults = Arrays.asList(
                new Object[]{null, 5L},
                new Object[]{"건물화재", 15L}
        );
        when(hospitalSelectionRepository.countByDisasterSubtype(eq(hospitalId), any(), any()))
                .thenReturn(disasterSubtypeResults);

        when(hospitalSelectionRepository.countByHospitalIdAndPeriod(eq(hospitalId), any(), any()))
                .thenReturn(30L);

        // when
        DisasterTypeStatisticsResponse response = service.getDisasterTypeStatistics(request, hospitalId);

        // then
        assertThat(response.byDisasterType()).containsEntry("미분류", 10L);
        assertThat(response.byDisasterSubtype()).containsEntry("미분류", 5L);
    }

    @Test
    @DisplayName("시간별 통계 조회 - 날짜 범위 변환 확인")
    void getTimeStatistics_dateRangeConversion() {
        // given
        when(hospitalRepository.existsById(hospitalId)).thenReturn(true);
        when(hospitalSelectionRepository.countByDayOfWeek(eq(hospitalId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(hospitalSelectionRepository.countByHour(eq(hospitalId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(hospitalSelectionRepository.countByHospitalIdAndPeriod(eq(hospitalId), any(), any()))
                .thenReturn(0L);

        // when
        service.getTimeStatistics(request, hospitalId);

        // then
        verify(hospitalSelectionRepository).countByDayOfWeek(
                eq(hospitalId),
                eq(startDateTime),
                eq(endDateTime)
        );
    }
}
