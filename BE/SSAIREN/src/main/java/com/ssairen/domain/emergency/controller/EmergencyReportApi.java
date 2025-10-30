package com.ssairen.domain.emergency.controller;

import com.ssairen.config.swagger.annotation.ApiInternalServerError;
import com.ssairen.config.swagger.annotation.ApiUnauthorizedError;
import com.ssairen.domain.emergency.dto.EmergencyReportCreateRequest;
import com.ssairen.domain.emergency.dto.EmergencyReportCreateResponse;
import com.ssairen.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
}
