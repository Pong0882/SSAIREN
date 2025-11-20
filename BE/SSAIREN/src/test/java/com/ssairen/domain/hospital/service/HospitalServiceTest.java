package com.ssairen.domain.hospital.service;

import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.repository.DispatchRepository;
import com.ssairen.domain.emergency.repository.EmergencyReportRepository;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import com.ssairen.domain.hospital.dto.*;
import com.ssairen.domain.hospital.entity.Hospital;
import com.ssairen.domain.hospital.entity.HospitalSelection;
import com.ssairen.domain.hospital.entity.PatientInfo;
import com.ssairen.domain.hospital.enums.DateRangeFilter;
import com.ssairen.domain.hospital.enums.HospitalSelectionStatus;
import com.ssairen.domain.hospital.enums.PatientFilterType;
import com.ssairen.domain.hospital.repository.HospitalRepository;
import com.ssairen.domain.hospital.repository.HospitalSelectionRepository;
import com.ssairen.domain.hospital.repository.PatientInfoRepository;
import com.ssairen.global.dto.PageResponse;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class HospitalServiceTest {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private HospitalSelectionRepository hospitalSelectionRepository;

    @Autowired
    private EmergencyReportRepository emergencyReportRepository;

    @Autowired
    private DispatchRepository dispatchRepository;

    @Autowired
    private ParamedicRepository paramedicRepository;

    @Autowired
    private FireStateRepository fireStateRepository;

    @Autowired
    private PatientInfoRepository patientInfoRepository;

    @Test
    @DisplayName("PENDING 요청 목록 조회 - 성공")
    void getPendingRequests_success() {
        // given
        Hospital hospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();

        createTestHospitalSelection(hospital, emergencyReport, HospitalSelectionStatus.PENDING);

        // when
        List<HospitalRequestMessage> requests = hospitalService.getPendingRequests(
                hospital.getId(), hospital.getId());

        // then
        assertThat(requests).isNotEmpty();
        assertThat(requests.get(0).getEmergencyReportId()).isEqualTo(emergencyReport.getId());
    }

    @Test
    @DisplayName("PENDING 요청 목록 조회 - 권한 없음")
    void getPendingRequests_accessDenied() {
        // given
        Hospital hospital = createTestHospital();
        Hospital anotherHospital = createTestHospital();

        // when & then
        assertThatThrownBy(() -> hospitalService.getPendingRequests(hospital.getId(), anotherHospital.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("PENDING 요청 목록 조회 - 병원 없음")
    void getPendingRequests_hospitalNotFound() {
        // when & then
        assertThatThrownBy(() -> hospitalService.getPendingRequests(99999, 99999))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HOSPITAL_NOT_FOUND);
    }

    @Test
    @DisplayName("수용한 환자 목록 조회 - 성공")
    void getAcceptedPatients_success() {
        // given
        Hospital hospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();
        createTestPatientInfo(emergencyReport);

        createTestHospitalSelection(hospital, emergencyReport, HospitalSelectionStatus.ACCEPTED);

        // when
        List<AcceptedPatientDto> patients = hospitalService.getAcceptedPatients(
                hospital.getId(), hospital.getId());

        // then
        assertThat(patients).isNotEmpty();
        assertThat(patients.get(0).getEmergencyReportId()).isEqualTo(emergencyReport.getId());
    }

    @Test
    @DisplayName("수용한 환자 목록 조회 - 권한 없음")
    void getAcceptedPatients_accessDenied() {
        // given
        Hospital hospital = createTestHospital();
        Hospital anotherHospital = createTestHospital();

        // when & then
        assertThatThrownBy(() -> hospitalService.getAcceptedPatients(hospital.getId(), anotherHospital.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("수용한 환자 목록 조회 - 병원 없음")
    void getAcceptedPatients_hospitalNotFound() {
        // when & then
        assertThatThrownBy(() -> hospitalService.getAcceptedPatients(99999, 99999))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HOSPITAL_NOT_FOUND);
    }

    @Test
    @DisplayName("수용한 환자 목록 조회 (페이지네이션) - 성공")
    void getAcceptedPatientsWithPagination_success() {
        // given
        Hospital hospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();
        createTestPatientInfo(emergencyReport);

        createTestHospitalSelection(hospital, emergencyReport, HospitalSelectionStatus.ACCEPTED);

        // when
        PageResponse<AcceptedPatientDto> response = hospitalService.getAcceptedPatientsWithPagination(
                hospital.getId(), hospital.getId(), 0, 10, PatientFilterType.ALL, DateRangeFilter.ALL);

        // then
        assertThat(response.getContent()).isNotEmpty();
        assertThat(response.getTotalElements()).isGreaterThan(0);
    }

    @Test
    @DisplayName("수용한 환자 목록 조회 (페이지네이션) - 권한 없음")
    void getAcceptedPatientsWithPagination_accessDenied() {
        // given
        Hospital hospital = createTestHospital();
        Hospital anotherHospital = createTestHospital();

        // when & then
        assertThatThrownBy(() -> hospitalService.getAcceptedPatientsWithPagination(
                hospital.getId(), anotherHospital.getId(), 0, 10, PatientFilterType.ALL, DateRangeFilter.ALL))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("수용한 환자 목록 조회 (페이지네이션) - 병원 없음")
    void getAcceptedPatientsWithPagination_hospitalNotFound() {
        // when & then
        assertThatThrownBy(() -> hospitalService.getAcceptedPatientsWithPagination(
                99999, 99999, 0, 10, PatientFilterType.ALL, DateRangeFilter.ALL))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HOSPITAL_NOT_FOUND);
    }

    @Test
    @DisplayName("환자 상세 정보 조회 - 성공")
    void getPatientDetail_success() {
        // given
        Hospital hospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();
        createTestPatientInfo(emergencyReport);

        createTestHospitalSelection(hospital, emergencyReport, HospitalSelectionStatus.ACCEPTED);

        // when
        PatientInfoDto patientInfo = hospitalService.getPatientDetail(
                hospital.getId(), emergencyReport.getId(), hospital.getId());

        // then
        assertThat(patientInfo).isNotNull();
        assertThat(patientInfo.getEmergencyReportId()).isEqualTo(emergencyReport.getId());
    }

    @Test
    @DisplayName("환자 상세 정보 조회 - 권한 없음 (다른 병원)")
    void getPatientDetail_accessDenied_differentHospital() {
        // given
        Hospital hospital = createTestHospital();
        Hospital anotherHospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();
        createTestPatientInfo(emergencyReport);

        createTestHospitalSelection(hospital, emergencyReport, HospitalSelectionStatus.ACCEPTED);

        // when & then
        assertThatThrownBy(() -> hospitalService.getPatientDetail(
                hospital.getId(), emergencyReport.getId(), anotherHospital.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("환자 상세 정보 조회 - 권한 없음 (수용하지 않은 환자)")
    void getPatientDetail_accessDenied_notAccepted() {
        // given
        Hospital hospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();
        createTestPatientInfo(emergencyReport);

        // when & then
        assertThatThrownBy(() -> hospitalService.getPatientDetail(
                hospital.getId(), emergencyReport.getId(), hospital.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("환자 정보 수정 - 성공")
    void updatePatientInfo_success() {
        // given
        Hospital hospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();
        createTestPatientInfo(emergencyReport);

        createTestHospitalSelection(hospital, emergencyReport, HospitalSelectionStatus.ACCEPTED);

        UpdatePatientInfoRequest request = createUpdateRequest();

        // when
        PatientInfoDto updated = hospitalService.updatePatientInfo(
                hospital.getId(), emergencyReport.getId(), request, hospital.getId());

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getAge()).isEqualTo(50);
    }

    @Test
    @DisplayName("환자 정보 수정 - 권한 없음 (다른 병원)")
    void updatePatientInfo_accessDenied_differentHospital() {
        // given
        Hospital hospital = createTestHospital();
        Hospital anotherHospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();
        createTestPatientInfo(emergencyReport);

        createTestHospitalSelection(hospital, emergencyReport, HospitalSelectionStatus.ACCEPTED);

        UpdatePatientInfoRequest request = createUpdateRequest();

        // when & then
        assertThatThrownBy(() -> hospitalService.updatePatientInfo(
                hospital.getId(), emergencyReport.getId(), request, anotherHospital.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("환자 정보 수정 - 권한 없음 (수용하지 않은 환자)")
    void updatePatientInfo_accessDenied_notAccepted() {
        // given
        Hospital hospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();
        createTestPatientInfo(emergencyReport);

        UpdatePatientInfoRequest request = createUpdateRequest();

        // when & then
        assertThatThrownBy(() -> hospitalService.updatePatientInfo(
                hospital.getId(), emergencyReport.getId(), request, hospital.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("환자 내원 완료 처리 - 성공")
    void markPatientAsArrived_success() {
        // given
        Hospital hospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();

        createTestHospitalSelection(hospital, emergencyReport, HospitalSelectionStatus.ACCEPTED);

        // when
        hospitalService.markPatientAsArrived(hospital.getId(), emergencyReport.getId(), hospital.getId());

        // then
        HospitalSelection selection = hospitalSelectionRepository
                .findByHospitalIdAndEmergencyReportIdAndStatus(
                        hospital.getId(), emergencyReport.getId(), HospitalSelectionStatus.ARRIVED)
                .orElseThrow();

        assertThat(selection.getStatus()).isEqualTo(HospitalSelectionStatus.ARRIVED);
    }

    @Test
    @DisplayName("환자 내원 완료 처리 - 권한 없음")
    void markPatientAsArrived_accessDenied() {
        // given
        Hospital hospital = createTestHospital();
        Hospital anotherHospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();

        createTestHospitalSelection(hospital, emergencyReport, HospitalSelectionStatus.ACCEPTED);

        // when & then
        assertThatThrownBy(() -> hospitalService.markPatientAsArrived(
                hospital.getId(), emergencyReport.getId(), anotherHospital.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("환자 내원 완료 처리 - HospitalSelection 없음")
    void markPatientAsArrived_selectionNotFound() {
        // given
        Hospital hospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();

        // when & then
        assertThatThrownBy(() -> hospitalService.markPatientAsArrived(
                hospital.getId(), emergencyReport.getId(), hospital.getId()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HOSPITAL_SELECTION_NOT_FOUND);
    }

    @Test
    @DisplayName("병원 선택 상태 조회 - 성공")
    void getHospitalSelectionStatus_success() {
        // given
        Hospital hospital = createTestHospital();
        EmergencyReport emergencyReport = createTestEmergencyReport();

        createTestHospitalSelection(hospital, emergencyReport, HospitalSelectionStatus.PENDING);

        // when
        HospitalSelectionStatusResponse response = hospitalService.getHospitalSelectionStatus(emergencyReport.getId());

        // then
        assertThat(response.getEmergencyReportId()).isEqualTo(emergencyReport.getId());
        assertThat(response.getHospitals()).isNotEmpty();
    }

    @Test
    @DisplayName("병원 선택 상태 조회 - EmergencyReport 없음")
    void getHospitalSelectionStatus_emergencyReportNotFound() {
        // when & then
        assertThatThrownBy(() -> hospitalService.getHospitalSelectionStatus(99999L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMERGENCY_REPORT_NOT_FOUND);
    }

    // === Helper Methods ===

    private Hospital createTestHospital() {
        Hospital hospital = Hospital.builder()
                .name("테스트병원" + System.currentTimeMillis())
                .officialName("테스트의료원")
                .password("password")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .phoneNumber("02-1234-5678")
                .address("서울시 중구 테스트로 1")
                .build();
        return hospitalRepository.save(hospital);
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

    private HospitalSelection createTestHospitalSelection(Hospital hospital, EmergencyReport emergencyReport, HospitalSelectionStatus status) {
        HospitalSelection selection = HospitalSelection.builder()
                .hospital(hospital)
                .emergencyReport(emergencyReport)
                .status(status)
                .build();

        if (status != HospitalSelectionStatus.PENDING) {
            selection.respond(status);
        }

        return hospitalSelectionRepository.save(selection);
    }

    private PatientInfo createTestPatientInfo(EmergencyReport emergencyReport) {
        PatientInfo patientInfo = PatientInfo.builder()
                .emergencyReport(emergencyReport)
                .gender(PatientInfo.Gender.M)
                .age(45)
                .recordTime(LocalDateTime.now())
                .mentalStatus(PatientInfo.MentalStatus.ALERT)
                .chiefComplaint("복통")
                .hr(85)
                .bp("120/80")
                .spo2(99)
                .rr(16)
                .bt(new BigDecimal("36.5"))
                .hasGuardian(true)
                .hx("고혈압")
                .onsetTime(LocalDateTime.now().minusHours(2))
                .lnt(LocalDateTime.now().minusHours(3))
                .build();

        return patientInfoRepository.save(patientInfo);
    }

    private UpdatePatientInfoRequest createUpdateRequest() {
        return UpdatePatientInfoRequest.builder()
                .gender("M")
                .age(50)
                .recordTime(LocalDateTime.now())
                .mentalStatus("ALERT")
                .chiefComplaint("복통, 구토")
                .hr(90)
                .bp("130/85")
                .spo2(98)
                .rr(18)
                .bt(new BigDecimal("37.0"))
                .hasGuardian(true)
                .hx("고혈압, 당뇨")
                .onsetTime(LocalDateTime.now().minusHours(2))
                .lnt(LocalDateTime.now().minusHours(3))
                .build();
    }
}
