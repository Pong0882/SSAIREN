package com.ssairen.domain.emergency.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record DispatchInfoSimple(
        Long id,
        String disasterNumber,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime date
) {
}
