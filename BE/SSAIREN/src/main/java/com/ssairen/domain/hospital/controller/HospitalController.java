package com.ssairen.domain.hospital.controller;

import com.ssairen.domain.hospital.dto.*;
import com.ssairen.domain.hospital.service.HospitalService;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 병원 Controller
 * 병원 이송 요청 생성, 응답, 조회 기능 제공
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HospitalController implements HospitalApi {

    private final HospitalService hospitalService;

    @Override
    @PostMapping("/hospital-selection/request")
    public ResponseEntity<ApiResponse<HospitalSelectionResponse>> createHospitalSelectionRequest(
            @Valid @RequestBody HospitalSelectionRequest request
    ) {
        HospitalSelectionResponse response = hospitalService.createHospitalSelectionRequest(request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "병원 이송 요청이 성공적으로 전송되었습니다.")
        );
    }

    @Override
    @PatchMapping("/hospital-selection/{hospitalSelectionId}")
    public ResponseEntity<ApiResponse<HospitalResponseDto>> respondToRequest(
            @PathVariable Integer hospitalSelectionId,
            @Valid @RequestBody HospitalResponseRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        HospitalResponseDto response = hospitalService.respondToRequest(
                hospitalSelectionId,
                request,
                principal.getId()
        );

        String message = switch (response.getStatus()) {
            case ACCEPTED -> "환자 수용이 승인되었습니다.";
            case REJECTED -> "환자 수용이 거절되었습니다.";
            case CALLREQUEST -> "전화 요망으로 응답되었습니다.";
            default -> "병원 응답이 처리되었습니다.";
        };

        return ResponseEntity.ok(ApiResponse.success(response, message));
    }

    @Override
    @GetMapping("/hospitals/{hospitalId}/requests/pending")
    public ResponseEntity<ApiResponse<List<HospitalRequestMessage>>> getPendingRequests(
            @PathVariable Integer hospitalId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<HospitalRequestMessage> requests = hospitalService.getPendingRequests(
                hospitalId,
                principal.getId()
        );

        return ResponseEntity.ok(
                ApiResponse.success(requests, "PENDING 상태인 요청 목록을 조회했습니다.")
        );
    }

    @Override
    @GetMapping("/hospitals/{hospitalId}/patients")
    public ResponseEntity<ApiResponse<List<AcceptedPatientDto>>> getAcceptedPatients(
            @PathVariable Integer hospitalId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<AcceptedPatientDto> patients = hospitalService.getAcceptedPatients(
                hospitalId,
                principal.getId()
        );

        return ResponseEntity.ok(
                ApiResponse.success(patients, "수용한 환자 목록을 조회했습니다.")
        );
    }
}
