package com.ssairen.domain.emergency.controller;

import com.ssairen.config.swagger.annotation.ApiInternalServerError;
import com.ssairen.config.swagger.annotation.ApiUnauthorizedError;
import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import com.ssairen.domain.emergency.dto.DispatchCreateResponse;
import com.ssairen.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Dispatches", description = "출동 지령 관련 API")
public interface DispatchApi {

    @Operation(
            summary = "출동 지령 생성",
            description = "신고 접수 시 새로운 출동지령을 생성합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "출동 지령 생성 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DispatchCreateResponse.class),
                    examples = @ExampleObject(
                            name = "출동 지령 생성 성공",
                            value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "id": 456,
                                        "disasterNumber": "CB0000000662",
                                        "disasterType": "화재",
                                        "disasterSubtype": "고층건물(3층이상,아파트)",
                                        "reporterName": "김철수",
                                        "reporterPhone": "010-2222-2222",
                                        "locationAddress": "충청북도 청주시 법무구 오창읍 양청상 45 송학타워 오창캠퍼스",
                                        "incidentDescription": "4층 창고에서 화재 발생",
                                        "dispatchLevel": "실전",
                                        "dispatchOrder": 1,
                                        "dispatchStation": "오창현센터",
                                        "date": "2023-11-13T09:16:00Z"
                                      },
                                      "message": "출동지령이 생성되었습니다.",
                                      "timestamp": "2023-11-13T09:16:00+09:00"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "출동 지령 생성 요청",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DispatchCreateRequest.class),
                    examples = @ExampleObject(
                            name = "출동 지령 생성 요청 예시",
                            value = """
                                    {
                                        "disasterNumber": "CB0000000662",
                                        "disasterType": "화재",
                                        "disasterSubtype": "고층건물(3층이상,아파트)",
                                        "reporterName": "김철수",
                                        "reporterPhone": "010-2222-2222",
                                        "locationAddress": "충청북도 청주시 법무구 오창읍 양청상 45 송학타워 오창캠퍼스",
                                        "incidentDescription": "4층 창고에서 화재 발생",
                                        "dispatchLevel": "실전",
                                        "dispatchOrder": 1,
                                        "dispatchStation": "오창현센터",
                                        "date": "2023-11-13T09:16:00Z"
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
                                            "field": "disaster_type",
                                            "message": "재난 분류는 필수 입력 항목입니다."
                                          }
                                        ]
                                      },
                                      "status": 400,
                                      "timestamp": "2023-11-13T09:16:00+09:00"
                                    }
                                    """
                    )
            )
    )
    @ApiUnauthorizedError
    @ApiInternalServerError
    ResponseEntity<? extends ApiResponse> createDispatch(DispatchCreateRequest request);
}
