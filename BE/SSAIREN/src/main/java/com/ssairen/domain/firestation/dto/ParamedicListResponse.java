package com.ssairen.domain.firestation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * 전체 구급대원 조회 응답 Record
 */
@Schema(description = "전체 구급대원 목록 조회 응답")
public record ParamedicListResponse(
        @Schema(description = "전체 구급대원 목록")
        List<ParamedicInfo> paramedics
) {

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
}
