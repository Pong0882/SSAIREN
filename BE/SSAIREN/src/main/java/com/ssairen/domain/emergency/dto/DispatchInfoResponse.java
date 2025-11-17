package com.ssairen.domain.emergency.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record DispatchInfoResponse(
        Long dispatchId,
        String disasterNumber,
        String disasterType,
        String locationAddress,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime date,
        FireStateResponse fireStateInfo
) {
}
