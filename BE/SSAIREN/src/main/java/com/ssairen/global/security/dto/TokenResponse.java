package com.ssairen.global.security.dto;

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

        UserType userType,
        Integer userId,
        String username,
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
