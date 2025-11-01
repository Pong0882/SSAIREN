package com.ssairen.domain.emergency.controller;

import com.ssairen.config.swagger.annotation.ApiInternalServerError;
import com.ssairen.config.swagger.annotation.ApiUnauthorizedError;
import com.ssairen.domain.emergency.dto.EmergencyReportCreateRequest;
import com.ssairen.domain.emergency.dto.EmergencyReportCreateResponse;
import com.ssairen.domain.emergency.dto.ParamedicEmergencyReportResponse;
import com.ssairen.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Emergency Report", description = "구급일지 API")
public interface EmergencyReportApi {

    @Operation(
            summary = "구급일지 생성",
            description = "출동지령에 구급대원을 배정하고 구급일지를 생성합니다."
//            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "구급일지 생성 요청",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation =EmergencyReportCreateRequest.class),
                    examples = @ExampleObject(
                            name = "구급일지 생성 요청 예시",
                            value = """
                                    {
                                        "dispatchId": 1,
                                        "paramedicId": 1,
                                        "fireStateId": 1
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "구급일지 생성 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EmergencyReportCreateResponse.class),
                    examples = @ExampleObject(
                            name = "구급일지 생성 성공",
                            value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "emergencyReportId": 123,
                                        "paramedicInfo": {
                                          "paramedicId": 1,
                                          "name": "홍길동",
                                          "rank": "소방사",
                                          "fireStateId": 1,
                                          "studentNumber": "20240001"
                                        },
                                        "dispatchInfo": {
                                          "dispatchId": 1,
                                          "disasterNumber": "CB0000000662",
                                          "disasterType": "화재",
                                          "locationAddress": "충청북도 청주시 법무구 오창읍 양청상 45 송학타워 오창캠퍼스",
                                          "date": "2023-11-13T09:16:00Z"
                                        },
                                        "createdAt": "2023-11-13T09:20:00Z"
                                      },
                                      "message": "구급일지가 생성되었습니다.",
                                      "timestamp": "2023-11-13T09:20:00Z"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "유효성 검증 실패",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "유효성 검증 실패",
                            value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "VALIDATION_ERROR",
                                        "message": "입력 정보가 올바르지 않습니다.",
                                        "details": [
                                          {
                                            "field": "dispatchId",
                                            "message": "출동지령 ID는 필수 입력 항목입니다."
                                          }
                                        ]
                                      },
                                      "status": 400,
                                      "timestamp": "2023-11-13T09:20:00Z"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "리소스를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "리소스 없음",
                            value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "DISPATCH_NOT_FOUND",
                                        "message": "존재하지 않는 출동지령입니다."
                                      },
                                      "status": 404,
                                      "timestamp": "2023-11-13T09:20:00Z"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "구급일지 중복 생성",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "구급일지 중복",
                            value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "EMERGENCY_REPORT_ALREADY_EXISTS",
                                        "message": "해당 출동에 대한 구급일지가 이미 존재합니다."
                                      },
                                      "status": 409,
                                      "timestamp": "2023-11-13T09:20:00Z"
                                    }
                                    """
                    )
            )
    )
    @ApiUnauthorizedError
    @ApiInternalServerError
    @PostMapping
    ResponseEntity<? extends ApiResponse> createEmergencyReport(
            @Valid @RequestBody EmergencyReportCreateRequest request
    );

    @Operation(
            summary = "특정 구급대원 보고서 조회",
            description = "특정 구급대원이 작성한 모든 보고서를 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "구급대원 보고서 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "구급대원 보고서 조회 성공",
                            value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "paramedicInfo": {
                                            "paramedicId": 1,
                                            "name": "김철수",
                                            "studentNumber": "20240001"
                                          },
                                          "dispatchInfo": {
                                            "dispatchId": 1,
                                            "disasterNumber": "C000000065",
                                            "date": "2025-10-23T10:46:20+09:00",
                                            "fireStateInfo": {
                                                "id": 1,
                                                "name": "강남소방서"
                                            }
                                          }
                                        }
                                      ],
                                      "message": "구급대원이 작성한 보고서 조회를 완료하였습니다.",
                                      "timestamp": "2025-10-23T10:46:20+09:00"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "유효성 검증 실패",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "유효성 검증 실패",
                            value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "VALIDATION_ERROR",
                                        "message": "입력 정보가 올바르지 않습니다.",
                                        "details": [
                                          {
                                            "field": "",
                                            "message": ""
                                          }
                                        ]
                                      },
                                      "status": 400,
                                      "timestamp": "2023-11-13T09:20:00Z"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "구급대원을 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "구급대원 없음",
                            value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "PARAMEDIC_NOT_FOUND",
                                        "message": "존재하지 않는 구급대원입니다."
                                      },
                                      "status": 404,
                                      "timestamp": "2023-11-13T10:00:00Z"
                                    }
                                    """
                    )
            )
    )
    @ApiUnauthorizedError
    @ApiInternalServerError
    @GetMapping("/{paramedicId}")
    ResponseEntity<ApiResponse<List<ParamedicEmergencyReportResponse>>> getEmergencyReportsByParamedic(
            @Parameter(description = "구급대원 ID", required = true, example = "1")
            @PathVariable("paramedicId") @Positive(message = "구급대원 ID는 양의 정수여야 합니다.") Integer paramedicId
    );
}
