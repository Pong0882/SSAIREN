package com.ssairen.domain.hospital.dto;

import com.ssairen.domain.hospital.enums.HospitalSelectionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 병원 응답 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalResponseRequest {

    @NotNull(message = "상태는 필수 입력 항목입니다.")
    private HospitalSelectionStatus status;
}
