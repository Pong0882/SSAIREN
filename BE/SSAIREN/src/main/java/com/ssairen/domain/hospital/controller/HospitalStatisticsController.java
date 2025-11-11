package com.ssairen.domain.hospital.controller;

import com.ssairen.domain.hospital.dto.DisasterTypeStatisticsResponse;
import com.ssairen.domain.hospital.dto.PatientStatisticsResponse;
import com.ssairen.domain.hospital.dto.StatisticsRequest;
import com.ssairen.domain.hospital.dto.TimeStatisticsResponse;
import com.ssairen.domain.hospital.service.HospitalStatisticsService;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 병원 통계 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/hospitals/{hospitalId}/statistics")
@RequiredArgsConstructor
@Validated
@Tag(name = "Hospital Statistics", description = "병원 통계 API")
public class HospitalStatisticsController {

    private final HospitalStatisticsService hospitalStatisticsService;

    @Operation(
            summary = "시간별 통계 조회",
            description = "특정 기간 동안 병원의 요일별, 시간대별 환자 수용 통계를 조회합니다. " +
                    "요일별(월~일), 시간대별(0~23시) 환자 수용 건수를 제공합니다. " +
                    "병원은 본인의 통계만 조회 가능합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = TimeStatisticsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (날짜 형식 오류 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 병원의 통계 조회 시도)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "병원을 찾을 수 없음"
            )
    })
    @PostMapping("/time")
    public ResponseEntity<ApiResponse<TimeStatisticsResponse>> getTimeStatistics(
            @Parameter(description = "병원 ID", required = true)
            @PathVariable Integer hospitalId,

            @Parameter(description = "통계 조회 요청", required = true)
            @Valid @RequestBody StatisticsRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        log.info("🔍 GET /api/hospitals/{}/statistics/time - User ID: {}, Period: {} ~ {}",
                hospitalId, principal.getId(), request.startDate(), request.endDate());

        // 권한 검증: 본인 병원 통계만 조회 가능
        if (!hospitalId.equals(principal.getId())) {
            log.warn("⚠️ Access denied - Requested hospital ID: {}, User ID: {}",
                    hospitalId, principal.getId());
            throw new com.ssairen.global.exception.CustomException(
                    com.ssairen.global.exception.ErrorCode.ACCESS_DENIED,
                    "본인 병원의 통계만 조회할 수 있습니다."
            );
        }

        TimeStatisticsResponse response = hospitalStatisticsService.getTimeStatistics(request, hospitalId);

        return ResponseEntity.ok(
                ApiResponse.success(response, "시간별 통계가 조회되었습니다.")
        );
    }

    @Operation(
            summary = "환자 통계 조회",
            description = "특정 기간 동안 병원의 환자 통계를 조회합니다. " +
                    "성별 분포(남/여), 연령대 분포(10년 단위), 의식 상태 분포(ALERT/VERBAL/PAIN/UNRESPONSIVE)를 제공합니다. " +
                    "병원은 본인의 통계만 조회 가능합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = PatientStatisticsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (날짜 형식 오류 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 병원의 통계 조회 시도)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "병원을 찾을 수 없음"
            )
    })
    @PostMapping("/patient")
    public ResponseEntity<ApiResponse<PatientStatisticsResponse>> getPatientStatistics(
            @Parameter(description = "병원 ID", required = true)
            @PathVariable Integer hospitalId,

            @Parameter(description = "통계 조회 요청", required = true)
            @Valid @RequestBody StatisticsRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        log.info("🔍 GET /api/hospitals/{}/statistics/patient - User ID: {}, Period: {} ~ {}",
                hospitalId, principal.getId(), request.startDate(), request.endDate());

        // 권한 검증: 본인 병원 통계만 조회 가능
        if (!hospitalId.equals(principal.getId())) {
            log.warn("⚠️ Access denied - Requested hospital ID: {}, User ID: {}",
                    hospitalId, principal.getId());
            throw new com.ssairen.global.exception.CustomException(
                    com.ssairen.global.exception.ErrorCode.ACCESS_DENIED,
                    "본인 병원의 통계만 조회할 수 있습니다."
            );
        }

        PatientStatisticsResponse response = hospitalStatisticsService.getPatientStatistics(request, hospitalId);

        return ResponseEntity.ok(
                ApiResponse.success(response, "환자 통계가 조회되었습니다.")
        );
    }

    @Operation(
            summary = "재난 유형별 통계 조회",
            description = "특정 기간 동안 병원의 재난 유형별 환자 수용 통계를 조회합니다. " +
                    "재난 유형별 분포(질병, 교통사고, 추락 등), 재난 세부 유형별 분포를 제공합니다. " +
                    "병원은 본인의 통계만 조회 가능합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = DisasterTypeStatisticsResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (날짜 형식 오류 등)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (다른 병원의 통계 조회 시도)"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "병원을 찾을 수 없음"
            )
    })
    @PostMapping("/disaster-type")
    public ResponseEntity<ApiResponse<DisasterTypeStatisticsResponse>> getDisasterTypeStatistics(
            @Parameter(description = "병원 ID", required = true)
            @PathVariable Integer hospitalId,

            @Parameter(description = "통계 조회 요청", required = true)
            @Valid @RequestBody StatisticsRequest request,

            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        log.info("🔍 GET /api/hospitals/{}/statistics/disaster-type - User ID: {}, Period: {} ~ {}",
                hospitalId, principal.getId(), request.startDate(), request.endDate());

        // 권한 검증: 본인 병원 통계만 조회 가능
        if (!hospitalId.equals(principal.getId())) {
            log.warn("⚠️ Access denied - Requested hospital ID: {}, User ID: {}",
                    hospitalId, principal.getId());
            throw new com.ssairen.global.exception.CustomException(
                    com.ssairen.global.exception.ErrorCode.ACCESS_DENIED,
                    "본인 병원의 통계만 조회할 수 있습니다."
            );
        }

        DisasterTypeStatisticsResponse response = hospitalStatisticsService.getDisasterTypeStatistics(request, hospitalId);

        return ResponseEntity.ok(
                ApiResponse.success(response, "재난 유형별 통계가 조회되었습니다.")
        );
    }
}
