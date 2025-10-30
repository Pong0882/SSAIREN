package com.ssairen.domain.hospital.dto;

import com.ssairen.domain.hospital.entity.HospitalSelection;
import com.ssairen.domain.hospital.enums.HospitalSelectionStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 병원 응답 결과 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalResponseDto {

    private Integer hospitalSelectionId;
    private Integer hospitalId;
    private String hospitalName;
    private Long emergencyReportId;
    private HospitalSelectionStatus status;
    private LocalDateTime responseAt;

    public static HospitalResponseDto from(HospitalSelection selection) {
        return HospitalResponseDto.builder()
                .hospitalSelectionId(selection.getId())
                .hospitalId(selection.getHospital().getId())
                .hospitalName(selection.getHospital().getName())
                .emergencyReportId(selection.getEmergencyReport().getId())
                .status(selection.getStatus())
                .responseAt(selection.getResponseAt())
                .build();
    }
}
