package com.ssairen.domain.emergency.repository;

import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EmergencyReportRepositoryTest {

    @Autowired
    private EmergencyReportRepository emergencyReportRepository;

    @Autowired
    private DispatchRepository dispatchRepository;

    @Autowired
    private ParamedicRepository paramedicRepository;

    @Autowired
    private FireStateRepository fireStateRepository;

    @Test
    @DisplayName("출동지령으로 구급일지 존재 확인 - 존재함")
    void existsByDispatch_true() {
        // given
        Dispatch dispatch = createTestDispatch();
        EmergencyReport emergencyReport = createTestEmergencyReport(dispatch);

        // when
        boolean exists = emergencyReportRepository.existsByDispatch(dispatch);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("출동지령으로 구급일지 존재 확인 - 존재하지 않음")
    void existsByDispatch_false() {
        // given
        Dispatch dispatch = createTestDispatch();

        // when
        boolean exists = emergencyReportRepository.existsByDispatch(dispatch);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("구급대원 ID로 구급일지 조회 - 성공")
    void findByParamedicIdWithFetchJoin_success() {
        // given
        Dispatch dispatch = createTestDispatch();
        EmergencyReport emergencyReport = createTestEmergencyReport(dispatch);
        Paramedic paramedic = emergencyReport.getParamedic();

        // when
        List<EmergencyReport> reports = emergencyReportRepository.findByParamedicIdWithFetchJoin(paramedic.getId());

        // then
        assertThat(reports).isNotEmpty();
        assertThat(reports).anyMatch(r -> r.getId().equals(emergencyReport.getId()));
    }

    @Test
    @DisplayName("구급대원 ID로 구급일지 조회 - 빈 결과")
    void findByParamedicIdWithFetchJoin_empty() {
        // when
        List<EmergencyReport> reports = emergencyReportRepository.findByParamedicIdWithFetchJoin(99999);

        // then
        assertThat(reports).isEmpty();
    }

    @Test
    @DisplayName("소방서 ID로 구급일지 조회 - 성공")
    void findByFireStateIdWithFetchJoin_success() {
        // given
        Dispatch dispatch = createTestDispatch();
        EmergencyReport emergencyReport = createTestEmergencyReport(dispatch);
        FireState fireState = emergencyReport.getFireState();

        // when
        List<EmergencyReport> reports = emergencyReportRepository.findByFireStateIdWithFetchJoin(fireState.getId());

        // then
        assertThat(reports).isNotEmpty();
        assertThat(reports).anyMatch(r -> r.getId().equals(emergencyReport.getId()));
    }

    @Test
    @DisplayName("소방서 ID로 구급일지 조회 - 빈 결과")
    void findByFireStateIdWithFetchJoin_empty() {
        // when
        List<EmergencyReport> reports = emergencyReportRepository.findByFireStateIdWithFetchJoin(99999);

        // then
        assertThat(reports).isEmpty();
    }

    private EmergencyReport createTestEmergencyReport(Dispatch dispatch) {
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
