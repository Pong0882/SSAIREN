package com.ssairen.domain.emergency.dto;

public record ParamedicEmergencyReportResponse(
        ParamedicInfoResponse paramedicInfo,
        DispatchInfoResponse dispatchInfo
) {
}
