package com.ssairen.domain.hospital.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 환자 목록 필터 타입
 */
@Getter
public enum PatientFilterType {
    ALL(Arrays.asList(HospitalSelectionStatus.ACCEPTED, HospitalSelectionStatus.ARRIVED)),
    ACCEPTED(List.of(HospitalSelectionStatus.ACCEPTED));

    private final List<HospitalSelectionStatus> statuses;

    PatientFilterType(List<HospitalSelectionStatus> statuses) {
        this.statuses = statuses;
    }
}
