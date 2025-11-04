package com.ssairen.domain.emergency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record DispatchListResponse(
        @JsonProperty("fire_state")
        FireStateResponse fireState,

        List<DispatchCreateResponse> dispatches,

        PaginationResponse pagination
) {
}
