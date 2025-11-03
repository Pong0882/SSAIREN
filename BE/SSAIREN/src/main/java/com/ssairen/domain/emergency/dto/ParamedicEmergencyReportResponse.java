package com.ssairen.domain.emergency.dto;

import java.util.List;

public record ParamedicEmergencyReportResponse(
        ParamedicInfoResponse paramedicInfo,
        List<EmergencyReportItemResponse> emergencyReports
) {
}
