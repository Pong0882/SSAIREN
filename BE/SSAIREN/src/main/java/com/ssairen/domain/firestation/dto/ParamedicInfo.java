package com.ssairen.domain.firestation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 개별 구급대원 정보 Record
 */
@Schema(description = "개별 구급대원")
public record ParamedicInfo(
        @Schema(description = "구급대원 ID", examples = "1")
        Integer paramedicId,

        @Schema(description = "구급대원 이름", examples = "김철수")
        String name,

        @Schema(description = "구급대원 등급", examples = "소방사")
        String rank,

        @Schema(description = "구급대원 상태", examples = "ACTIVE")
        String status,

        @Schema(description = "학번", examples = "20240001")
        String studentNumber,

        @Schema(description = "소속 소방서 ID", examples = "1")
        Integer fireStateId
) {
}
