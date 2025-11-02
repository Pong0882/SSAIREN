package com.ssairen.domain.emergency.controller;

import com.ssairen.domain.emergency.dto.EmergencyReportCreateRequest;
import com.ssairen.domain.emergency.dto.EmergencyReportCreateResponse;
import com.ssairen.domain.emergency.dto.ParamedicEmergencyReportResponse;
import com.ssairen.domain.emergency.dto.ReportSectionCreateRequest;
import com.ssairen.domain.emergency.dto.ReportSectionCreateResponse;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.service.EmergencyReportService;
import com.ssairen.domain.emergency.service.ReportSectionService;
import com.ssairen.global.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emergency-reports")
@RequiredArgsConstructor
@Validated
public class EmergencyReportController implements EmergencyReportApi {

    private final EmergencyReportService emergencyReportService;
    private final ReportSectionService reportSectionService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResponse<EmergencyReportCreateResponse>> createEmergencyReport(
            @Valid @RequestBody EmergencyReportCreateRequest request) {
        EmergencyReportCreateResponse response = emergencyReportService.createEmergencyReport(request);
        return ResponseEntity.ok(ApiResponse.success(response, "구급일지가 생성되었습니다."));
    }

    @Override
    @GetMapping("/{paramedicId}")
    public ResponseEntity<ApiResponse<List<ParamedicEmergencyReportResponse>>> getEmergencyReportsByParamedic(
            @PathVariable("paramedicId") @Positive(message = "구급대원 ID는 양의 정수여야 합니다.") Integer paramedicId) {
        // TODO: JWT에서 구급대원 ID 추출하여 사용하도록 변경 필요
        List<ParamedicEmergencyReportResponse> response = emergencyReportService.getEmergencyReportsByParamedic(paramedicId);
        return ResponseEntity.ok(ApiResponse.success(response, "구급대원이 작성한 보고서 조회를 완료하였습니다."));
    }

    @Override
    @PostMapping("/report-sections")
    public ResponseEntity<ApiResponse<ReportSectionCreateResponse>> createReportSection(
            @Valid @RequestBody ReportSectionCreateRequest request) {
        ReportSectionCreateResponse response = reportSectionService.createReportSection(request);
        return ResponseEntity.ok(ApiResponse.success(response, "구급일지 섹션이 저장되었습니다."));
    }

    @Override
    @GetMapping("/{emergencyReportId}/sections/{type}")
    public ResponseEntity<ApiResponse<ReportSectionCreateResponse>> getReportSection(
            @PathVariable("emergencyReportId") @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @PathVariable("type") ReportSectionType type) {
        ReportSectionCreateResponse response = reportSectionService.getReportSection(emergencyReportId, type);
        return ResponseEntity.ok(ApiResponse.success(response, "구급일지 해당 섹션 조회를 완료하였습니다."));
    }
}
