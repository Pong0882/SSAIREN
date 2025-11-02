package com.ssairen.domain.emergency.dto;

import java.util.List;

public record FireStateEmergencyReportsResponse(
        FireStateResponse fireStateInfo,
        List<EmergencyReportSummaryResponse> emergencyReports
) {
}
