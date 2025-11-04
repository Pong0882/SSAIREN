package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.ReportSectionCreateResponse;
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
class ReportSectionServiceImplTest {

    @Autowired
    private ReportSectionService reportSectionService;

    @Autowired
    private EmergencyReportRepository emergencyReportRepository;

    @Autowired
    private DispatchRepository dispatchRepository;

    @Autowired
    private ParamedicRepository paramedicRepository;

    @Autowired
    private FireStateRepository fireStateRepository;

    @Autowired
    private ReportSectionRepository reportSectionRepository;

    @Test
    @DisplayName("구급일지 섹션 생성 - 성공")
    void createReportSection_success() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();

        // when
        ReportSectionCreateResponse response = reportSectionService.createReportSection(
                emergencyReport.getId(),
                ReportSectionType.PATIENT_INFO
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.type()).isEqualTo(ReportSectionType.PATIENT_INFO);
        assertThat(response.data()).isNotNull();
    }

    @Test
    @DisplayName("구급일지 섹션 생성 - 중복 생성 실패")
    void createReportSection_alreadyExists() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();

        // 첫 번째 섹션 생성
        reportSectionService.createReportSection(
                emergencyReport.getId(),
                ReportSectionType.PATIENT_INFO
        );

        // when & then - 동일한 타입의 섹션 중복 생성 시도
        assertThatThrownBy(() -> reportSectionService.createReportSection(
                emergencyReport.getId(),
                ReportSectionType.PATIENT_INFO
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REPORT_SECTION_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("구급일지 섹션 조회 - 성공")
    void getReportSection_success() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();

        reportSectionService.createReportSection(
                emergencyReport.getId(),
                ReportSectionType.DISPATCH
        );

        // when
        ReportSectionCreateResponse response = reportSectionService.getReportSection(
                emergencyReport.getId(),
                ReportSectionType.DISPATCH
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.type()).isEqualTo(ReportSectionType.DISPATCH);
        assertThat(response.data()).isNotNull();
    }

    private EmergencyReport createTestEmergencyReport() {
        FireState fireState = fireStateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 소방서 데이터가 없습니다."));

        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        Dispatch dispatch = Dispatch.builder()
                .disasterNumber("TEST-RS-" + System.currentTimeMillis())
                .disasterType("응급환자")
                .locationAddress("서울시 강남구")
                .date(LocalDateTime.now())
                .fireState(fireState)
                .build();
        dispatch = dispatchRepository.save(dispatch);

        EmergencyReport report = EmergencyReport.builder()
                .dispatch(dispatch)
                .paramedic(paramedic)
                .fireState(fireState)
                .build();

        return emergencyReportRepository.save(report);
    }
}
