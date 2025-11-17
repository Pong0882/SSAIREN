package com.ssairen.domain.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 구급일지별 병원 선택 상태 조회 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalSelectionStatusResponse {

    /**
     * 구급일지 ID
     */
    private Long emergencyReportId;

    /**
     * 요청 보낸 병원 목록 (병원 ID, 이름, 상태)
     */
    private List<HospitalStatusDto> hospitals;

    /**
     * 편의 생성자
     *
     * @param emergencyReportId 구급일지 ID
     * @param hospitals 병원 상태 목록
     * @return HospitalSelectionStatusResponse
     */
    public static HospitalSelectionStatusResponse of(Long emergencyReportId, List<HospitalStatusDto> hospitals) {
        return HospitalSelectionStatusResponse.builder()
                .emergencyReportId(emergencyReportId)
                .hospitals(hospitals)
                .build();
    }
}
