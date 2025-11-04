package com.ssairen.domain.emergency.controller;

import com.ssairen.domain.emergency.dto.EmergencyReportCreateResponse;
import com.ssairen.domain.emergency.dto.FireStateEmergencyReportsResponse;
import com.ssairen.domain.emergency.dto.ParamedicEmergencyReportResponse;
import com.ssairen.domain.emergency.dto.ReportSectionCreateResponse;
import com.ssairen.domain.emergency.dto.ReportSectionUpdateRequest;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.service.EmergencyReportService;
import com.ssairen.domain.emergency.service.ReportSectionService;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @PostMapping("/{dispatch_id}")
    public ResponseEntity<ApiResponse<EmergencyReportCreateResponse>> createEmergencyReport(
            @PathVariable("dispatch_id") @Positive(message = "출동지령 ID는 양의 정수여야 합니다.") Long dispatchId,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        EmergencyReportCreateResponse response = emergencyReportService.createEmergencyReport(dispatchId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "구급일지가 생성되었습니다."));
    }

    @Override
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ParamedicEmergencyReportResponse>> getEmergencyReportsByParamedic(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        ParamedicEmergencyReportResponse response = emergencyReportService.getEmergencyReportsByParamedic(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "구급대원이 작성한 보고서 조회를 완료하였습니다."));
    }

    @Override
    @PostMapping("/{emergencyReportId}/sections/{type}")
    public ResponseEntity<ApiResponse<ReportSectionCreateResponse>> createReportSection(
            @PathVariable("emergencyReportId") @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @PathVariable("type") ReportSectionType type) {
        ReportSectionCreateResponse response = reportSectionService.createReportSection(emergencyReportId, type);
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

    @Override
    @GetMapping("/fire-state")
    public ResponseEntity<ApiResponse<List<FireStateEmergencyReportsResponse>>> getEmergencyReportsByFireState(
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        List<FireStateEmergencyReportsResponse> response = emergencyReportService.getEmergencyReportsByFireState(principal.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "소방서 보고서 조회를 완료하였습니다."));
    }

    @Override
    @PatchMapping("/{emergencyReportId}/sections/{type}")
    public ResponseEntity<ApiResponse<ReportSectionCreateResponse>> updateReportSection(
            @PathVariable("emergencyReportId") @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @PathVariable("type") ReportSectionType type,
            @Valid @RequestBody ReportSectionUpdateRequest request) {
        ReportSectionCreateResponse response = reportSectionService.updateReportSection(emergencyReportId, type, request);
        return ResponseEntity.ok(ApiResponse.success(response, "구급일지 섹션이 수정되었습니다."));
    }
}
