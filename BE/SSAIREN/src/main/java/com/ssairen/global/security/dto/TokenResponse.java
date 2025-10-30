package com.ssairen.global.security.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssairen.domain.firestation.enums.ParamedicRank;
import com.ssairen.domain.firestation.enums.ParamedicStatus;
import com.ssairen.global.annotation.ExcludeFromLogging;
import com.ssairen.global.security.enums.UserType;

/**
 * 토큰 응답 DTO
 */
public record TokenResponse(
        @ExcludeFromLogging
        String accessToken,

        @ExcludeFromLogging
        String refreshToken,

        String tokenType,
        UserType userType,
        Integer userId,
        String username,

        // 구급대원 상세 정보 (PARAMEDIC인 경우에만 포함)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String name,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        ParamedicRank rank,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        ParamedicStatus status,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        Integer fireStateId,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String fireStateName
) {
    /**
     * 편의 생성자 (tokenType 기본값: Bearer, 상세 정보 없음)
     */
    public TokenResponse(String accessToken, String refreshToken, UserType userType,
                         Integer userId, String username) {
        this(accessToken, refreshToken, "Bearer", userType, userId, username,
             null, null, null, null, null);
    }

    /**
     * 편의 생성자 (구급대원 상세 정보 포함)
     */
    public TokenResponse(String accessToken, String refreshToken, UserType userType,
                         Integer userId, String username, String name,
                         ParamedicRank rank, ParamedicStatus status,
                         Integer fireStateId, String fireStateName) {
        this(accessToken, refreshToken, "Bearer", userType, userId, username,
             name, rank, status, fireStateId, fireStateName);
    }
}
