package com.ssairen.domain.hospital.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * 병원 이송 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalSelectionRequest {

    @NotNull(message = "구급일지 ID는 필수 입력 항목입니다.")
    private Long emergencyReportId;

    @NotEmpty(message = "병원 이름 목록은 필수 입력 항목입니다.")
    private List<String> hospitalNames;
}
