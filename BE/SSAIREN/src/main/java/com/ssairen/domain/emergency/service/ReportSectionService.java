package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.ReportSectionCreateResponse;
import com.ssairen.domain.emergency.dto.ReportSectionUpdateRequest;
import com.ssairen.domain.emergency.enums.ReportSectionType;

public interface ReportSectionService {

    /**
     * 구급일지 섹션 생성
     *
     * @param emergencyReportId 구급일지 ID
     * @param type              섹션 타입
     * @param paramedicId       구급대원 ID (권한 검증용)
     * @return 생성된 섹션 정보 (스켈레톤 JSON 포함)
     */
    ReportSectionCreateResponse createReportSection(Long emergencyReportId, ReportSectionType type, Integer paramedicId);

    /**
     * 구급일지 특정 섹션 조회
     *
     * @param emergencyReportId 구급일지 ID
     * @param type              섹션 타입
     * @param paramedicId       구급대원 ID (권한 검증용)
     * @return 섹션 정보 (JSON 데이터 포함)
     */
    ReportSectionCreateResponse getReportSection(Long emergencyReportId, ReportSectionType type, Integer paramedicId);

    /**
     * 구급일지 섹션 수정
     *
     * @param emergencyReportId 구급일지 ID
     * @param type              섹션 타입
     * @param request           수정할 데이터
     * @param paramedicId       구급대원 ID (권한 검증용)
     * @return 수정된 섹션 정보
     */
    ReportSectionCreateResponse updateReportSection(Long emergencyReportId, ReportSectionType type, ReportSectionUpdateRequest request, Integer paramedicId);
}
