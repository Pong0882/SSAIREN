package com.ssairen.domain.emergency.dto;

import com.ssairen.domain.emergency.enums.ReportSectionType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReportSectionCreateRequest(
        @NotNull(message = "구급일지 ID는 필수 입력 항목입니다.")
        @Positive(message = "구급일지 ID는 양의 정수여야 합니다.")
        @Schema(description = "구급일지 ID", example = "5")
        Long emergencyReportId,

        @NotNull(message = "섹션 타입은 필수 입력 항목입니다.")
        @Schema(description = "섹션 유형", example = "PATIENT_INFO")
        ReportSectionType type
) {
}
