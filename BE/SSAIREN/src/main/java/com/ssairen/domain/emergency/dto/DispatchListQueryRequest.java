package com.ssairen.domain.emergency.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record DispatchListQueryRequest(
        @Schema(description = "다음 페이지 커서 (첫 요청 시 생략)", example = "Ng")
        String cursor,

        @Schema(description = "페이지당 개수 (기본값: 10, 최소: 1, 최대: 100)", example = "10")
        @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
        @Max(value = 100, message = "limit은 100 이하여야 합니다.")
        Integer limit
) {
    public DispatchListQueryRequest {
        if (limit == null) {
            limit = 10;
        }
    }
}
