package com.ssairen.domain.emergency.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record EmergencyReportSummaryResponse(
        Long id,
        Boolean isCompleted,
        String patientName,
        ParamedicInfoSimple paramedicInfo,
        DispatchInfoSimple dispatchInfo,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime createdAt
) {
}
