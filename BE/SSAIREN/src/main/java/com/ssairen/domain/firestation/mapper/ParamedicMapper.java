package com.ssairen.domain.firestation.mapper;

import com.ssairen.domain.firestation.dto.ParamedicInfo;
import com.ssairen.domain.firestation.entity.Paramedic;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface ParamedicMapper {

    @Mapping(source = "id", target = "paramedicId")
    @Mapping(source = "fireState.id", target = "fireStateId")
    ParamedicInfo toParamedicInfo(Paramedic paramedic);

    /**
     * NOTE: 현재는 List<ParamedicInfo>를 직접 반환
     * 만약 페이징 정보, 총 개수 등의 메타데이터가 필요한 경우, 래핑위한 메서드 별도 추가
     *
     * 예시:
     * default ParamedicListResponse toParamedicListResponse(List<ParamedicInfo> paramedicInfoList) {
     *     return new ParamedicListResponse(paramedicInfoList);
     * }
     */
    List<ParamedicInfo> toParamedicInfoList(List<Paramedic> paramedics);
}
