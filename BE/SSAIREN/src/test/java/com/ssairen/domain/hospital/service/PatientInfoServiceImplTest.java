package com.ssairen.domain.hospital.service;

import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.repository.DispatchRepository;
import com.ssairen.domain.emergency.repository.EmergencyReportRepository;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import com.ssairen.domain.hospital.dto.PatientInfoCreateRequest;
import com.ssairen.domain.hospital.entity.PatientInfo;
import com.ssairen.domain.hospital.repository.PatientInfoRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PatientInfoServiceImplTest {

    @Autowired
    private PatientInfoService patientInfoService;

    @Autowired
    private PatientInfoRepository patientInfoRepository;

    @Autowired
    private EmergencyReportRepository emergencyReportRepository;

    @Autowired
    private DispatchRepository dispatchRepository;

    @Autowired
    private ParamedicRepository paramedicRepository;

    @Autowired
    private FireStateRepository fireStateRepository;

    @Test
    @DisplayName("환자 정보 생성 - 성공")
    void createPatientInfo_success() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = emergencyReport.getParamedic();

        PatientInfoCreateRequest request = new PatientInfoCreateRequest(
                emergencyReport.getId(),
                PatientInfo.Gender.M,
                45,
                LocalDateTime.now(),
                PatientInfo.MentalStatus.ALERT,
                "복통, 구토",
                85,
                "120/80",
                99,
                16,
                new BigDecimal("36.5"),
                true,
                "고혈압, 당뇨",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(3)
        );

        // when
        var response = patientInfoService.createPatientInfo(request, paramedic.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.emergencyReportId()).isEqualTo(emergencyReport.getId());
        assertThat(response.gender()).isEqualTo(PatientInfo.Gender.M);
        assertThat(response.age()).isEqualTo(45);
    }

    @Test
    @DisplayName("환자 정보 생성 - EmergencyReport 없음 실패")
    void createPatientInfo_emergencyReportNotFound() {
        // given
        Paramedic paramedic = paramedicRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 구급대원 데이터가 없습니다."));

        PatientInfoCreateRequest request = new PatientInfoCreateRequest(
                99999L,
                PatientInfo.Gender.M,
                45,
                LocalDateTime.now(),
                PatientInfo.MentalStatus.ALERT,
                "복통",
                85,
                "120/80",
                99,
                16,
                new BigDecimal("36.5"),
                true,
                "고혈압",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(3)
        );

        // when & then
        assertThatThrownBy(() -> patientInfoService.createPatientInfo(request, paramedic.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMERGENCY_REPORT_NOT_FOUND);
    }

    @Test
    @DisplayName("환자 정보 생성 - 권한 없음 실패")
    void createPatientInfo_accessDenied() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();

        // 다른 구급대원 찾기
        Paramedic anotherParamedic = paramedicRepository.findAll().stream()
                .filter(p -> !p.getId().equals(emergencyReport.getParamedic().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 다른 구급대원 데이터가 없습니다."));

        PatientInfoCreateRequest request = new PatientInfoCreateRequest(
                emergencyReport.getId(),
                PatientInfo.Gender.M,
                45,
                LocalDateTime.now(),
                PatientInfo.MentalStatus.ALERT,
                "복통",
                85,
                "120/80",
                99,
                16,
                new BigDecimal("36.5"),
                true,
                "고혈압",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(3)
        );

        // when & then
        assertThatThrownBy(() -> patientInfoService.createPatientInfo(request, anotherParamedic.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("환자 정보 생성 - 이미 존재 실패")
    void createPatientInfo_alreadyExists() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = emergencyReport.getParamedic();

        PatientInfoCreateRequest request = new PatientInfoCreateRequest(
                emergencyReport.getId(),
                PatientInfo.Gender.M,
                45,
                LocalDateTime.now(),
                PatientInfo.MentalStatus.ALERT,
                "복통",
                85,
                "120/80",
                99,
                16,
                new BigDecimal("36.5"),
                true,
                "고혈압",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(3)
        );

        // 첫 번째 생성
        patientInfoService.createPatientInfo(request, paramedic.getId());

        // when & then - 두 번째 생성 시도
        assertThatThrownBy(() -> patientInfoService.createPatientInfo(request, paramedic.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PATIENT_INFO_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("환자 정보 조회 - 성공")
    void getPatientInfo_success() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = emergencyReport.getParamedic();

        PatientInfoCreateRequest request = new PatientInfoCreateRequest(
                emergencyReport.getId(),
                PatientInfo.Gender.M,
                45,
                LocalDateTime.now(),
                PatientInfo.MentalStatus.ALERT,
                "복통",
                85,
                "120/80",
                99,
                16,
                new BigDecimal("36.5"),
                true,
                "고혈압",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(3)
        );

        patientInfoService.createPatientInfo(request, paramedic.getId());

        // when
        var response = patientInfoService.getPatientInfo(emergencyReport.getId(), paramedic.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.emergencyReportId()).isEqualTo(emergencyReport.getId());
        assertThat(response.gender()).isEqualTo(PatientInfo.Gender.M);
    }

    @Test
    @DisplayName("환자 정보 조회 - PatientInfo 없음 실패")
    void getPatientInfo_notFound() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = emergencyReport.getParamedic();

        // when & then
        assertThatThrownBy(() -> patientInfoService.getPatientInfo(emergencyReport.getId(), paramedic.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PATIENT_INFO_NOT_FOUND);
    }

    @Test
    @DisplayName("환자 정보 조회 - 권한 없음 실패")
    void getPatientInfo_accessDenied() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = emergencyReport.getParamedic();

        PatientInfoCreateRequest request = new PatientInfoCreateRequest(
                emergencyReport.getId(),
                PatientInfo.Gender.M,
                45,
                LocalDateTime.now(),
                PatientInfo.MentalStatus.ALERT,
                "복통",
                85,
                "120/80",
                99,
                16,
                new BigDecimal("36.5"),
                true,
                "고혈압",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(3)
        );

        patientInfoService.createPatientInfo(request, paramedic.getId());

        // 다른 구급대원 찾기
        Paramedic anotherParamedic = paramedicRepository.findAll().stream()
                .filter(p -> !p.getId().equals(paramedic.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 다른 구급대원 데이터가 없습니다."));

        // when & then
        assertThatThrownBy(() -> patientInfoService.getPatientInfo(emergencyReport.getId(), anotherParamedic.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("환자 정보 수정 - 성공")
    void updatePatientInfo_success() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = emergencyReport.getParamedic();

        PatientInfoCreateRequest createRequest = new PatientInfoCreateRequest(
                emergencyReport.getId(),
                PatientInfo.Gender.M,
                45,
                LocalDateTime.now(),
                PatientInfo.MentalStatus.ALERT,
                "복통",
                85,
                "120/80",
                99,
                16,
                new BigDecimal("36.5"),
                true,
                "고혈압",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(3)
        );

        patientInfoService.createPatientInfo(createRequest, paramedic.getId());

        PatientInfoCreateRequest updateRequest = new PatientInfoCreateRequest(
                emergencyReport.getId(),
                PatientInfo.Gender.M,
                46, // 나이 변경
                LocalDateTime.now(),
                PatientInfo.MentalStatus.VERBAL,
                "두통 추가",
                90, // 심박수 변경
                "130/85",
                98,
                18,
                new BigDecimal("37.0"),
                true,
                "고혈압, 당뇨",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(3)
        );

        // when
        var response = patientInfoService.updatePatientInfo(emergencyReport.getId(), updateRequest, paramedic.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.age()).isEqualTo(46);
        assertThat(response.hr()).isEqualTo(90);
        assertThat(response.mentalStatus()).isEqualTo(PatientInfo.MentalStatus.VERBAL);
    }

    @Test
    @DisplayName("환자 정보 수정 - PatientInfo 없음 실패")
    void updatePatientInfo_notFound() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = emergencyReport.getParamedic();

        PatientInfoCreateRequest updateRequest = new PatientInfoCreateRequest(
                emergencyReport.getId(),
                PatientInfo.Gender.M,
                45,
                LocalDateTime.now(),
                PatientInfo.MentalStatus.ALERT,
                "복통",
                85,
                "120/80",
                99,
                16,
                new BigDecimal("36.5"),
                true,
                "고혈압",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(3)
        );

        // when & then
        assertThatThrownBy(() -> patientInfoService.updatePatientInfo(emergencyReport.getId(), updateRequest, paramedic.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PATIENT_INFO_NOT_FOUND);
    }

    @Test
    @DisplayName("환자 정보 수정 - 권한 없음 실패")
    void updatePatientInfo_accessDenied() {
        // given
        EmergencyReport emergencyReport = createTestEmergencyReport();
        Paramedic paramedic = emergencyReport.getParamedic();

        PatientInfoCreateRequest createRequest = new PatientInfoCreateRequest(
                emergencyReport.getId(),
                PatientInfo.Gender.M,
                45,
                LocalDateTime.now(),
                PatientInfo.MentalStatus.ALERT,
                "복통",
                85,
                "120/80",
                99,
                16,
                new BigDecimal("36.5"),
                true,
                "고혈압",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(3)
        );

        patientInfoService.createPatientInfo(createRequest, paramedic.getId());

        // 다른 구급대원 찾기
        Paramedic anotherParamedic = paramedicRepository.findAll().stream()
                .filter(p -> !p.getId().equals(paramedic.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("테스트용 다른 구급대원 데이터가 없습니다."));

        PatientInfoCreateRequest updateRequest = new PatientInfoCreateRequest(
                emergencyReport.getId(),
                PatientInfo.Gender.M,
                46,
                LocalDateTime.now(),
                PatientInfo.MentalStatus.VERBAL,
                "두통",
                90,
                "130/85",
                98,
                18,
                new BigDecimal("37.0"),
                true,
                "고혈압, 당뇨",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(3)
        );

        // when & then
        assertThatThrownBy(() -> patientInfoService.updatePatientInfo(emergencyReport.getId(), updateRequest, anotherParamedic.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
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
