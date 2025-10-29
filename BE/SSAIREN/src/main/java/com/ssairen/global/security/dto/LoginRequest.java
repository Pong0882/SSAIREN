package com.ssairen.global.security.dto;

import com.ssairen.global.annotation.ExcludeFromLogging;
import com.ssairen.global.security.enums.UserType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 로그인 요청 DTO
 */
public record LoginRequest(
        @NotNull(message = "사용자 타입은 필수입니다")
        UserType userType,

        @NotBlank(message = "사용자명은 필수입니다")
        String username,

        @ExcludeFromLogging
        @NotBlank(message = "비밀번호는 필수입니다")
        String password
) {
}
