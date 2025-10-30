package com.ssairen.domain.emergency.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record EmergencyReportCreateResponse(
        Long emergencyReportId,
        ParamedicInfoResponse paramedicInfo,
        DispatchInfoResponse dispatchInfo,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime createdAt
) {
}
