package com.ssairen.global.security.dto;

import com.ssairen.global.annotation.ExcludeFromLogging;
import com.ssairen.global.security.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 토큰 응답 DTO
 */
@Schema(description = "토큰 응답")
public record TokenResponse(
        @Schema(
                description = "액세스 토큰 (API 호출 시 Authorization 헤더에 사용, 유효기간: 15분)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @ExcludeFromLogging
        String accessToken,

        @Schema(
                description = "리프레시 토큰 (AccessToken 갱신에 사용, 유효기간: 7일)",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        @ExcludeFromLogging
        String refreshToken,

        @Schema(
                description = "사용자 타입",
                example = "PARAMEDIC",
                allowableValues = {"PARAMEDIC", "HOSPITAL"}
        )
        UserType userType,

        @Schema(description = "사용자 ID", example = "1")
        Integer userId,

        @Schema(description = "사용자명", example = "20240101")
        String username,

        @Schema(description = "토큰 타입", example = "Bearer", defaultValue = "Bearer")
        String tokenType
) {
    /**
     * 편의 생성자 (tokenType 기본값: Bearer)
     */
    public TokenResponse(String accessToken, String refreshToken, UserType userType,
                         Integer userId, String username) {
        this(accessToken, refreshToken, userType, userId, username, "Bearer");
    }
}
