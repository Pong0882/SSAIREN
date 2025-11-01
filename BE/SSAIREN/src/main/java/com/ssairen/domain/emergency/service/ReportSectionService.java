package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.ReportSectionCreateRequest;
import com.ssairen.domain.emergency.dto.ReportSectionCreateResponse;

public interface ReportSectionService {

    /**
     * 구급일지 섹션 생성
     *
     * @param request 섹션 생성 요청 (구급일지 ID, 섹션 타입)
     * @return 생성된 섹션 정보 (스켈레톤 JSON 포함)
     */
    ReportSectionCreateResponse createReportSection(ReportSectionCreateRequest request);
}
