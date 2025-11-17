package com.ssairen.domain.emergency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssairen.domain.emergency.dto.ReportSectionUpdateRequest;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.repository.DispatchRepository;
import com.ssairen.domain.emergency.repository.EmergencyReportRepository;
import com.ssairen.domain.emergency.repository.ReportSectionRepository;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ReportSectionServiceTest {

    @Autowired
    private ReportSectionService reportSectionService;

    @Autowired
    private ReportSectionRepository reportSectionRepository;

    @Autowired
    private EmergencyReportRepository emergencyReportRepository;

    @Autowired
    private DispatchRepository dispatchRepository;

    @Autowired
    private ParamedicRepository paramedicRepository;

    @Autowired
    private FireStateRepository fireStateRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("섹션 생성 - 성공")
    void createReportSection_success() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        // when
        var response = reportSectionService.createReportSection(
                emergencyReport.getId(),
                ReportSectionType.PATIENT_INFO,
                paramedic.getId()
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.type()).isEqualTo(ReportSectionType.PATIENT_INFO);
    }

    @Test
    @DisplayName("섹션 생성 - EmergencyReport 없음 실패")
    void createReportSection_reportNotFound() {
        // given
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        // when & then
        assertThatThrownBy(() -> reportSectionService.createReportSection(
                99999L,
                ReportSectionType.PATIENT_INFO,
                paramedic.getId()
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMERGENCY_REPORT_NOT_FOUND);
    }

    @Test
    @DisplayName("섹션 조회 - 성공")
    void getReportSection_success() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        reportSectionService.createReportSection(
                emergencyReport.getId(),
                ReportSectionType.PATIENT_INFO,
                paramedic.getId()
        );

        // when
        var response = reportSectionService.getReportSection(
                emergencyReport.getId(),
                ReportSectionType.PATIENT_INFO,
                paramedic.getId()
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.type()).isEqualTo(ReportSectionType.PATIENT_INFO);
    }

    @Test
    @DisplayName("섹션 조회 - 섹션 없음 실패")
    void getReportSection_sectionNotFound() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        // when & then
        assertThatThrownBy(() -> reportSectionService.getReportSection(
                emergencyReport.getId(),
                ReportSectionType.PATIENT_INFO,
                paramedic.getId()
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REPORT_SECTION_NOT_FOUND);
    }

    @Test
    @DisplayName("섹션 수정 - 성공")
    void updateReportSection_success() throws Exception {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        reportSectionService.createReportSection(
                emergencyReport.getId(),
                ReportSectionType.PATIENT_INFO,
                paramedic.getId()
        );

        JsonNode updateData = objectMapper.readTree("{\"name\": \"홍길동\", \"age\": 30}");
        ReportSectionUpdateRequest updateRequest = new ReportSectionUpdateRequest(updateData);

        // when
        var response = reportSectionService.updateReportSection(
                emergencyReport.getId(),
                ReportSectionType.PATIENT_INFO,
                updateRequest,
                paramedic.getId()
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.type()).isEqualTo(ReportSectionType.PATIENT_INFO);
    }

    @Test
    @DisplayName("섹션 수정 - 섹션 없음 실패")
    void updateReportSection_sectionNotFound() throws Exception {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        JsonNode updateData = objectMapper.readTree("{\"name\": \"홍길동\"}");
        ReportSectionUpdateRequest updateRequest = new ReportSectionUpdateRequest(updateData);

        // when & then
        assertThatThrownBy(() -> reportSectionService.updateReportSection(
                emergencyReport.getId(),
                ReportSectionType.PATIENT_INFO,
                updateRequest,
                paramedic.getId()
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REPORT_SECTION_NOT_FOUND);
    }

    private EmergencyReport createTestEmergencyReport() {
        Dispatch dispatch = createTestDispatch();
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));
        FireState fireState = fireStateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 소방서 데이터가 없습니다."));

        EmergencyReport report = EmergencyReport.builder()
                .dispatch(dispatch)
                .paramedic(paramedic)
                .fireState(fireState)
                .isCompleted(false)
                .build();

        return emergencyReportRepository.save(report);
    }

    private Dispatch createTestDispatch() {
        FireState fireState = fireStateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 소방서 데이터가 없습니다."));
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        Dispatch dispatch = Dispatch.builder()
                .fireState(fireState)
                .paramedic(paramedic)
                .disasterNumber("TEST-" + System.currentTimeMillis())
                .disasterType("화재")
                .disasterSubtype("건물화재")
                .reporterName("홍길동")
                .reporterPhone("010-1234-5678")
                .locationAddress("서울시 강남구")
                .incidentDescription("테스트 화재")
                .dispatchLevel("일반")
                .dispatchOrder(1)
                .dispatchStation("테스트119센터")
                .date(LocalDateTime.now())
                .build();

        return dispatchRepository.save(dispatch);
    }
}
