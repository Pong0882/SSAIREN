package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.EmergencyReportCreateResponse;
import com.ssairen.domain.emergency.dto.FireStateEmergencyReportsResponse;
import com.ssairen.domain.emergency.dto.ParamedicEmergencyReportResponse;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.mapper.EmergencyReportMapper;
import com.ssairen.domain.emergency.repository.DispatchRepository;
import com.ssairen.domain.emergency.repository.EmergencyReportRepository;
import com.ssairen.domain.emergency.validation.EmergencyReportValidator;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmergencyReportServiceImpl implements EmergencyReportService {

    private final EmergencyReportRepository emergencyReportRepository;
    private final DispatchRepository dispatchRepository;
    private final ParamedicRepository paramedicRepository;
    private final FireStateRepository fireStateRepository;
    private final EmergencyReportValidator emergencyReportValidator;
    private final EmergencyReportMapper emergencyReportMapper;

    /**
     * 구급일지 생성 (출동 배정)
     *
     * @param dispatchId  출동지령 ID
     * @param paramedicId 구급대원 ID
     * @return 생성된 구급일지 정보
     */
    @Override
    @Transactional
    public EmergencyReportCreateResponse createEmergencyReport(Long dispatchId, Integer paramedicId) {
        // 1. 엔티티 조회
        Dispatch dispatch = dispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new CustomException(ErrorCode.DISPATCH_NOT_FOUND));

        Paramedic paramedic = paramedicRepository.findById(paramedicId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARAMEDIC_NOT_FOUND));

        FireState fireState = fireStateRepository.findById(paramedic.getFireState().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FIRE_STATE_NOT_FOUND));

        // 2. 중복 생성 방지
        if (emergencyReportRepository.existsByDispatch(dispatch)) {
            throw new CustomException(ErrorCode.EMERGENCY_REPORT_ALREADY_EXISTS);
        }

        // 3. 구급일지 엔티티 생성 및 저장
        EmergencyReport emergencyReport = EmergencyReport.builder()
                .dispatch(dispatch)
                .paramedic(paramedic)
                .fireState(fireState)
                .build();

        EmergencyReport savedReport = emergencyReportRepository.save(emergencyReport);

        log.info("구급일지 생성 완료 - 구급일지 ID: {}, 출동지령 ID: {}, 구급대원: {}, 소방서 ID: {}",
                savedReport.getId(), dispatch.getId(), paramedic.getName(), fireState.getId());

        // 4. 응답 DTO 변환
        return emergencyReportMapper.toEmergencyReportCreateResponse(savedReport);
    }

    /**
     * 특정 구급대원이 작성한 모든 보고서 조회
     *
     * @param paramedicId 구급대원 ID
     * @return 구급대원 정보와 작성한 보고서 목록
     */
    @Override
    @Transactional(readOnly = true)
    public ParamedicEmergencyReportResponse getEmergencyReportsByParamedic(Integer paramedicId) {
        // 1. 구급대원 존재 여부 확인
        Paramedic paramedic = paramedicRepository.findById(paramedicId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARAMEDIC_NOT_FOUND));

        // 2. 구급일지 조회
        List<EmergencyReport> emergencyReports = emergencyReportRepository.findByParamedicIdWithFetchJoin(paramedicId);

        log.info("구급대원 보고서 조회 완료 - 구급대원: {}, 조회 건수: {}",
                paramedic.getName(), emergencyReports.size());

        // 3. DTO 변환
        return emergencyReportMapper.toParamedicEmergencyReportResponse(emergencyReports);
    }

    /**
     * 특정 소방서의 모든 구급일지 조회
     *
     * @param paramedicId 구급대원 ID
     * @return 소방서별 구급일지 목록 (List로 래핑)
     */
    @Override
    @Transactional(readOnly = true)
    public List<FireStateEmergencyReportsResponse> getEmergencyReportsByFireState(Integer paramedicId) {
        // 1. 구급대원 조회
        Paramedic paramedic = paramedicRepository.findById(paramedicId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARAMEDIC_NOT_FOUND));

        // 2. 구급대원이 소속된 소방서 조회
        FireState fireState = fireStateRepository.findById(paramedic.getFireState().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FIRE_STATE_NOT_FOUND));

        // 3. 소방서의 모든 구급일지 조회
        List<EmergencyReport> emergencyReports = emergencyReportRepository.findByFireStateIdWithFetchJoin(fireState.getId());

        log.info("소방서 보고서 조회 완료 - 소방서: {}, 조회 건수: {}",
                fireState.getName(), emergencyReports.size());

        // 4. 응답 DTO 변환 (List로 래핑)
        FireStateEmergencyReportsResponse response = emergencyReportMapper
                .toFireStateEmergencyReportsResponse(fireState, emergencyReports);

        return Collections.singletonList(response);
    }
}
