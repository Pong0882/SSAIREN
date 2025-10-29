package com.ssairen.domain.firestation.dto;

import com.ssairen.global.annotation.ExcludeFromLogging;
import com.ssairen.global.validator.ValidPassword;
import com.ssairen.global.validator.ValidStudentNumber;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * 구급대원 회원가입 요청 DTO
 */
@Builder
@Schema(description = "구급대원 회원가입 요청")
public record ParamedicRegisterRequest(
        @Schema(description = "학번", example = "20240005", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "학번은 필수 입력 항목입니다.")
        @ValidStudentNumber
        String studentNumber,

        @Schema(description = "비밀번호", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @ValidPassword
        @ExcludeFromLogging
        String password,

        @Schema(description = "이름", example = "김철수", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        String name,

        @Schema(description = "계급", example = "FIREFIGHTER", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "계급은 필수 입력 항목입니다.")
        String rank,

        @Schema(description = "소속 소방서 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "소속 소방서는 필수 입력 항목입니다.")
        Integer fireStateId
) {
}
