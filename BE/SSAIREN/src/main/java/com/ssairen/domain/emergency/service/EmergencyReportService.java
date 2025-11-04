package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.EmergencyReportCreateResponse;
import com.ssairen.domain.emergency.dto.FireStateEmergencyReportsResponse;
import com.ssairen.domain.emergency.dto.ParamedicEmergencyReportResponse;

import java.util.List;

public interface EmergencyReportService {

    /**
     * 구급일지 생성 (출동 배정)
     *
     * @param dispatchId  출동지령 ID
     * @param paramedicId 구급대원 ID
     * @return 생성된 구급일지 정보
     */
    EmergencyReportCreateResponse createEmergencyReport(Long dispatchId, Integer paramedicId);

    /**
     * 특정 구급대원이 작성한 모든 보고서 조회
     *
     * @param paramedicId 구급대원 ID
     * @return 구급대원 정보와 작성한 보고서 목록
     */
    ParamedicEmergencyReportResponse getEmergencyReportsByParamedic(Integer paramedicId);

    /**
     * 특정 소방서의 모든 구급일지 조회
     *
     * @param fireStateId 소방서 ID
     * @return 소방서별 구급일지 목록 (List로 래핑)
     */
    List<FireStateEmergencyReportsResponse> getEmergencyReportsByFireState(Integer fireStateId);
}
