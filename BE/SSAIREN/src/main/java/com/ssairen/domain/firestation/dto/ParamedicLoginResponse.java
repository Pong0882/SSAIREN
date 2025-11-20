package com.ssairen.domain.firestation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.enums.ParamedicRank;
import com.ssairen.domain.firestation.enums.ParamedicStatus;
import com.ssairen.global.annotation.ExcludeFromLogging;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * 구급대원 로그인 응답 DTO
 */
@Builder
public record ParamedicLoginResponse(
        @Schema(description = "액세스 토큰 (추후 JWT 구현 예정)", example = "null", nullable = true)
        @com.fasterxml.jackson.annotation.JsonProperty("access_token")
        @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS)
        @ExcludeFromLogging
        String accessToken,

        @Schema(description = "리프레시 토큰 (추후 JWT 구현 예정)", example = "null", nullable = true)
        @com.fasterxml.jackson.annotation.JsonProperty("refresh_token")
        @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.ALWAYS)
        @ExcludeFromLogging
        String refreshToken,

        @Schema(description = "구급대원 정보")
        ParamedicInfo paramedic
) {
    @Builder
    @Schema(description = "구급대원 상세 정보")
    public record ParamedicInfo(
            @Schema(description = "구급대원 ID", example = "1")
            Integer id,

            @Schema(description = "학번", example = "20240001")
            String studentNumber,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "계급", example = "소방사")
            ParamedicRank rank,

            @Schema(description = "상태", example = "ACTIVE")
            ParamedicStatus status,

            @Schema(description = "소속 소방서 ID", example = "1")
            Integer fireStateId,

            @Schema(description = "소속 소방서 이름", example = "서울소방서")
            String fireStateName
    ) {
    }

    public static ParamedicLoginResponse from(Paramedic paramedic) {
        return ParamedicLoginResponse.builder()
                .accessToken(null)  // JWT 구현 시 추가 예정
                .refreshToken(null)  // JWT 구현 시 추가 예정
                .paramedic(ParamedicInfo.builder()
                        .id(paramedic.getId())
                        .studentNumber(paramedic.getStudentNumber())
                        .name(paramedic.getName())
                        .rank(paramedic.getRank())
                        .status(paramedic.getStatus())
                        .fireStateId(paramedic.getFireState().getId())
                        .fireStateName(paramedic.getFireState().getName())
                        .build())
                .build();
    }
}