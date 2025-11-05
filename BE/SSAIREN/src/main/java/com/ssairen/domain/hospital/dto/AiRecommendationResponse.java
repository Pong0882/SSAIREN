package com.ssairen.domain.hospital.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRecommendationResponse {
    private Boolean success;

    @JsonProperty("recommended_hospitals")
    private List<String> recommendedHospitals;

    @JsonProperty("total_hospitals_found")
    private Integer totalHospitalsFound;

    @JsonProperty("hospitals_detail")
    private List<Map<String, Object>> hospitalsDetail;

    @JsonProperty("gpt_reasoning")
    private String gptReasoning;

    @JsonProperty("reasoning_time")
    private Double reasoningTime;
}