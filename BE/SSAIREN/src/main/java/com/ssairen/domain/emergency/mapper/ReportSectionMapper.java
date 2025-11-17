package com.ssairen.domain.emergency.mapper;

import com.ssairen.domain.emergency.dto.ReportSectionCreateResponse;
import com.ssairen.domain.emergency.entity.ReportSection;
import org.springframework.stereotype.Component;

/**
 * ReportSection 엔티티 ↔ DTO 변환 매퍼
 */
@Component
public class ReportSectionMapper {

    /**
     * ReportSection 엔티티 → ReportSectionCreateResponse DTO 변환
     *
     * @param section 섹션 엔티티
     * @return 섹션 생성 응답 DTO
     */
    public ReportSectionCreateResponse toCreateResponse(ReportSection section) {
        return new ReportSectionCreateResponse(
                section.getId(),
                section.getEmergencyReport().getId(),
                section.getEmergencyReport().getIsCompleted(),
                section.getType(),
                section.getData(),
                section.getVersion(),
                section.getCreatedAt()
        );
    }
}
