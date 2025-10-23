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
}
