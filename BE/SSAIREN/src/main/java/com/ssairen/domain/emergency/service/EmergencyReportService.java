package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.EmergencyReportCreateRequest;
import com.ssairen.domain.emergency.dto.EmergencyReportCreateResponse;

public interface EmergencyReportService {

    /**
     * 구급일지 생성 (출동 배정)
     *
     * @param request 구급일지 생성 요청 (출동지령 ID, 구급대원 ID, 소방서 ID)
     * @return 생성된 구급일지 정보
     */
    EmergencyReportCreateResponse createEmergencyReport(EmergencyReportCreateRequest request);
}
