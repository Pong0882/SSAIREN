package com.ssairen.domain.firestation.dto;

import com.ssairen.global.annotation.Sensitive;
import com.ssairen.global.validator.ValidPassword;
import com.ssairen.global.validator.ValidStudentNumber;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * 구급대원 로그인 요청 DTO
 */
@Builder
public record ParamedicLoginRequest(
        @NotBlank(message = "학번은 필수 입력 항목입니다.")
        @ValidStudentNumber
        String studentNumber,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @ValidPassword
        @Sensitive
        String password
) {
}
