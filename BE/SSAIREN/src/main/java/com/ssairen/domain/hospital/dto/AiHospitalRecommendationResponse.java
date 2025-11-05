package com.ssairen.domain.hospital.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * AI 기반 병원 추천 및 이송 요청 응답 DTO
 * AI 추천 정보와 병원 이송 요청 결과를 함께 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "AI 기반 병원 추천 및 이송 요청 응답")
public class AiHospitalRecommendationResponse {

    @Schema(description = "구급일지 ID", example = "1")
    private Long emergencyReportId;

    @Schema(description = "AI 추론 정보 (GPT가 병원을 추천한 이유)")
    private String gptReasoning;

    @Schema(description = "추천된 병원 이름 목록")
    private List<String> recommendedHospitals;

    @Schema(description = "반경 내 찾은 전체 병원 수", example = "15")
    private Integer totalHospitalsFound;

    @Schema(description = "AI 추론에 걸린 시간 (초)", example = "2.5")
    private Double reasoningTime;

    @Schema(description = "병원 상세 정보 목록")
    private List<Map<String, Object>> hospitalsDetail;

    @Schema(description = "생성된 병원 이송 요청 목록")
    private List<HospitalSelectionResponse.HospitalInfo> hospitalSelections;

    /**
     * AI 추천 응답과 병원 선택 목록으로부터 DTO 생성
     */
    public static AiHospitalRecommendationResponse of(
            Long emergencyReportId,
            AiRecommendationResponse aiResponse,
            HospitalSelectionResponse selectionResponse
    ) {
        return AiHospitalRecommendationResponse.builder()
                .emergencyReportId(emergencyReportId)
                .gptReasoning(aiResponse.getGptReasoning())
                .recommendedHospitals(aiResponse.getRecommendedHospitals())
                .totalHospitalsFound(aiResponse.getTotalHospitalsFound())
                .reasoningTime(aiResponse.getReasoningTime())
                .hospitalsDetail(aiResponse.getHospitalsDetail())
                .hospitalSelections(selectionResponse.getHospitals())
                .build();
    }
}