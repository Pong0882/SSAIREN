package com.ssairen.domain.emergency.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssairen.domain.emergency.dto.*;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.entity.ReportSection;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.repository.ReportSectionRepository;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmergencyReportMapper {

    private final ReportSectionRepository reportSectionRepository;

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
                emergencyReport.getIsCompleted(),
                paramedicInfo,
                dispatchInfo,
                emergencyReport.getCreatedAt()
        );
    }

    public EmergencyReportItemResponse toEmergencyReportItemResponse(EmergencyReport emergencyReport) {
        DispatchInfoResponse dispatchInfo = toDispatchInfoResponse(emergencyReport.getDispatch());

        return new EmergencyReportItemResponse(
                emergencyReport.getId(),
                emergencyReport.getIsCompleted(),
                dispatchInfo,
                emergencyReport.getCreatedAt()
        );
    }

    public ParamedicEmergencyReportResponse toParamedicEmergencyReportResponse(Paramedic paramedic, List<EmergencyReport> emergencyReports) {
        ParamedicInfoResponse paramedicInfo = toParamedicInfoResponse(paramedic);

        // 모든 보고서를 EmergencyReportItemResponse로 변환
        List<EmergencyReportItemResponse> reportItems = emergencyReports.stream()
                .map(this::toEmergencyReportItemResponse)
                .collect(Collectors.toList());

        return new ParamedicEmergencyReportResponse(
                paramedicInfo,
                reportItems
        );
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
                dispatch.getDisasterType(),
                dispatch.getDisasterSubtype(),
                dispatch.getReporterName(),
                dispatch.getLocationAddress(),
                dispatch.getDate()
        );
    }

    public EmergencyReportSummaryResponse toEmergencyReportSummaryResponse(EmergencyReport emergencyReport) {
        ParamedicInfoSimple paramedicInfo = toParamedicInfoSimple(emergencyReport.getParamedic());
        DispatchInfoSimple dispatchInfo = toDispatchInfoSimple(emergencyReport.getDispatch());
        String patientName = extractPatientName(emergencyReport);

        return new EmergencyReportSummaryResponse(
                emergencyReport.getId(),
                emergencyReport.getIsCompleted(),
                patientName,
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

    /**
     * EmergencyReport에서 환자 이름 추출
     * ReportSection의 PATIENT_INFO 타입에서 patientInfo.patient.name 경로로 추출
     *
     * @param emergencyReport 구급일지
     * @return 환자 이름 (없거나 null인 경우 "환자 이름 정보 없음")
     */
    private String extractPatientName(EmergencyReport emergencyReport) {
        try {
            // PATIENT_INFO 타입의 ReportSection 조회
            ReportSection patientInfoSection = reportSectionRepository
                    .findByEmergencyReportAndType(emergencyReport, ReportSectionType.PATIENT_INFO)
                    .orElse(null);

            if (patientInfoSection == null) {
                log.debug("PATIENT_INFO 섹션을 찾을 수 없음 - EmergencyReport ID: {}", emergencyReport.getId());
                return "환자 이름 정보 없음";
            }

            JsonNode data = patientInfoSection.getData();
            if (data == null) {
                log.debug("PATIENT_INFO 섹션의 데이터가 null - EmergencyReport ID: {}", emergencyReport.getId());
                return "환자 이름 정보 없음";
            }

            // JSON 경로: patientInfo.patient.name
            JsonNode patientInfoNode = data.get("patientInfo");
            if (patientInfoNode == null || patientInfoNode.isNull()) {
                log.debug("patientInfo 노드를 찾을 수 없음 - EmergencyReport ID: {}", emergencyReport.getId());
                return "환자 이름 정보 없음";
            }

            JsonNode patientNode = patientInfoNode.get("patient");
            if (patientNode == null || patientNode.isNull()) {
                log.debug("patient 노드를 찾을 수 없음 - EmergencyReport ID: {}", emergencyReport.getId());
                return "환자 이름 정보 없음";
            }

            JsonNode nameNode = patientNode.get("name");
            if (nameNode == null || nameNode.isNull()) {
                log.debug("name 필드가 null - EmergencyReport ID: {}", emergencyReport.getId());
                return "환자 이름 정보 없음";
            }

            String patientName = nameNode.asText();
            if (patientName == null || patientName.isEmpty() || patientName.equals("null")) {
                log.debug("name 필드가 비어있거나 null 문자열 - EmergencyReport ID: {}", emergencyReport.getId());
                return "환자 이름 정보 없음";
            }

            log.debug("환자 이름 추출 성공 - EmergencyReport ID: {}, 환자 이름: {}", emergencyReport.getId(), patientName);
            return patientName;

        } catch (Exception e) {
            log.error("환자 이름 추출 중 오류 발생 - EmergencyReport ID: {}", emergencyReport.getId(), e);
            return "환자 이름 정보 없음";
        }
    }
}
