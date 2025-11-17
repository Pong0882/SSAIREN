package com.ssairen.domain.emergency.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaginationResponse(
        @JsonProperty("next_cursor")
        String nextCursor,

        @JsonProperty("has_more")
        Boolean hasMore
) {
}
