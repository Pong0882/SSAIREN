package com.ssairen.global.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssairen.domain.firestation.enums.ParamedicRank;
import com.ssairen.domain.firestation.enums.ParamedicStatus;
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
        String tokenType,

        // 구급대원 상세 정보 (PARAMEDIC인 경우에만 포함)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "구급대원 이름 (PARAMEDIC인 경우)", example = "홍길동")
        String name,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "구급대원 계급 (PARAMEDIC인 경우)", example = "FIREFIGHTER")
        ParamedicRank rank,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "구급대원 상태 (PARAMEDIC인 경우)", example = "ACTIVE")
        ParamedicStatus status,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "소방서 ID (PARAMEDIC인 경우)", example = "1")
        Integer fireStateId,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "소방서명 (PARAMEDIC인 경우)", example = "서울소방서")
        String fireStateName,

        // 병원 상세 정보 (HOSPITAL인 경우에만 포함)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(description = "병원 공식 명칭 (HOSPITAL인 경우)", example = "서울대학교병원")
        String officialName
) {
    /**
     * 편의 생성자 (tokenType 기본값: Bearer, 상세 정보 없음)
     */
    public TokenResponse(String accessToken, String refreshToken, UserType userType,
                         Integer userId, String username) {
        this(accessToken, refreshToken, userType, userId, username, "Bearer",
             null, null, null, null, null, null);
    }

    /**
     * 편의 생성자 (구급대원 상세 정보 포함)
     */
    public TokenResponse(String accessToken, String refreshToken, UserType userType,
                         Integer userId, String username, String name,
                         ParamedicRank rank, ParamedicStatus status,
                         Integer fireStateId, String fireStateName) {
        this(accessToken, refreshToken, userType, userId, username, "Bearer",
             name, rank, status, fireStateId, fireStateName, null);
    }

    /**
     * 편의 생성자 (병원 상세 정보 포함)
     */
    public TokenResponse(String accessToken, String refreshToken, UserType userType,
                         Integer userId, String username, String officialName) {
        this(accessToken, refreshToken, userType, userId, username, "Bearer",
             null, null, null, null, null, officialName);
    }
}
