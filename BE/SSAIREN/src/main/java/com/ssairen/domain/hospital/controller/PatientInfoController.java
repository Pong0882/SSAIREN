package com.ssairen.domain.hospital.controller;

import com.ssairen.domain.hospital.dto.PatientInfoCreateRequest;
import com.ssairen.domain.hospital.dto.PatientInfoResponse;
import com.ssairen.domain.hospital.service.PatientInfoService;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 환자 정보 Controller
 */
@Tag(name = "Patient Info", description = "환자 정보 API")
@RestController
@RequestMapping("/api/patient-info")
@RequiredArgsConstructor
@Validated
public class PatientInfoController {

    private final PatientInfoService patientInfoService;

    /**
     * 환자 정보 생성
     */
    @Operation(
            summary = "환자 정보 생성",
            description = "구급일지에 대한 환자 정보를 생성합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "환자 정보 생성 성공",
                    content = @Content(schema = @Schema(implementation = PatientInfoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "구급일지를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "이미 환자 정보가 존재함",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<PatientInfoResponse>> createPatientInfo(
            @Valid @RequestBody PatientInfoCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        PatientInfoResponse response = patientInfoService.createPatientInfo(request, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.success(response, "환자 정보가 생성되었습니다.")
        );
    }

    /**
     * 환자 정보 조회
     */
    @Operation(
            summary = "환자 정보 조회",
            description = "구급일지 ID로 환자 정보를 조회합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "환자 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = PatientInfoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "환자 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/{emergencyReportId}")
    public ResponseEntity<ApiResponse<PatientInfoResponse>> getPatientInfo(
            @Parameter(description = "구급일지 ID", required = true, example = "1")
            @PathVariable @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        PatientInfoResponse response = patientInfoService.getPatientInfo(emergencyReportId, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.success(response, "환자 정보 조회가 완료되었습니다.")
        );
    }

    /**
     * 환자 정보 수정
     */
    @Operation(
            summary = "환자 정보 수정",
            description = "구급일지에 대한 환자 정보를 수정합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "환자 정보 수정 성공",
                    content = @Content(schema = @Schema(implementation = PatientInfoResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "환자 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PutMapping("/{emergencyReportId}")
    public ResponseEntity<ApiResponse<PatientInfoResponse>> updatePatientInfo(
            @Parameter(description = "구급일지 ID", required = true, example = "1")
            @PathVariable @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @Valid @RequestBody PatientInfoCreateRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        PatientInfoResponse response = patientInfoService.updatePatientInfo(emergencyReportId, request, principal.getId());
        return ResponseEntity.ok(
                ApiResponse.success(response, "환자 정보가 수정되었습니다.")
        );
    }
}
