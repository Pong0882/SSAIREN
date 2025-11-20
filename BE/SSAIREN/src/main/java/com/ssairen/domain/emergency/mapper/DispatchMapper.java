package com.ssairen.domain.emergency.mapper;

import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import com.ssairen.domain.emergency.dto.DispatchCreateResponse;
import com.ssairen.domain.emergency.dto.FireStateResponse;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DispatchMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fireState", source = "fireState")
    @Mapping(target = "paramedic", source = "paramedic")
    Dispatch toEntity(DispatchCreateRequest request, FireState fireState, Paramedic paramedic);

    DispatchCreateResponse toResponse(Dispatch entity);

    List<DispatchCreateResponse> toResponseList(List<Dispatch> dispatches);

    FireStateResponse toFireStateResponse(FireState fireState);
}
