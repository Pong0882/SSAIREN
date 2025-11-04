package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.EmergencyReportCreateResponse;
import com.ssairen.domain.emergency.dto.ParamedicEmergencyReportResponse;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.repository.DispatchRepository;
import com.ssairen.domain.emergency.repository.EmergencyReportRepository;
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
class EmergencyReportServiceImplTest {

    @Autowired
    private EmergencyReportService emergencyReportService;

    @Autowired
    private EmergencyReportRepository emergencyReportRepository;

    @Autowired
    private DispatchRepository dispatchRepository;

    @Autowired
    private ParamedicRepository paramedicRepository;

    @Autowired
    private FireStateRepository fireStateRepository;

    @Test
    @DisplayName("구급일지 생성 - 성공")
    void createEmergencyReport_success() {
        // given
        FireState fireState = fireStateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 소방서 데이터가 없습니다."));

        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        Dispatch dispatch = Dispatch.builder()
                .disasterNumber("TEST-ER-001")
                .disasterType("응급환자")
                .locationAddress("서울시 강남구")
                .date(LocalDateTime.now())
                .fireState(fireState)
                .build();
        dispatch = dispatchRepository.save(dispatch);

        // when
        EmergencyReportCreateResponse response = emergencyReportService.createEmergencyReport(
                dispatch.getId(),
                paramedic.getId()
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.emergencyReportId()).isNotNull();
        assertThat(response.paramedicInfo().paramedicId()).isEqualTo(paramedic.getId());
        assertThat(response.dispatchInfo().dispatchId()).isEqualTo(dispatch.getId());
    }

    @Test
    @DisplayName("구급일지 생성 - 출동지령 없음 실패")
    void createEmergencyReport_dispatchNotFound() {
        // given
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        Long nonExistentDispatchId = 99999L;  // 존재하지 않는 출동지령 ID

        // when & then
        assertThatThrownBy(() -> emergencyReportService.createEmergencyReport(
                nonExistentDispatchId,
                paramedic.getId()
        ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DISPATCH_NOT_FOUND);
    }

    @Test
    @DisplayName("구급대원별 구급일지 조회 - 성공")
    void getEmergencyReportsByParamedic_success() {
        // given
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        FireState fireState = fireStateRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 소방서 데이터가 없습니다."));

        // 테스트용 출동지령 및 구급일지 생성
        Dispatch dispatch = Dispatch.builder()
                .disasterNumber("TEST-ER-002")
                .disasterType("응급환자")
                .locationAddress("서울시 서초구")
                .date(LocalDateTime.now())
                .fireState(fireState)
                .build();
        dispatch = dispatchRepository.save(dispatch);

        EmergencyReport report = EmergencyReport.builder()
                .dispatch(dispatch)
                .paramedic(paramedic)
                .fireState(fireState)
                .build();
        emergencyReportRepository.save(report);

        // when
        ParamedicEmergencyReportResponse response = emergencyReportService.getEmergencyReportsByParamedic(paramedic.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.paramedicInfo().paramedicId()).isEqualTo(paramedic.getId());
        assertThat(response.emergencyReports()).isNotEmpty();
    }
}
