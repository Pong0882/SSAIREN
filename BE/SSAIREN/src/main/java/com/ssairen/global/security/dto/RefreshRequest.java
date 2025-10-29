package com.ssairen.global.security.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * RefreshToken 갱신 요청 DTO
 */
public record RefreshRequest(
        @NotBlank(message = "RefreshToken은 필수입니다")
        String refreshToken
) {
}
