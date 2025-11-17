package com.ssairen.domain.emergency.validation;

import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.repository.EmergencyReportRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportSectionValidatorTest {

    @Mock
    private EmergencyReportRepository emergencyReportRepository;

    @InjectMocks
    private ReportSectionValidator validator;

    private Long emergencyReportId;
    private EmergencyReport emergencyReport;

    @BeforeEach
    void setUp() {
        emergencyReportId = 1L;
        emergencyReport = EmergencyReport.builder()
                .id(emergencyReportId)
                .build();
    }

    @Test
    @DisplayName("구급일지 존재 검증 - 성공")
    void validateEmergencyReportExists_success() {
        // given
        when(emergencyReportRepository.findById(emergencyReportId))
                .thenReturn(Optional.of(emergencyReport));

        // when
        EmergencyReport result = validator.validateEmergencyReportExists(emergencyReportId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(emergencyReportId);
        verify(emergencyReportRepository).findById(emergencyReportId);
    }

    @Test
    @DisplayName("구급일지 존재 검증 - 구급일지가 없는 경우")
    void validateEmergencyReportExists_notFound() {
        // given
        when(emergencyReportRepository.findById(emergencyReportId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> validator.validateEmergencyReportExists(emergencyReportId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMERGENCY_REPORT_NOT_FOUND);

        verify(emergencyReportRepository).findById(emergencyReportId);
    }

    @Test
    @DisplayName("구급일지 존재 검증 - 여러 ID로 조회")
    void validateEmergencyReportExists_multipleIds() {
        // given
        Long id1 = 1L;
        Long id2 = 2L;
        Long id3 = 3L;

        EmergencyReport report1 = EmergencyReport.builder().id(id1).build();
        EmergencyReport report2 = EmergencyReport.builder().id(id2).build();

        when(emergencyReportRepository.findById(id1)).thenReturn(Optional.of(report1));
        when(emergencyReportRepository.findById(id2)).thenReturn(Optional.of(report2));
        when(emergencyReportRepository.findById(id3)).thenReturn(Optional.empty());

        // when
        EmergencyReport result1 = validator.validateEmergencyReportExists(id1);
        EmergencyReport result2 = validator.validateEmergencyReportExists(id2);

        // then
        assertThat(result1.getId()).isEqualTo(id1);
        assertThat(result2.getId()).isEqualTo(id2);

        assertThatThrownBy(() -> validator.validateEmergencyReportExists(id3))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMERGENCY_REPORT_NOT_FOUND);
    }

    @Test
    @DisplayName("섹션 타입 검증 - 성공")
    void validateSectionType_success() {
        // given
        ReportSectionType type = ReportSectionType.PATIENT_INFO;

        // when & then
        // 예외가 발생하지 않아야 함
        validator.validateSectionType(type);
    }

    @Test
    @DisplayName("섹션 타입 검증 - null인 경우")
    void validateSectionType_null() {
        // when & then
        assertThatThrownBy(() -> validator.validateSectionType(null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_REPORT_SECTION_TYPE);
    }

    @Test
    @DisplayName("섹션 타입 검증 - 모든 타입 검증")
    void validateSectionType_allTypes() {
        // given
        ReportSectionType[] types = ReportSectionType.values();

        // when & then
        for (ReportSectionType type : types) {
            // 모든 타입이 유효해야 함 (예외가 발생하지 않아야 함)
            validator.validateSectionType(type);
        }
    }

    @Test
    @DisplayName("섹션 타입 검증 - DISPATCH 타입")
    void validateSectionType_dispatch() {
        // given
        ReportSectionType type = ReportSectionType.DISPATCH;

        // when & then
        validator.validateSectionType(type);
    }

    @Test
    @DisplayName("섹션 타입 검증 - INCIDENT_TYPE 타입")
    void validateSectionType_incidentType() {
        // given
        ReportSectionType type = ReportSectionType.INCIDENT_TYPE;

        // when & then
        validator.validateSectionType(type);
    }

    @Test
    @DisplayName("섹션 타입 검증 - PATIENT_INFO 타입")
    void validateSectionType_patientInfo() {
        // given
        ReportSectionType type = ReportSectionType.PATIENT_INFO;

        // when & then
        validator.validateSectionType(type);
    }

    @Test
    @DisplayName("섹션 타입 검증 - ASSESSMENT 타입")
    void validateSectionType_assessment() {
        // given
        ReportSectionType type = ReportSectionType.ASSESSMENT;

        // when & then
        validator.validateSectionType(type);
    }

    @Test
    @DisplayName("섹션 타입 검증 - TRANSPORT 타입")
    void validateSectionType_transport() {
        // given
        ReportSectionType type = ReportSectionType.TRANSPORT;

        // when & then
        validator.validateSectionType(type);
    }
}
