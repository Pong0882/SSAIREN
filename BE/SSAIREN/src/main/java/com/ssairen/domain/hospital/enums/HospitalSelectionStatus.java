package com.ssairen.domain.hospital.enums;

public enum HospitalSelectionStatus {
    PENDING,      // 대기중
    ACCEPTED,     // 수락 (내원 대기중)
    ARRIVED,      // 내원 완료
    REJECTED,     // 거절
    COMPLETED,    // 완료 (다른 병원이 수락하여 종료됨)
    CALLREQUEST   // 전화요망
}