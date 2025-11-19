package com.ssairen.domain.hospital.dto;

import com.ssairen.domain.hospital.entity.HospitalSelection;
import com.ssairen.domain.hospital.enums.HospitalSelectionStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 병원 이송 요청 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalSelectionResponse {

    private Long emergencyReportId;
    private List<HospitalInfo> hospitals;
    private String message;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HospitalInfo {
        private Integer hospitalSelectionId;
        private String hospitalName;
        private HospitalSelectionStatus status;
        private LocalDateTime createdAt;
        private String phoneNumber;
        private String address;

        public static HospitalInfo from(HospitalSelection selection) {
            return HospitalInfo.builder()
                    .hospitalSelectionId(selection.getId())
                    .hospitalName(selection.getHospital().getName())
                    .status(selection.getStatus())
                    .createdAt(selection.getCreatedAt())
                    .phoneNumber(selection.getHospital().getPhoneNumber())
                    .address(selection.getHospital().getAddress())
                    .build();
        }
    }

    public static HospitalSelectionResponse from(Long emergencyReportId, List<HospitalSelection> selections) {
        return HospitalSelectionResponse.builder()
                .emergencyReportId(emergencyReportId)
                .hospitals(selections.stream()
                        .map(HospitalInfo::from)
                        .toList())
                .message(selections.size() + "개 병원에 이송 요청을 전송했습니다.")
                .build();
    }
}
