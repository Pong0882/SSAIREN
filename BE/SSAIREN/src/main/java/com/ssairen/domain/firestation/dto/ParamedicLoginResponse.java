package com.ssairen.domain.firestation.dto;

import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.enums.ParamedicRank;
import com.ssairen.domain.firestation.enums.ParamedicStatus;
import lombok.Builder;

/**
 * 구급대원 로그인 응답 DTO
 */
@Builder
public record ParamedicLoginResponse(
        Integer id,
        String studentNumber,
        String name,
        ParamedicRank rank,
        ParamedicStatus status,
        FireStationInfo fireStation
) {
    @Builder
    public record FireStationInfo(
            Integer id,
            String name
    ) {
    }

    public static ParamedicLoginResponse from(Paramedic paramedic) {
        return ParamedicLoginResponse.builder()
                .id(paramedic.getId())
                .studentNumber(paramedic.getStudentNumber())
                .name(paramedic.getName())
                .rank(paramedic.getRank())
                .status(paramedic.getStatus())
                .fireStation(FireStationInfo.builder()
                        .id(paramedic.getFireState().getId())
                        .name(paramedic.getFireState().getName())
                        .build())
                .build();
    }
}