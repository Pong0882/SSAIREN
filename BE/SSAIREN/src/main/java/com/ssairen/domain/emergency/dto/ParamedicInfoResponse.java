package com.ssairen.domain.emergency.dto;

public record ParamedicInfoResponse(
        Integer paramedicId,
        String name,
        String rank,
        Integer fireStateId,
        String studentNumber
) {
}
