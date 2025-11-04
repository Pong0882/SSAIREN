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
    public PatientInfoResponse createPatientInfo(PatientInfoCreateRequest request) {
        log.info("Creating patient info for emergency report ID: {}", request.emergencyReportId());

        // 1. 구급일지 존재 확인
        EmergencyReport emergencyReport = emergencyReportRepository.findById(request.emergencyReportId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_REPORT_NOT_FOUND));

        // 2. 이미 환자 정보가 존재하는지 확인
        if (patientInfoRepository.findById(request.emergencyReportId()).isPresent()) {
            throw new CustomException(ErrorCode.PATIENT_INFO_ALREADY_EXISTS);
        }

        // 3. 환자 정보 엔티티 생성
        // @MapsId 사용 시 ID는 자동 매핑되므로 emergencyReport만 설정
        PatientInfo patientInfo = PatientInfo.builder()
                .emergencyReport(emergencyReport)
                .gender(request.gender())
                .age(request.age())
                .recordTime(request.recordTime())
                .mentalStatus(request.mentalStatus())
                .chiefComplaint(request.chiefComplaint())
                .hr(request.hr())
                .bp(request.bp())
                .spo2(request.spo2())
                .rr(request.rr())
                .bt(request.bt())
                .hasGuardian(request.hasGuardian())
                .hx(request.hx())
                .onsetTime(request.onsetTime())
                .lnt(request.lnt())
                .build();

        // 4. 저장
        PatientInfo savedPatientInfo = patientInfoRepository.save(patientInfo);

        log.info("Patient info created successfully for emergency report ID: {}", request.emergencyReportId());

        return PatientInfoResponse.from(savedPatientInfo);
    }

    /**
     * 환자 정보 조회
     */
    @Override
    public PatientInfoResponse getPatientInfo(Long emergencyReportId) {
        log.info("Fetching patient info for emergency report ID: {}", emergencyReportId);

        PatientInfo patientInfo = patientInfoRepository.findById(emergencyReportId)
                .orElseThrow(() -> new CustomException(ErrorCode.PATIENT_INFO_NOT_FOUND));

        return PatientInfoResponse.from(patientInfo);
    }

    /**
     * 환자 정보 수정
     */
    @Override
    @Transactional
    public PatientInfoResponse updatePatientInfo(Long emergencyReportId, PatientInfoCreateRequest request) {
        log.info("Updating patient info for emergency report ID: {}", emergencyReportId);

        // 1. 환자 정보 조회
        PatientInfo patientInfo = patientInfoRepository.findById(emergencyReportId)
                .orElseThrow(() -> new CustomException(ErrorCode.PATIENT_INFO_NOT_FOUND));

        // 2. 환자 정보 수정
        patientInfo.updatePatientInfo(
                request.gender(),
                request.age(),
                request.recordTime(),
                request.mentalStatus(),
                request.chiefComplaint(),
                request.hr(),
                request.bp(),
                request.spo2(),
                request.rr(),
                request.bt(),
                request.hasGuardian(),
                request.hx(),
                request.onsetTime(),
                request.lnt()
        );

        log.info("Patient info updated successfully for emergency report ID: {}", emergencyReportId);

        return PatientInfoResponse.from(patientInfo);
    }
}
