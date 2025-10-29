package com.ssairen.domain.hospital.service;

import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.repository.EmergencyReportRepository;
import com.ssairen.domain.hospital.dto.HospitalRequestMessage;
import com.ssairen.domain.hospital.dto.HospitalSelectionRequest;
import com.ssairen.domain.hospital.dto.HospitalSelectionResponse;
import com.ssairen.domain.hospital.dto.PatientInfoDto;
import com.ssairen.domain.hospital.entity.Hospital;
import com.ssairen.domain.hospital.entity.HospitalSelection;
import com.ssairen.domain.hospital.entity.PatientInfo;
import com.ssairen.domain.hospital.enums.HospitalSelectionStatus;
import com.ssairen.domain.hospital.repository.HospitalRepository;
import com.ssairen.domain.hospital.repository.HospitalSelectionRepository;
import com.ssairen.domain.hospital.repository.PatientInfoRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 병원 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalService {

    private static final String LOG_PREFIX = "[HospitalService] ";

    private final HospitalRepository hospitalRepository;
    private final HospitalSelectionRepository hospitalSelectionRepository;
    private final EmergencyReportRepository emergencyReportRepository;
    private final PatientInfoRepository patientInfoRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 병원 이송 요청 생성
     *
     * @param request 병원 이송 요청 정보
     * @return 병원 이송 요청 응답
     */
    @Transactional
    public HospitalSelectionResponse createHospitalSelectionRequest(HospitalSelectionRequest request) {
        log.info(LOG_PREFIX + "병원 이송 요청 생성 시작 - 구급일지 ID: {}, 병원 개수: {}",
                request.getEmergencyReportId(), request.getHospitalNames().size());

        // 1. 구급일지 조회
        EmergencyReport emergencyReport = emergencyReportRepository.findById(request.getEmergencyReportId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_REPORT_NOT_FOUND));

        // 2. 환자 정보 조회
        Optional<PatientInfo> patientInfoOptional = patientInfoRepository.findByEmergencyReportId_Id(request.getEmergencyReportId());
        PatientInfoDto patientInfoDto = patientInfoOptional.map(PatientInfoDto::from).orElse(null);

        if (patientInfoDto != null) {
            log.info(LOG_PREFIX + "환자 정보 조회 성공 - 환자 ID: {}, 나이: {}, 성별: {}",
                    patientInfoDto.getId(), patientInfoDto.getAge(), patientInfoDto.getGender());
        } else {
            log.warn(LOG_PREFIX + "환자 정보가 없습니다 - 구급일지 ID: {}", request.getEmergencyReportId());
        }

        // 3. 병원 목록 조회
        List<Hospital> hospitals = hospitalRepository.findByNameIn(request.getHospitalNames());

        // 3. 요청된 병원 중 찾지 못한 병원이 있는지 확인
        if (hospitals.size() != request.getHospitalNames().size()) {
            log.warn(LOG_PREFIX + "일부 병원을 찾을 수 없습니다 - 요청: {}, 찾은 개수: {}",
                    request.getHospitalNames().size(), hospitals.size());
            throw new CustomException(ErrorCode.HOSPITAL_NOT_FOUND,
                    "요청한 병원 중 일부를 찾을 수 없습니다.");
        }

        // 4. HospitalSelection 생성 및 저장
        List<HospitalSelection> selections = new ArrayList<>();
        for (Hospital hospital : hospitals) {
            HospitalSelection selection = HospitalSelection.builder()
                    .emergencyReport(emergencyReport)
                    .hospital(hospital)
                    .status(HospitalSelectionStatus.PENDING)
                    .build();
            HospitalSelection savedSelection = hospitalSelectionRepository.save(selection);
            selections.add(savedSelection);

            // 5. 각 병원에게 웹소켓으로 요청 메시지 전송 (환자 정보 포함)
            String topic = "/topic/hospital." + hospital.getId();
            HospitalRequestMessage message = HospitalRequestMessage.of(
                    savedSelection.getId(),
                    request.getEmergencyReportId(),
                    patientInfoDto
            );

            log.info(LOG_PREFIX + "웹소켓 메시지 전송 시작 - 병원 ID: {}, 토픽: {}, 환자 정보 포함: {}",
                    hospital.getId(), topic, (patientInfoDto != null));

            try {
                messagingTemplate.convertAndSend(topic, message);
                log.info(LOG_PREFIX + "✅ 웹소켓 메시지 전송 성공 - 병원 ID: {}, 토픽: {}",
                        hospital.getId(), topic);
            } catch (Exception e) {
                log.error(LOG_PREFIX + "❌ 웹소켓 메시지 전송 실패 - 병원 ID: {}, 에러: {}",
                        hospital.getId(), e.getMessage(), e);
            }
        }

        log.info(LOG_PREFIX + "병원 이송 요청 생성 완료 - 구급일지 ID: {}, 요청 병원 수: {}",
                request.getEmergencyReportId(), selections.size());

        // 6. 응답 생성
        return HospitalSelectionResponse.from(request.getEmergencyReportId(), selections);
    }
}
