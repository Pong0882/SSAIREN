package com.ssairen.domain.emergency.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.ssairen.domain.emergency.enums.ReportSectionType;

import java.time.LocalDateTime;

public record ReportSectionCreateResponse(
        Long id,
        Long emergencyReportId,
        Boolean isCompleted,
        ReportSectionType type,
        JsonNode data,
        Integer version,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime createdAt
) {
}
