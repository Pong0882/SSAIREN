package com.ssairen.domain.emergency.service;

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
class EmergencyReportServiceTest {

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
        Dispatch dispatch = createTestDispatch();
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        // when
        var response = emergencyReportService.createEmergencyReport(dispatch.getId(), paramedic.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.emergencyReportId()).isNotNull();
        assertThat(response.paramedicInfo()).isNotNull();
        assertThat(response.paramedicInfo().paramedicId()).isEqualTo(paramedic.getId());
    }

    @Test
    @DisplayName("구급일지 생성 - Dispatch 없음 실패")
    void createEmergencyReport_dispatchNotFound() {
        // given
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        // when & then
        assertThatThrownBy(() -> emergencyReportService.createEmergencyReport(99999L, paramedic.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DISPATCH_NOT_FOUND);
    }

    @Test
    @DisplayName("구급일지 생성 - Paramedic 없음 실패")
    void createEmergencyReport_paramedicNotFound() {
        // given
        Dispatch dispatch = createTestDispatch();

        // when & then
        assertThatThrownBy(() -> emergencyReportService.createEmergencyReport(dispatch.getId(), 99999))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARAMEDIC_NOT_FOUND);
    }

    @Test
    @DisplayName("구급대원이 작성한 보고서 조회 - 성공")
    void getEmergencyReportsByParamedic_success() {
        // given
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        // when
        var response = emergencyReportService.getEmergencyReportsByParamedic(paramedic.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.emergencyReports()).isNotNull();
    }

    @Test
    @DisplayName("구급대원이 작성한 보고서 조회 - Paramedic 없음 실패")
    void getEmergencyReportsByParamedic_paramedicNotFound() {
        // when & then
        assertThatThrownBy(() -> emergencyReportService.getEmergencyReportsByParamedic(99999))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARAMEDIC_NOT_FOUND);
    }

    @Test
    @DisplayName("소방서 보고서 조회 - 성공")
    void getEmergencyReportsByFireState_success() {
        // given
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        // when
        var response = emergencyReportService.getEmergencyReportsByFireState(paramedic.getId());

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("구급일지 완료 상태 변경 - 성공")
    void toggleEmergencyReportCompleted_success() {
        // given
        Dispatch dispatch = createTestDispatch();
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        var createResponse = emergencyReportService.createEmergencyReport(dispatch.getId(), paramedic.getId());

        // when
        var response = emergencyReportService.toggleEmergencyReportCompleted(createResponse.emergencyReportId(), paramedic.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.emergencyReportId()).isEqualTo(createResponse.emergencyReportId());
    }

    @Test
    @DisplayName("구급일지 완료 상태 변경 - EmergencyReport 없음 실패")
    void toggleEmergencyReportCompleted_reportNotFound() {
        // given
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        // when & then
        assertThatThrownBy(() -> emergencyReportService.toggleEmergencyReportCompleted(99999L, paramedic.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMERGENCY_REPORT_NOT_FOUND);
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
