package com.ssairen.domain.emergency.mapper;

import com.ssairen.domain.emergency.dto.DispatchInfoResponse;
import com.ssairen.domain.emergency.dto.EmergencyReportCreateResponse;
import com.ssairen.domain.emergency.dto.ParamedicInfoResponse;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.firestation.entity.Paramedic;
import org.springframework.stereotype.Component;

@Component
public class EmergencyReportMapper {

    public ParamedicInfoResponse toParamedicInfoResponse(Paramedic paramedic) {
        return new ParamedicInfoResponse(
                paramedic.getId(),
                paramedic.getName(),
                paramedic.getRank().name(),
                paramedic.getFireState().getId(),
                paramedic.getStudentNumber()
        );
    }

    public DispatchInfoResponse toDispatchInfoResponse(Dispatch dispatch) {
        return new DispatchInfoResponse(
                dispatch.getId(),
                dispatch.getDisasterNumber(),
                dispatch.getDisasterType(),
                dispatch.getLocationAddress(),
                dispatch.getDate()
        );
    }

    public EmergencyReportCreateResponse toEmergencyReportCreateResponse(EmergencyReport emergencyReport) {
        ParamedicInfoResponse paramedicInfo = toParamedicInfoResponse(emergencyReport.getParamedic());
        DispatchInfoResponse dispatchInfo = toDispatchInfoResponse(emergencyReport.getDispatch());

        return new EmergencyReportCreateResponse(
                emergencyReport.getId(),
                paramedicInfo,
                dispatchInfo,
                emergencyReport.getCreatedAt()
        );
    }
}
