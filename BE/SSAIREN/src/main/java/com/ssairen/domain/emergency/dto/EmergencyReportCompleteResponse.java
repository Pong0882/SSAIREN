package com.ssairen.domain.emergency.dto;

public record EmergencyReportCompleteResponse(
        Long emergencyReportId,
        Boolean isCompleted
) {
}
