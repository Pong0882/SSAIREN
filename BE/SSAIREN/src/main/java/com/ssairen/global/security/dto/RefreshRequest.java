package com.ssairen.global.security.dto;

import com.ssairen.global.annotation.ExcludeFromLogging;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * RefreshToken 갱신 요청 DTO
 */
@Schema(description = "토큰 갱신 요청")
public record RefreshRequest(
        @Schema(
                description = "리프레시 토큰 (로그인 시 발급받은 RefreshToken)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @ExcludeFromLogging
        @NotBlank(message = "RefreshToken은 필수입니다")
        String refreshToken
) {
}
