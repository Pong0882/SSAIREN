package com.ssairen.domain.hospital.dto;

import com.ssairen.domain.hospital.entity.HospitalSelection;
import com.ssairen.domain.hospital.enums.HospitalSelectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 병원 상태 DTO (개별 병원 정보)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalStatusDto {

    /**
     * 병원 ID
     */
    private Integer hospitalId;

    /**
     * 병원 이름 (공식 명칭)
     */
    private String hospitalName;

    /**
     * 병원 선택 상태
     */
    private HospitalSelectionStatus status;

    /**
     * 병원 위도
     */
    private BigDecimal latitude;

    /**
     * 병원 경도
     */
    private BigDecimal longitude;

    /**
     * 병원 전화번호
     */
    private String phoneNumber;

    /**
     * 병원 주소
     */
    private String address;

    /**
     * HospitalSelection 엔티티로부터 DTO 생성
     *
     * @param selection HospitalSelection 엔티티 (Hospital Fetch Join 필요)
     * @return HospitalStatusDto
     */
    public static HospitalStatusDto from(HospitalSelection selection) {
        return HospitalStatusDto.builder()
                .hospitalId(selection.getHospital().getId())
                .hospitalName(selection.getHospital().getOfficialName())
                .status(selection.getStatus())
                .latitude(selection.getHospital().getLatitude())
                .longitude(selection.getHospital().getLongitude())
                .phoneNumber(selection.getHospital().getPhoneNumber())
                .address(selection.getHospital().getAddress())
                .build();
    }
}
