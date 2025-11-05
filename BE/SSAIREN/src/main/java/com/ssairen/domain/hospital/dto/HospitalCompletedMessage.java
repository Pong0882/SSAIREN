package com.ssairen.domain.hospital.dto;

import lombok.*;

/**
 * 병원에게 전송할 웹소켓 완료 메시지 DTO
 * ACCEPTED 처리된 다른 병원이 있어 자동으로 COMPLETED 처리된 경우 전송
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalCompletedMessage {

    private String type;  // "COMPLETED"
    private Integer hospitalSelectionId;
    private Long emergencyReportId;

    public static HospitalCompletedMessage of(Integer hospitalSelectionId, Long emergencyReportId) {
        return HospitalCompletedMessage.builder()
                .type("COMPLETED")
                .hospitalSelectionId(hospitalSelectionId)
                .emergencyReportId(emergencyReportId)
                .build();
    }
}
