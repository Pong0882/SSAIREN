package com.ssairen.global.security.dto;

import com.ssairen.global.annotation.ExcludeFromLogging;
import com.ssairen.global.security.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 로그인 요청 DTO
 */
@Schema(description = "로그인 요청")
public record LoginRequest(
        @Schema(
                description = "사용자 타입",
                example = "PARAMEDIC",
                allowableValues = {"PARAMEDIC", "HOSPITAL"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "사용자 타입은 필수입니다")
        UserType userType,

        @Schema(
                description = "사용자명 (구급대원: 학번, 병원: 병원 ID)",
                example = "20240001",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "사용자명은 필수입니다")
        String username,

        @Schema(
                description = "비밀번호",
                example = "Password123!",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @ExcludeFromLogging
        @NotBlank(message = "비밀번호는 필수입니다")
        String password
) {
}
