package com.ssairen.domain.hospital.service;

import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.repository.EmergencyReportRepository;
import com.ssairen.domain.hospital.dto.PatientInfoCreateRequest;
import com.ssairen.domain.hospital.dto.PatientInfoResponse;
import com.ssairen.domain.hospital.entity.PatientInfo;
import com.ssairen.domain.hospital.repository.PatientInfoRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 환자 정보 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientInfoServiceImpl implements PatientInfoService {

    private final PatientInfoRepository patientInfoRepository;
    private final EmergencyReportRepository emergencyReportRepository;

    /**
     * 환자 정보 생성
     */
    @Override
    @Transactional
    public PatientInfoResponse createPatientInfo(PatientInfoCreateRequest request, Integer paramedicId) {
        log.info("Creating patient info for emergency report ID: {} by paramedic ID: {}",
                request.emergencyReportId(), paramedicId);

        // 1. 구급일지 존재 확인
        EmergencyReport emergencyReport = emergencyReportRepository.findById(request.emergencyReportId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_REPORT_NOT_FOUND));

        // 2. 권한 검증: 해당 구급일지를 작성한 구급대원인지 확인
        if (!emergencyReport.getParamedic().getId().equals(paramedicId)) {
            log.warn("Unauthorized patient info creation attempt - Emergency Report ID: {}, Paramedic ID: {}, Report Owner: {}",
                    request.emergencyReportId(), paramedicId, emergencyReport.getParamedic().getId());
            throw new CustomException(ErrorCode.ACCESS_DENIED, "본인이 작성한 구급일지에 대해서만 환자 정보를 생성할 수 있습니다.");
        }

        // 3. 이미 환자 정보가 존재하는지 확인
        if (patientInfoRepository.findById(request.emergencyReportId()).isPresent()) {
            throw new CustomException(ErrorCode.PATIENT_INFO_ALREADY_EXISTS);
        }

        // 3. 환자 정보 엔티티 생성 (null일 경우 기본값 사용)
        // 기본값: 여자 28살, 체온 36.5도, 혈압 120/80, 호흡수 16, 맥박 85, 산소포화도 99
        // @MapsId 사용 시 ID는 자동 매핑되므로 emergencyReport만 설정
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        PatientInfo patientInfo = PatientInfo.builder()
                .emergencyReport(emergencyReport)
                .gender(request.gender() != null ? request.gender() : PatientInfo.Gender.F)
                .age(request.age() != null ? request.age() : 28)
                .recordTime(request.recordTime() != null ? request.recordTime() : now)
                .mentalStatus(request.mentalStatus() != null ? request.mentalStatus() : PatientInfo.MentalStatus.ALERT)
                .chiefComplaint(request.chiefComplaint())
                .hr(request.hr() != null ? request.hr() : 85)
                .bp(request.bp() != null ? request.bp() : "120/80")
                .spo2(request.spo2() != null ? request.spo2() : 99)
                .rr(request.rr() != null ? request.rr() : 16)
                .bt(request.bt() != null ? request.bt() : new java.math.BigDecimal("36.5"))
                .hasGuardian(request.hasGuardian() != null ? request.hasGuardian() : false)
                .hx(request.hx())
                .onsetTime(request.onsetTime() != null ? request.onsetTime() : now)
                .lnt(request.lnt() != null ? request.lnt() : now)
                .build();

        log.debug("Patient info created with defaults - gender: {}, age: {}, mentalStatus: {}, hr: {}, bp: {}, spo2: {}, rr: {}, bt: {}, hasGuardian: {}, onsetTime: {}, lnt: {}",
                patientInfo.getGender(), patientInfo.getAge(), patientInfo.getMentalStatus(),
                patientInfo.getHr(), patientInfo.getBp(), patientInfo.getSpo2(), patientInfo.getRr(), patientInfo.getBt(),
                patientInfo.getHasGuardian(), patientInfo.getOnsetTime(), patientInfo.getLnt());

        // 4. 저장
        PatientInfo savedPatientInfo = patientInfoRepository.save(patientInfo);

        log.info("Patient info created successfully for emergency report ID: {}", request.emergencyReportId());

        return PatientInfoResponse.from(savedPatientInfo);
    }

    /**
     * 환자 정보 조회
     */
    @Override
    public PatientInfoResponse getPatientInfo(Long emergencyReportId, Integer paramedicId) {
        log.info("Fetching patient info for emergency report ID: {} by paramedic ID: {}",
                emergencyReportId, paramedicId);

        // 1. 환자 정보 조회
        PatientInfo patientInfo = patientInfoRepository.findById(emergencyReportId)
                .orElseThrow(() -> new CustomException(ErrorCode.PATIENT_INFO_NOT_FOUND));

        // 2. 권한 검증: 해당 구급일지를 작성한 구급대원인지 확인
        if (!patientInfo.getEmergencyReport().getParamedic().getId().equals(paramedicId)) {
            log.warn("Unauthorized patient info access attempt - Emergency Report ID: {}, Paramedic ID: {}, Report Owner: {}",
                    emergencyReportId, paramedicId, patientInfo.getEmergencyReport().getParamedic().getId());
            throw new CustomException(ErrorCode.ACCESS_DENIED, "본인이 작성한 구급일지의 환자 정보만 조회할 수 있습니다.");
        }

        return PatientInfoResponse.from(patientInfo);
    }

    /**
     * 환자 정보 수정
     */
    @Override
    @Transactional
    public PatientInfoResponse updatePatientInfo(Long emergencyReportId, PatientInfoCreateRequest request, Integer paramedicId) {
        log.info("Updating patient info for emergency report ID: {} by paramedic ID: {}",
                emergencyReportId, paramedicId);

        // 1. 환자 정보 조회
        PatientInfo patientInfo = patientInfoRepository.findById(emergencyReportId)
                .orElseThrow(() -> new CustomException(ErrorCode.PATIENT_INFO_NOT_FOUND));

        // 2. 권한 검증: 해당 구급일지를 작성한 구급대원인지 확인
        if (!patientInfo.getEmergencyReport().getParamedic().getId().equals(paramedicId)) {
            log.warn("Unauthorized patient info update attempt - Emergency Report ID: {}, Paramedic ID: {}, Report Owner: {}",
                    emergencyReportId, paramedicId, patientInfo.getEmergencyReport().getParamedic().getId());
            throw new CustomException(ErrorCode.ACCESS_DENIED, "본인이 작성한 구급일지의 환자 정보만 수정할 수 있습니다.");
        }

        // 2. 환자 정보 수정 (null일 경우 기존 값 유지 또는 기본값 사용)
        patientInfo.updatePatientInfo(
                request.gender() != null ? request.gender() : patientInfo.getGender(),
                request.age() != null ? request.age() : patientInfo.getAge(),
                request.recordTime() != null ? request.recordTime() : patientInfo.getRecordTime(),
                request.mentalStatus() != null ? request.mentalStatus() : patientInfo.getMentalStatus(),
                request.chiefComplaint(),
                request.hr() != null ? request.hr() : patientInfo.getHr(),
                request.bp() != null ? request.bp() : patientInfo.getBp(),
                request.spo2() != null ? request.spo2() : patientInfo.getSpo2(),
                request.rr() != null ? request.rr() : patientInfo.getRr(),
                request.bt(),
                request.hasGuardian() != null ? request.hasGuardian() : patientInfo.getHasGuardian(),
                request.hx(),
                request.onsetTime(),
                request.lnt()
        );

        log.info("Patient info updated successfully for emergency report ID: {}", emergencyReportId);

        return PatientInfoResponse.from(patientInfo);
    }
}
