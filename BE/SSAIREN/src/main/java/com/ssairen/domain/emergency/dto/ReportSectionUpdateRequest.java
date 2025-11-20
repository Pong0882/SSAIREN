package com.ssairen.domain.emergency.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

/**
 * 구급일지 섹션 수정 요청 DTO
 */
public record ReportSectionUpdateRequest(
        @NotNull(message = "데이터는 필수 입력 항목입니다.")
        JsonNode data
) {
}
