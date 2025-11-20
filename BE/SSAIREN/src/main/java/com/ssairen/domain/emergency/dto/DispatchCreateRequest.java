package com.ssairen.domain.emergency.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record DispatchCreateRequest(
        @NotNull(message = "소방서 ID는 필수 입력 항목입니다.")
        @Positive(message = "소방서 ID는 양의 정수여야 합니다.")
        Integer fireStateId,

//        @NotNull(message = "소방서 ID는 필수 입력 항목입니다.")
        @Positive(message = "구급대원 ID는 양의 정수여야 합니다.")
        Integer paramedicId,

        @Size(max = 50, message = "재난번호는 50자 이내여야 합니다.")
        String disasterNumber,

        @NotBlank(message = "재난 분류는 필수 입력 항목입니다.")
        @Size(max = 50, message = "재난 분류는 50자 이내여야 합니다.")
        String disasterType,

        @Size(max = 50, message = "재난 종별은 50자 이내여야 합니다.")
        String disasterSubtype,

        @Size(max = 20, message = "신고자 이름은 20자 이내여야 합니다.")
        String reporterName,

        @Size(max = 50, message = "신고자 연락처는 50자 이내여야 합니다.")
        String reporterPhone,

        @NotBlank(message = "출동지 주소는 필수 입력 항목입니다.")
        @Size(max = 50, message = "출동지 주소는 50자 이내여야 합니다.")
        String locationAddress,

        @Size(max = 100, message = "사고 발생 내용은 100자 이내여야 합니다.")
        String incidentDescription,

        @Size(max = 50, message = "출동 구분은 50자 이내여야 합니다.")
        String dispatchLevel,

        @Positive(message = "출동차수는 양의 정수여야 합니다.")
        Integer dispatchOrder,

        @Size(max = 50, message = "출동 센터는 50자 이내여야 합니다.")
        String dispatchStation,

        @NotNull(message = "출동 일시는 필수 입력 항목입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime date
) {
}
