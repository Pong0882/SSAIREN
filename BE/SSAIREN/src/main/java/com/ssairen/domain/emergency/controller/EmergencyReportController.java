package com.ssairen.domain.emergency.controller;

import com.ssairen.domain.emergency.dto.EmergencyReportCreateRequest;
import com.ssairen.domain.emergency.dto.EmergencyReportCreateResponse;
import com.ssairen.domain.emergency.service.EmergencyReportService;
import com.ssairen.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emergency-reports")
@RequiredArgsConstructor
public class EmergencyReportController implements EmergencyReportApi {

    private final EmergencyReportService emergencyReportService;

    @Override
    public ResponseEntity<ApiResponse<EmergencyReportCreateResponse>> createEmergencyReport(
            @Valid @RequestBody EmergencyReportCreateRequest request) {
        EmergencyReportCreateResponse response = emergencyReportService.createEmergencyReport(request);
        return ResponseEntity.ok(ApiResponse.success(response, "구급일지가 생성되었습니다."));
    }
}
