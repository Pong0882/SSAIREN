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

    @Override
    @GetMapping("/hospitals/{hospitalId}/patients/{emergencyReportId}")
    public ResponseEntity<ApiResponse<PatientInfoDto>> getPatientDetail(
            @PathVariable Integer hospitalId,
            @PathVariable Long emergencyReportId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        PatientInfoDto patientInfo = hospitalService.getPatientDetail(
                hospitalId,
                emergencyReportId,
                principal.getId()
        );

        return ResponseEntity.ok(
                ApiResponse.success(patientInfo, "환자 상세 정보를 조회했습니다.")
        );
    }

    @Override
    @PutMapping("/hospitals/{hospitalId}/patients/{emergencyReportId}")
    public ResponseEntity<ApiResponse<PatientInfoDto>> updatePatientInfo(
            @PathVariable Integer hospitalId,
            @PathVariable Long emergencyReportId,
            @Valid @RequestBody UpdatePatientInfoRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        PatientInfoDto updatedPatientInfo = hospitalService.updatePatientInfo(
                hospitalId,
                emergencyReportId,
                request,
                principal.getId()
        );

        return ResponseEntity.ok(
                ApiResponse.success(updatedPatientInfo, "환자 정보를 수정했습니다.")
        );
    }

    @Override
    @PatchMapping("/hospitals/{hospitalId}/patients/{emergencyReportId}/arrival")
    public ResponseEntity<ApiResponse<Void>> markPatientAsArrived(
            @PathVariable Integer hospitalId,
            @PathVariable Long emergencyReportId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        hospitalService.markPatientAsArrived(
                hospitalId,
                emergencyReportId,
                principal.getId()
        );

        return ResponseEntity.ok(
                ApiResponse.success("환자가 내원 완료 처리되었습니다.")
        );
    }

    @Override
    @GetMapping("/emergency-reports/{emergency_report_id}/hospital-selections")
    public ResponseEntity<ApiResponse<HospitalSelectionStatusResponse>> getHospitalSelectionStatus(
            @PathVariable("emergency_report_id") Long emergencyReportId
    ) {
        HospitalSelectionStatusResponse response = hospitalService.getHospitalSelectionStatus(emergencyReportId);

        return ResponseEntity.ok(
                ApiResponse.success(response, "병원 선택 상태를 조회했습니다.")
        );
    }
}
