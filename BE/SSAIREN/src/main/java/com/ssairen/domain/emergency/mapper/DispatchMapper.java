package com.ssairen.domain.emergency.mapper;

import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import com.ssairen.domain.emergency.dto.DispatchCreateResponse;
import com.ssairen.domain.emergency.entity.Dispatch;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface DispatchMapper {
    
    Dispatch toEntity(DispatchCreateRequest request);

    DispatchCreateResponse toResponse(Dispatch entity);
}
