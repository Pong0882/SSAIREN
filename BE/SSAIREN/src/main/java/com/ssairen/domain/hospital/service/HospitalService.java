package com.ssairen.domain.hospital.service;

import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.repository.EmergencyReportRepository;
import com.ssairen.domain.hospital.dto.*;
import com.ssairen.domain.hospital.entity.Hospital;
import com.ssairen.domain.hospital.entity.HospitalSelection;
import com.ssairen.domain.hospital.entity.PatientInfo;
import com.ssairen.domain.hospital.enums.HospitalSelectionStatus;
import com.ssairen.domain.hospital.enums.PatientFilterType;
import com.ssairen.domain.hospital.repository.HospitalRepository;
import com.ssairen.domain.hospital.repository.HospitalSelectionRepository;
import com.ssairen.domain.hospital.repository.PatientInfoRepository;
import com.ssairen.global.dto.PageResponse;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ai.recommendation.api.url:http://localhost:8000/api/emergency/recommend}")
    private String aiRecommendationApiUrl;

    /**
     * 병원 이송 요청 생성
     *
     * @param request 병원 이송 요청 정보
     * @return 병원 이송 요청 응답
     */
    @Transactional
    public HospitalSelectionResponse createHospitalSelectionRequest(HospitalSelectionRequest request) {
        log.info(LOG_PREFIX + "병원 이송 요청 생성 시작 - 구급일지 ID: {}, 위도: {}, 경도: {}, 반경: {}",
                request.getEmergencyReportId(), request.getLatitude(), request.getLongitude(), request.getRadius());

        // 1. 구급일지 조회
        EmergencyReport emergencyReport = emergencyReportRepository.findById(request.getEmergencyReportId())
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_REPORT_NOT_FOUND));

        // 2. 환자 정보 조회
        Optional<PatientInfo> patientInfoOptional = patientInfoRepository.findById(request.getEmergencyReportId());
        PatientInfoDto patientInfoDto = patientInfoOptional.map(PatientInfoDto::from).orElse(null);

        if (patientInfoDto != null) {
            log.info(LOG_PREFIX + "환자 정보 조회 성공 - 구급일지 ID: {}, 나이: {}, 성별: {}",
                    patientInfoDto.getEmergencyReportId(), patientInfoDto.getAge(), patientInfoDto.getGender());
        } else {
            log.warn(LOG_PREFIX + "환자 정보가 없습니다 - 구급일지 ID: {}", request.getEmergencyReportId());
        }

        // 3. 환자 정보를 JSON 문자열로 변환
        String patientConditionJson;
        try {
            patientConditionJson = objectMapper.writeValueAsString(patientInfoDto);
            log.info(LOG_PREFIX + "환자 정보 JSON 변환 완료 - 구급일지 ID: {}", request.getEmergencyReportId());
        } catch (Exception e) {
            log.error(LOG_PREFIX + "환자 정보 JSON 변환 실패 - 구급일지 ID: {}, 에러: {}",
                    request.getEmergencyReportId(), e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "환자 정보를 JSON으로 변환하는 데 실패했습니다.");
        }

        // 4. AI API 호출
        AiRecommendationRequest aiRequest = AiRecommendationRequest.builder()
                .patientCondition(patientConditionJson)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .radius(request.getRadius())
                .build();

        AiRecommendationResponse aiResponse;
        try {
            log.info(LOG_PREFIX + "AI API 호출 시작 - URL: {}", aiRecommendationApiUrl);
            aiResponse = restTemplate.postForObject(aiRecommendationApiUrl, aiRequest, AiRecommendationResponse.class);

            if (aiResponse == null || !aiResponse.getSuccess()) {
                log.error(LOG_PREFIX + "AI API 호출 실패 - 응답이 null이거나 success가 false입니다.");
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "AI 추천 API 호출에 실패했습니다.");
            }

            log.info(LOG_PREFIX + "AI API 호출 성공 - 추천 병원 수: {}, 전체 병원 수: {}",
                    aiResponse.getRecommendedHospitals().size(), aiResponse.getTotalHospitalsFound());

            // AI 추론 정보 로그 출력
            if (aiResponse.getGptReasoning() != null) {
                log.info(LOG_PREFIX + "AI 추론 정보: {}", aiResponse.getGptReasoning());
            }
            if (aiResponse.getReasoningTime() != null) {
                log.info(LOG_PREFIX + "AI 추론 시간: {}초", aiResponse.getReasoningTime());
            }
            if (aiResponse.getHospitalsDetail() != null) {
                log.info(LOG_PREFIX + "병원 상세 정보: {}", aiResponse.getHospitalsDetail());
            }
        } catch (Exception e) {
            log.error(LOG_PREFIX + "AI API 호출 중 예외 발생 - 에러: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR, "AI 추천 API 호출 중 오류가 발생했습니다: " + e.getMessage());
        }

        // 5. recommended_hospitals로 병원 조회
        List<String> recommendedHospitalNames = aiResponse.getRecommendedHospitals();
        if (recommendedHospitalNames == null || recommendedHospitalNames.isEmpty()) {
            log.warn(LOG_PREFIX + "추천된 병원이 없습니다 - 구급일지 ID: {}", request.getEmergencyReportId());
            throw new CustomException(ErrorCode.HOSPITAL_NOT_FOUND, "추천된 병원이 없습니다.");
        }

        List<Hospital> hospitals = hospitalRepository.findByNameIn(recommendedHospitalNames);

        // 6. 요청된 병원 중 찾지 못한 병원이 있는지 확인
        if (hospitals.size() != recommendedHospitalNames.size()) {
            log.warn(LOG_PREFIX + "일부 추천 병원을 찾을 수 없습니다 - 추천: {}, 찾은 개수: {}",
                    recommendedHospitalNames.size(), hospitals.size());
            // 찾은 병원만 사용하도록 계속 진행
        }

        // 7. HospitalSelection 생성 및 저장
        List<HospitalSelection> selections = new ArrayList<>();
        for (Hospital hospital : hospitals) {
            HospitalSelection selection = HospitalSelection.builder()
                    .emergencyReport(emergencyReport)
                    .hospital(hospital)
                    .status(HospitalSelectionStatus.PENDING)
                    .build();
            HospitalSelection savedSelection = hospitalSelectionRepository.save(selection);
            selections.add(savedSelection);

            // 8. 각 병원에게 웹소켓으로 요청 메시지 전송 (환자 정보 포함)
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
                log.info(LOG_PREFIX + "웹소켓 메시지 전송 성공 - 병원 ID: {}, 병원명: {}, 토픽: {}",
                        hospital.getId(), hospital.getName(), topic);
            } catch (Exception e) {
                log.error(LOG_PREFIX + "웹소켓 메시지 전송 실패 - 병원 ID: {}, 에러: {}",
                        hospital.getId(), e.getMessage(), e);
            }
        }

        log.info(LOG_PREFIX + "병원 이송 요청 생성 완료 - 구급일지 ID: {}, 요청 병원 수: {}",
                request.getEmergencyReportId(), selections.size());

        // 9. 응답 생성
        return HospitalSelectionResponse.from(request.getEmergencyReportId(), selections);
    }

    /**
     * 병원 이송 요청에 응답
     *
     * @param hospitalSelectionId 병원 선택 ID
     * @param request 병원 응답 요청 정보 (status)
     * @param currentHospitalId 현재 로그인한 병원 ID
     * @return 병원 응답 결과
     */
    @Transactional
    public HospitalResponseDto respondToRequest(
            Integer hospitalSelectionId,
            HospitalResponseRequest request,
            Integer currentHospitalId
    ) {
        log.info(LOG_PREFIX + "병원 응답 처리 시작 - 선택 ID: {}, 상태: {}, 병원 ID: {}",
                hospitalSelectionId, request.getStatus(), currentHospitalId);

        // 1. HospitalSelection 조회 (Hospital, EmergencyReport Fetch Join)
        HospitalSelection selection = hospitalSelectionRepository
                .findByIdWithHospitalAndEmergencyReport(hospitalSelectionId)
                .orElseThrow(() -> new CustomException(ErrorCode.HOSPITAL_SELECTION_NOT_FOUND));

        // 2. 권한 검증: 현재 로그인한 병원이 해당 요청의 대상 병원인지 확인
        if (!selection.getHospital().getId().equals(currentHospitalId)) {
            log.warn(LOG_PREFIX + "권한 없는 응답 시도 - 요청 병원 ID: {}, 현재 병원 ID: {}",
                    selection.getHospital().getId(), currentHospitalId);
            throw new CustomException(ErrorCode.UNAUTHORIZED_HOSPITAL_RESPONSE);
        }

        // 3. 이미 처리된 요청인지 확인
        if (selection.getStatus() != HospitalSelectionStatus.PENDING) {
            log.warn(LOG_PREFIX + "이미 처리된 요청 - 선택 ID: {}, 현재 상태: {}",
                    hospitalSelectionId, selection.getStatus());
            throw new CustomException(ErrorCode.HOSPITAL_SELECTION_ALREADY_PROCESSED);
        }

        // 4. 상태 변경 및 응답 시간 설정
        selection.respond(request.getStatus());

        log.info(LOG_PREFIX + "상태 변경 완료 - 선택 ID: {}, 새로운 상태: {}",
                hospitalSelectionId, request.getStatus());

        // 5. ACCEPTED 상태인 경우, 같은 EmergencyReport의 다른 HospitalSelection들을 COMPLETED로 변경
        if (request.getStatus() == HospitalSelectionStatus.ACCEPTED) {
            Long emergencyReportId = selection.getEmergencyReport().getId();
            int updatedCount = hospitalSelectionRepository.updateOtherSelectionsToCompleted(
                    emergencyReportId,
                    hospitalSelectionId,
                    HospitalSelectionStatus.COMPLETED,
                    LocalDateTime.now()
            );

            log.info(LOG_PREFIX + "다른 병원 요청 완료 처리 - 구급일지 ID: {}, 완료 처리된 요청 수: {}",
                    emergencyReportId, updatedCount);
        }

        // 6. 저장
        HospitalSelection savedSelection = hospitalSelectionRepository.save(selection);

        log.info(LOG_PREFIX + "병원 응답 처리 완료 - 선택 ID: {}, 상태: {}, 응답 시간: {}",
                savedSelection.getId(), savedSelection.getStatus(), savedSelection.getResponseAt());

        // 7. 응답 생성
        return HospitalResponseDto.from(savedSelection);
    }

    /**
     * 병원의 PENDING 상태인 요청 목록 조회
     *
     * @param hospitalId 병원 ID
     * @param currentHospitalId 현재 로그인한 병원 ID
     * @return PENDING 상태인 요청 목록
     */
    @Transactional(readOnly = true)
    public List<HospitalRequestMessage> getPendingRequests(Integer hospitalId, Integer currentHospitalId) {
        log.info(LOG_PREFIX + "PENDING 요청 목록 조회 시작 - 병원 ID: {}", hospitalId);

        // 1. 권한 검증: 본인의 요청만 조회 가능
        if (!hospitalId.equals(currentHospitalId)) {
            log.warn(LOG_PREFIX + "권한 없는 요청 목록 조회 시도 - 요청 병원 ID: {}, 현재 병원 ID: {}",
                    hospitalId, currentHospitalId);
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 병원 존재 여부 확인
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new CustomException(ErrorCode.HOSPITAL_NOT_FOUND);
        }

        // 3. PENDING 상태인 HospitalSelection 목록 조회
        List<HospitalSelection> pendingSelections = hospitalSelectionRepository
                .findByHospitalIdAndStatus(hospitalId, HospitalSelectionStatus.PENDING);

        log.info(LOG_PREFIX + "PENDING 요청 조회 완료 - 병원 ID: {}, 요청 수: {}",
                hospitalId, pendingSelections.size());

        // 4. 각 selection에 대해 환자 정보 조회 및 DTO 변환
        List<HospitalRequestMessage> requestMessages = new ArrayList<>();
        for (HospitalSelection selection : pendingSelections) {
            Long emergencyReportId = selection.getEmergencyReport().getId();

            // 환자 정보 조회
            Optional<PatientInfo> patientInfoOptional = patientInfoRepository
                    .findById(emergencyReportId);

            PatientInfoDto patientInfoDto = patientInfoOptional
                    .map(PatientInfoDto::from)
                    .orElse(null);

            // HospitalRequestMessage 생성
            HospitalRequestMessage message = HospitalRequestMessage.of(
                    selection.getId(),
                    emergencyReportId,
                    patientInfoDto
            );

            requestMessages.add(message);

            log.debug(LOG_PREFIX + "요청 메시지 생성 - 선택 ID: {}, 구급일지 ID: {}, 환자 정보 포함: {}",
                    selection.getId(), emergencyReportId, (patientInfoDto != null));
        }

        log.info(LOG_PREFIX + "PENDING 요청 목록 조회 완료 - 병원 ID: {}, 반환 개수: {}",
                hospitalId, requestMessages.size());

        return requestMessages;
    }

    /**
     * 병원이 수용한 환자 목록 조회 (ACCEPTED, ARRIVED 상태)
     *
     * @param hospitalId 병원 ID
     * @param currentHospitalId 현재 로그인한 병원 ID
     * @return 수용한 환자 목록
     * @deprecated 페이지네이션 버전인 getAcceptedPatientsWithPagination 사용 권장
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<AcceptedPatientDto> getAcceptedPatients(Integer hospitalId, Integer currentHospitalId) {
        log.info(LOG_PREFIX + "수용한 환자 목록 조회 시작 - 병원 ID: {}", hospitalId);

        // 1. 권한 검증: 본인의 환자만 조회 가능
        if (!hospitalId.equals(currentHospitalId)) {
            log.warn(LOG_PREFIX + "권한 없는 환자 목록 조회 시도 - 요청 병원 ID: {}, 현재 병원 ID: {}",
                    hospitalId, currentHospitalId);
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 병원 존재 여부 확인
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new CustomException(ErrorCode.HOSPITAL_NOT_FOUND);
        }

        // 3. ACCEPTED, ARRIVED 상태인 HospitalSelection 목록 조회
        List<HospitalSelection> acceptedSelections = hospitalSelectionRepository
                .findAcceptedPatientsByHospitalId(hospitalId);

        log.info(LOG_PREFIX + "수용한 환자 조회 완료 - 병원 ID: {}, 환자 수: {}",
                hospitalId, acceptedSelections.size());

        // 4. 각 selection에 대해 환자 정보 조회 및 DTO 변환
        List<AcceptedPatientDto> acceptedPatients = new ArrayList<>();
        for (HospitalSelection selection : acceptedSelections) {
            Long emergencyReportId = selection.getEmergencyReport().getId();

            // 환자 정보 조회
            Optional<PatientInfo> patientInfoOptional = patientInfoRepository
                    .findById(emergencyReportId);

            if (patientInfoOptional.isPresent()) {
                PatientInfo patientInfo = patientInfoOptional.get();
                AcceptedPatientDto dto = AcceptedPatientDto.from(selection, patientInfo);
                acceptedPatients.add(dto);

                log.debug(LOG_PREFIX + "환자 정보 추가 - 선택 ID: {}, 구급일지 ID: {}, 상태: {}",
                        selection.getId(), emergencyReportId, selection.getStatus());
            } else {
                log.warn(LOG_PREFIX + "환자 정보 없음 - 선택 ID: {}, 구급일지 ID: {}",
                        selection.getId(), emergencyReportId);
            }
        }

        log.info(LOG_PREFIX + "수용한 환자 목록 조회 완료 - 병원 ID: {}, 반환 개수: {}",
                hospitalId, acceptedPatients.size());

        return acceptedPatients;
    }

    /**
     * 병원이 수용한 환자 목록 조회 (페이지네이션 + 필터)
     *
     * @param hospitalId 병원 ID
     * @param currentHospitalId 현재 로그인한 병원 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 데이터 개수
     * @param filterType 필터 타입 (ALL: ACCEPTED+ARRIVED, ACCEPTED: ACCEPTED만)
     * @return 페이지네이션된 환자 목록
     */
    @Transactional(readOnly = true)
    public PageResponse<AcceptedPatientDto> getAcceptedPatientsWithPagination(
            Integer hospitalId,
            Integer currentHospitalId,
            int page,
            int size,
            PatientFilterType filterType
    ) {
        log.info(LOG_PREFIX + "수용한 환자 목록 조회 시작 (페이지네이션) - 병원 ID: {}, page: {}, size: {}, filter: {}",
                hospitalId, page, size, filterType);

        // 1. 권한 검증: 본인의 환자만 조회 가능
        if (!hospitalId.equals(currentHospitalId)) {
            log.warn(LOG_PREFIX + "권한 없는 환자 목록 조회 시도 - 요청 병원 ID: {}, 현재 병원 ID: {}",
                    hospitalId, currentHospitalId);
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 병원 존재 여부 확인
        if (!hospitalRepository.existsById(hospitalId)) {
            throw new CustomException(ErrorCode.HOSPITAL_NOT_FOUND);
        }

        // 3. 필터에 따른 상태 목록 가져오기
        List<HospitalSelectionStatus> statuses = filterType.getStatuses();

        // 4. 전체 데이터 개수 조회
        long totalElements = hospitalSelectionRepository
                .countPatientsByHospitalIdAndStatuses(hospitalId, statuses);

        log.info(LOG_PREFIX + "전체 환자 수 - 병원 ID: {}, 개수: {}", hospitalId, totalElements);

        // 5. 페이지네이션된 데이터 조회
        int offset = page * size;
        List<HospitalSelection> selections = hospitalSelectionRepository
                .findPatientsByHospitalIdWithPagination(hospitalId, statuses, offset, size);

        log.info(LOG_PREFIX + "페이지네이션 데이터 조회 완료 - 병원 ID: {}, 조회 개수: {}",
                hospitalId, selections.size());

        // 6. 각 selection에 대해 환자 정보 조회 및 DTO 변환
        List<AcceptedPatientDto> acceptedPatients = new ArrayList<>();
        for (HospitalSelection selection : selections) {
            Long emergencyReportId = selection.getEmergencyReport().getId();

            // 환자 정보 조회
            Optional<PatientInfo> patientInfoOptional = patientInfoRepository
                    .findById(emergencyReportId);

            if (patientInfoOptional.isPresent()) {
                PatientInfo patientInfo = patientInfoOptional.get();
                AcceptedPatientDto dto = AcceptedPatientDto.from(selection, patientInfo);
                acceptedPatients.add(dto);

                log.debug(LOG_PREFIX + "환자 정보 추가 - 선택 ID: {}, 구급일지 ID: {}, 상태: {}",
                        selection.getId(), emergencyReportId, selection.getStatus());
            } else {
                log.warn(LOG_PREFIX + "환자 정보 없음 - 선택 ID: {}, 구급일지 ID: {}",
                        selection.getId(), emergencyReportId);
            }
        }

        log.info(LOG_PREFIX + "수용한 환자 목록 조회 완료 (페이지네이션) - 병원 ID: {}, 반환 개수: {}, 전체: {}",
                hospitalId, acceptedPatients.size(), totalElements);

        // 7. PageResponse 생성 및 반환
        return PageResponse.of(acceptedPatients, page, size, totalElements);
    }

    /**
     * 병원이 수용한 환자의 상세 정보 조회
     *
     * @param hospitalId 병원 ID
     * @param emergencyReportId 구급일지 ID
     * @param currentHospitalId 현재 로그인한 병원 ID
     * @return 환자 상세 정보
     */
    @Transactional(readOnly = true)
    public PatientInfoDto getPatientDetail(
            Integer hospitalId,
            Long emergencyReportId,
            Integer currentHospitalId
    ) {
        log.info(LOG_PREFIX + "환자 상세 정보 조회 시작 - 병원 ID: {}, 구급일지 ID: {}",
                hospitalId, emergencyReportId);

        // 1. 권한 검증: 본인의 환자만 조회 가능
        if (!hospitalId.equals(currentHospitalId)) {
            log.warn(LOG_PREFIX + "권한 없는 환자 상세 조회 시도 - 요청 병원 ID: {}, 현재 병원 ID: {}",
                    hospitalId, currentHospitalId);
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 병원이 이 환자를 수용했는지 확인 (ACCEPTED 또는 ARRIVED 상태)
        boolean isAccepted = hospitalSelectionRepository
                .existsByHospitalIdAndEmergencyReportIdAndAccepted(hospitalId, emergencyReportId);

        if (!isAccepted) {
            log.warn(LOG_PREFIX + "수용하지 않은 환자 조회 시도 - 병원 ID: {}, 구급일지 ID: {}",
                    hospitalId, emergencyReportId);
            throw new CustomException(ErrorCode.ACCESS_DENIED,
                    "수용한 환자만 상세 정보를 조회할 수 있습니다.");
        }

        // 3. 환자 정보 조회
        PatientInfo patientInfo = patientInfoRepository.findById(emergencyReportId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_REPORT_NOT_FOUND,
                        "환자 정보를 찾을 수 없습니다."));

        log.info(LOG_PREFIX + "환자 상세 정보 조회 완료 - 병원 ID: {}, 구급일지 ID: {}, 나이: {}, 성별: {}",
                hospitalId, emergencyReportId, patientInfo.getAge(), patientInfo.getGender());

        return PatientInfoDto.from(patientInfo);
    }

    /**
     * 병원이 수용한 환자의 정보 수정
     *
     * @param hospitalId 병원 ID
     * @param emergencyReportId 구급일지 ID
     * @param request 환자 정보 수정 요청
     * @param currentHospitalId 현재 로그인한 병원 ID
     * @return 수정된 환자 정보
     */
    @Transactional
    public PatientInfoDto updatePatientInfo(
            Integer hospitalId,
            Long emergencyReportId,
            UpdatePatientInfoRequest request,
            Integer currentHospitalId
    ) {
        log.info(LOG_PREFIX + "환자 정보 수정 시작 - 병원 ID: {}, 구급일지 ID: {}",
                hospitalId, emergencyReportId);

        // 1. 권한 검증: 본인의 환자만 수정 가능
        if (!hospitalId.equals(currentHospitalId)) {
            log.warn(LOG_PREFIX + "권한 없는 환자 정보 수정 시도 - 요청 병원 ID: {}, 현재 병원 ID: {}",
                    hospitalId, currentHospitalId);
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 병원이 이 환자를 수용했는지 확인 (ACCEPTED 또는 ARRIVED 상태)
        boolean isAccepted = hospitalSelectionRepository
                .existsByHospitalIdAndEmergencyReportIdAndAccepted(hospitalId, emergencyReportId);

        if (!isAccepted) {
            log.warn(LOG_PREFIX + "수용하지 않은 환자 수정 시도 - 병원 ID: {}, 구급일지 ID: {}",
                    hospitalId, emergencyReportId);
            throw new CustomException(ErrorCode.ACCESS_DENIED,
                    "수용한 환자만 정보를 수정할 수 있습니다.");
        }

        // 3. 환자 정보 조회
        PatientInfo patientInfo = patientInfoRepository.findById(emergencyReportId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_REPORT_NOT_FOUND,
                        "환자 정보를 찾을 수 없습니다."));

        // 4. 환자 정보 업데이트
        patientInfo.updatePatientInfo(
                PatientInfo.Gender.valueOf(request.getGender()),
                request.getAge(),
                request.getRecordTime(),
                PatientInfo.MentalStatus.valueOf(request.getMentalStatus()),
                request.getChiefComplaint(),
                request.getHr(),
                request.getBp(),
                request.getSpo2(),
                request.getRr(),
                request.getBt(),
                request.getHasGuardian(),
                request.getHx(),
                request.getOnsetTime(),
                request.getLnt()
        );

        // 5. 저장 (변경 감지로 자동 저장되지만 명시적으로 호출)
        PatientInfo updatedPatientInfo = patientInfoRepository.save(patientInfo);

        log.info(LOG_PREFIX + "환자 정보 수정 완료 - 병원 ID: {}, 구급일지 ID: {}, 나이: {}, 성별: {}",
                hospitalId, emergencyReportId, updatedPatientInfo.getAge(), updatedPatientInfo.getGender());

        // 6. DTO 변환 및 반환
        return PatientInfoDto.from(updatedPatientInfo);
    }

    /**
     * 환자 내원 완료 처리
     *
     * @param hospitalId 병원 ID
     * @param emergencyReportId 구급일지 ID
     * @param currentHospitalId 현재 로그인한 병원 ID
     */
    @Transactional
    public void markPatientAsArrived(
            Integer hospitalId,
            Long emergencyReportId,
            Integer currentHospitalId
    ) {
        log.info(LOG_PREFIX + "환자 내원 완료 처리 시작 - 병원 ID: {}, 구급일지 ID: {}",
                hospitalId, emergencyReportId);

        // 1. 권한 검증: 본인 병원만 처리 가능
        if (!hospitalId.equals(currentHospitalId)) {
            log.warn(LOG_PREFIX + "권한 없는 내원 완료 처리 시도 - 요청 병원 ID: {}, 현재 병원 ID: {}",
                    hospitalId, currentHospitalId);
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        // 2. 병원이 수용한 환자인지 확인 (ACCEPTED 상태)
        HospitalSelection selection = hospitalSelectionRepository
                .findByHospitalIdAndEmergencyReportIdAndStatus(
                        hospitalId,
                        emergencyReportId,
                        HospitalSelectionStatus.ACCEPTED
                )
                .orElseThrow(() -> {
                    log.warn(LOG_PREFIX + "ACCEPTED 상태의 환자를 찾을 수 없음 - 병원 ID: {}, 구급일지 ID: {}",
                            hospitalId, emergencyReportId);
                    return new CustomException(ErrorCode.HOSPITAL_SELECTION_NOT_FOUND,
                            "수용 대기 중인 환자를 찾을 수 없습니다.");
                });

        // 3. 상태를 ARRIVED로 변경
        selection.markAsArrived();

        // 4. 저장 (변경 감지로 자동 저장되지만 명시적으로 호출)
        hospitalSelectionRepository.save(selection);

        log.info(LOG_PREFIX + "환자 내원 완료 처리 완료 - 병원 ID: {}, 구급일지 ID: {}, 선택 ID: {}",
                hospitalId, emergencyReportId, selection.getId());
    }

    /**
     * 구급일지별 병원 선택 상태 조회 (구급대원용)
     *
     * @param emergencyReportId 구급일지 ID
     * @return 병원 선택 상태 응답 (병원 ID, 이름, 상태 목록)
     */
    @Transactional(readOnly = true)
    public HospitalSelectionStatusResponse getHospitalSelectionStatus(Long emergencyReportId) {
        log.info(LOG_PREFIX + "병원 선택 상태 조회 시작 - 구급일지 ID: {}", emergencyReportId);

        // 1. 구급일지 존재 여부 확인
        if (!emergencyReportRepository.existsById(emergencyReportId)) {
            log.warn(LOG_PREFIX + "구급일지를 찾을 수 없음 - 구급일지 ID: {}", emergencyReportId);
            throw new CustomException(ErrorCode.EMERGENCY_REPORT_NOT_FOUND);
        }

        // 2. 해당 구급일지에 대한 모든 HospitalSelection 조회 (Hospital Fetch Join)
        List<HospitalSelection> selections = hospitalSelectionRepository
                .findByEmergencyReportIdWithHospital(emergencyReportId);

        log.info(LOG_PREFIX + "병원 선택 조회 완료 - 구급일지 ID: {}, 병원 수: {}",
                emergencyReportId, selections.size());

        // 3. DTO 변환
        List<HospitalStatusDto> hospitalStatuses = selections.stream()
                .map(HospitalStatusDto::from)
                .toList();

        log.info(LOG_PREFIX + "병원 선택 상태 조회 완료 - 구급일지 ID: {}, 반환 병원 수: {}",
                emergencyReportId, hospitalStatuses.size());

        // 4. 응답 생성
        return HospitalSelectionStatusResponse.of(emergencyReportId, hospitalStatuses);
    }
}
