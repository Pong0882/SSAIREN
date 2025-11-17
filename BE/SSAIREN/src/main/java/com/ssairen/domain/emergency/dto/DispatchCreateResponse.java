package com.ssairen.domain.emergency.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record DispatchCreateResponse(
        Long id,
        String disasterNumber,
        String disasterType,
        String disasterSubtype,
        String reporterName,
        String reporterPhone,
        String locationAddress,
        String incidentDescription,
        String dispatchLevel,
        Integer dispatchOrder,
        String dispatchStation,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime date
) {
}
