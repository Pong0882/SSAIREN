package com.ssairen.domain.emergency.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record EmergencyReportItemResponse(
        Long id,
        DispatchInfoResponse dispatchInfo,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime createdAt
) {
}
