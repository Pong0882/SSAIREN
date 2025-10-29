package com.ssairen.domain.hospital.controller;

import com.ssairen.domain.hospital.dto.HospitalSelectionRequest;
import com.ssairen.domain.hospital.dto.HospitalSelectionResponse;
import com.ssairen.domain.hospital.service.HospitalService;
import com.ssairen.global.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
