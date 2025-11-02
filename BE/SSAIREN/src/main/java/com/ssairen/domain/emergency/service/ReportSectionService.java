package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.ReportSectionCreateRequest;
import com.ssairen.domain.emergency.dto.ReportSectionCreateResponse;
import com.ssairen.domain.emergency.enums.ReportSectionType;

public interface ReportSectionService {

    /**
     * 구급일지 섹션 생성
     *
     * @param request 섹션 생성 요청 (구급일지 ID, 섹션 타입)
     * @return 생성된 섹션 정보 (스켈레톤 JSON 포함)
     */
    ReportSectionCreateResponse createReportSection(ReportSectionCreateRequest request);

    /**
     * 구급일지 특정 섹션 조회
     *
     * @param emergencyReportId 구급일지 ID
     * @param type 섹션 타입
     * @return 섹션 정보 (JSON 데이터 포함)
     */
    ReportSectionCreateResponse getReportSection(Long emergencyReportId, ReportSectionType type);
}
