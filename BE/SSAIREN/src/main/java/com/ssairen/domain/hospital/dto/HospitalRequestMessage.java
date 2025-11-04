package com.ssairen.domain.hospital.dto;

import lombok.*;

/**
 * 병원에게 전송할 웹소켓 요청 메시지 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalRequestMessage {

    private String type;  // "REQUEST"
    private Integer hospitalSelectionId;
    private Long emergencyReportId;
    private PatientInfoDto patientInfo;

    public static HospitalRequestMessage of(Integer hospitalSelectionId, Long emergencyReportId, PatientInfoDto patientInfo) {
        return HospitalRequestMessage.builder()
                .type("REQUEST")
                .hospitalSelectionId(hospitalSelectionId)
                .emergencyReportId(emergencyReportId)
                .patientInfo(patientInfo)
                .build();
    }
}
