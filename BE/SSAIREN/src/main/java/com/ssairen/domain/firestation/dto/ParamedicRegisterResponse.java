package com.ssairen.domain.firestation.dto;

import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.enums.ParamedicRank;
import com.ssairen.domain.firestation.enums.ParamedicStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 구급대원 회원가입 응답 DTO
 */
@Builder
@Schema(description = "구급대원 회원가입 응답")
public record ParamedicRegisterResponse(
        @Schema(description = "구급대원 ID", example = "1")
        Integer id,

        @Schema(description = "학번", example = "20240005")
        String studentNumber,

        @Schema(description = "이름", example = "김철수")
        String name,

        @Schema(description = "계급", example = "FIREFIGHTER")
        ParamedicRank rank,

        @Schema(description = "상태", example = "ACTIVE")
        ParamedicStatus status,

        @Schema(description = "소속 소방서 ID", example = "1")
        Integer fireStateId,

        @Schema(description = "소속 소방서 이름", example = "강남소방서")
        String fireStateName
) {
    public static ParamedicRegisterResponse from(Paramedic paramedic) {
        return ParamedicRegisterResponse.builder()
                .id(paramedic.getId())
                .studentNumber(paramedic.getStudentNumber())
                .name(paramedic.getName())
                .rank(paramedic.getRank())
                .status(paramedic.getStatus())
                .fireStateId(paramedic.getFireState().getId())
                .fireStateName(paramedic.getFireState().getName())
                .build();
    }
}
