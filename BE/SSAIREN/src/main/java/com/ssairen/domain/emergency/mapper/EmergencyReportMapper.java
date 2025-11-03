package com.ssairen.domain.emergency.mapper;

import com.ssairen.domain.emergency.dto.*;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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

    public FireStateResponse toFireStateResponse(FireState fireState) {
        return new FireStateResponse(
                fireState.getId(),
                fireState.getName()
        );
    }

    public DispatchInfoResponse toDispatchInfoResponse(Dispatch dispatch) {
        return new DispatchInfoResponse(
                dispatch.getId(),
                dispatch.getDisasterNumber(),
                dispatch.getDisasterType(),
                dispatch.getLocationAddress(),
                dispatch.getDate(),
                toFireStateResponse(dispatch.getFireState())
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

    public ParamedicEmergencyReportResponse toParamedicEmergencyReportResponse(EmergencyReport emergencyReport) {
        ParamedicInfoResponse paramedicInfo = toParamedicInfoResponse(emergencyReport.getParamedic());
        DispatchInfoResponse dispatchInfo = toDispatchInfoResponse(emergencyReport.getDispatch());

        return new ParamedicEmergencyReportResponse(
                paramedicInfo,
                dispatchInfo
        );
    }

    public List<ParamedicEmergencyReportResponse> toParamedicEmergencyReportResponseList(List<EmergencyReport> emergencyReports) {
        return emergencyReports.stream()
                .map(this::toParamedicEmergencyReportResponse)
                .collect(Collectors.toList());
    }

    public ParamedicInfoSimple toParamedicInfoSimple(Paramedic paramedic) {
        return new ParamedicInfoSimple(
                paramedic.getId(),
                paramedic.getName()
        );
    }

    public DispatchInfoSimple toDispatchInfoSimple(Dispatch dispatch) {
        return new DispatchInfoSimple(
                dispatch.getId(),
                dispatch.getDisasterNumber(),
                dispatch.getDate()
        );
    }

    public EmergencyReportSummaryResponse toEmergencyReportSummaryResponse(EmergencyReport emergencyReport) {
        ParamedicInfoSimple paramedicInfo = toParamedicInfoSimple(emergencyReport.getParamedic());
        DispatchInfoSimple dispatchInfo = toDispatchInfoSimple(emergencyReport.getDispatch());

        return new EmergencyReportSummaryResponse(
                emergencyReport.getId(),
                paramedicInfo,
                dispatchInfo,
                emergencyReport.getCreatedAt()
        );
    }

    public List<EmergencyReportSummaryResponse> toEmergencyReportSummaryResponseList(List<EmergencyReport> emergencyReports) {
        return emergencyReports.stream()
                .map(this::toEmergencyReportSummaryResponse)
                .collect(Collectors.toList());
    }

    public FireStateEmergencyReportsResponse toFireStateEmergencyReportsResponse(FireState fireState, List<EmergencyReport> emergencyReports) {
        FireStateResponse fireStateInfo = toFireStateResponse(fireState);
        List<EmergencyReportSummaryResponse> summaryList = toEmergencyReportSummaryResponseList(emergencyReports);

        return new FireStateEmergencyReportsResponse(
                fireStateInfo,
                summaryList
        );
    }
}
