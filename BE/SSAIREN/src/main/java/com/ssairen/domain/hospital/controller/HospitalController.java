package com.ssairen.domain.hospital.controller;

import com.ssairen.domain.hospital.dto.HospitalResponseDto;
import com.ssairen.domain.hospital.dto.HospitalResponseRequest;
import com.ssairen.domain.hospital.dto.HospitalSelectionRequest;
import com.ssairen.domain.hospital.dto.HospitalSelectionResponse;
import com.ssairen.domain.hospital.service.HospitalService;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 병원 Controller
 */
@RestController
@RequestMapping("/api/hospital-selection")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    /**
     * 병원 이송 요청 생성
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse<HospitalSelectionResponse>> createHospitalSelectionRequest(
            @Valid @RequestBody HospitalSelectionRequest request
    ) {
        HospitalSelectionResponse response = hospitalService.createHospitalSelectionRequest(request);
        return ResponseEntity.ok(
                ApiResponse.success(response, "병원 이송 요청이 성공적으로 전송되었습니다.")
        );
    }

    /**
     * 병원 이송 요청에 응답
     *
     * @param hospitalSelectionId 병원 선택 ID
     * @param request 병원 응답 요청 (status: ACCEPTED, REJECTED, CALLREQUEST)
     * @param principal 현재 로그인한 병원 정보
     * @return 병원 응답 결과
     */
    @PatchMapping("/{hospitalSelectionId}")
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
}
