package com.ssairen.domain.emergency.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EmergencyReportCreateRequest(
        @NotNull(message = "출동지령 ID는 필수 입력 항목입니다.")
        @Positive(message = "출동지령 ID는 양의 정수여야 합니다.")
        @Schema(examples = "1")
        Long dispatchId,

        @NotNull(message = "구급대원 ID는 필수 입력 항목입니다.")
        @Positive(message = "구급대원 ID는 양의 정수여야 합니다.")
        @Schema(examples = "1")
        Integer paramedicId,

        @NotNull(message = "소방서 ID는 필수 입력 항목입니다.")
        @Positive(message = "소방서 ID는 양의 정수여야 합니다.")
        @Schema(examples = "1")
        Integer fireStateId
) {
}
