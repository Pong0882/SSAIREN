package com.ssairen.domain.hospital.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * AI 기반 병원 추천 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalAiRecommendationRequest {

    @Schema(description = "구급일지 ID", example = "1", required = true)
    @NotNull(message = "구급일지 ID는 필수 입력 항목입니다.")
    private Long emergencyReportId;

    @Schema(description = "위도", example = "37.5062528", required = true)
    @NotNull(message = "위도는 필수 입력 항목입니다.")
    private Double latitude;

    @Schema(description = "경도", example = "127.0317056", required = true)
    @NotNull(message = "경도는 필수 입력 항목입니다.")
    private Double longitude;

    @Schema(description = "검색 반경 (킬로미터)", example = "10", required = true)
    @NotNull(message = "반경은 필수 입력 항목입니다.")
    private Integer radius;
}