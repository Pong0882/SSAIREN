package com.ssairen.domain.emergency.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.entity.ReportSection;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ReportSectionRepositoryTest {

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
    @DisplayName("구급일지와 타입으로 섹션 존재 확인 - 존재함")
    void existsByEmergencyReportAndType_true() throws Exception {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        createTestReportSection(emergencyReport, ReportSectionType.PATIENT_INFO);

        // when
        boolean exists = reportSectionRepository.existsByEmergencyReportAndType(
                emergencyReport, ReportSectionType.PATIENT_INFO);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("구급일지와 타입으로 섹션 존재 확인 - 존재하지 않음")
    void existsByEmergencyReportAndType_false() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();

        // when
        boolean exists = reportSectionRepository.existsByEmergencyReportAndType(
                emergencyReport, ReportSectionType.PATIENT_INFO);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("구급일지와 타입으로 섹션 조회 - 성공")
    void findByEmergencyReportAndType_success() throws Exception {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        ReportSection section = createTestReportSection(emergencyReport, ReportSectionType.PATIENT_INFO);

        // when
        Optional<ReportSection> found = reportSectionRepository.findByEmergencyReportAndType(
                emergencyReport, ReportSectionType.PATIENT_INFO);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(section.getId());
        assertThat(found.get().getType()).isEqualTo(ReportSectionType.PATIENT_INFO);
    }

    @Test
    @DisplayName("구급일지와 타입으로 섹션 조회 - 빈 결과")
    void findByEmergencyReportAndType_empty() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();

        // when
        Optional<ReportSection> found = reportSectionRepository.findByEmergencyReportAndType(
                emergencyReport, ReportSectionType.PATIENT_INFO);

        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("다른 타입의 섹션 조회 - 빈 결과")
    void findByEmergencyReportAndType_differentType() throws Exception {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        createTestReportSection(emergencyReport, ReportSectionType.PATIENT_INFO);

        // when
        Optional<ReportSection> found = reportSectionRepository.findByEmergencyReportAndType(
                emergencyReport, ReportSectionType.TREATMENT);

        // then
        assertThat(found).isEmpty();
    }

    private ReportSection createTestReportSection(EmergencyReport emergencyReport, ReportSectionType type) throws Exception {
        JsonNode data = objectMapper.readTree("{\"test\": \"data\"}");

        ReportSection section = ReportSection.builder()
                .emergencyReport(emergencyReport)
                .type(type)
                .data(data)
                .version(1)
                .build();

        return reportSectionRepository.save(section);
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
