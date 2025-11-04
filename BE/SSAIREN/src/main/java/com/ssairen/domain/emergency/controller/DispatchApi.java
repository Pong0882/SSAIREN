package com.ssairen.domain.emergency.controller;

import com.ssairen.config.swagger.annotation.ApiInternalServerError;
import com.ssairen.config.swagger.annotation.ApiUnauthorizedError;
import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import com.ssairen.domain.emergency.dto.DispatchCreateResponse;
import com.ssairen.domain.emergency.dto.DispatchListQueryRequest;
import com.ssairen.domain.emergency.dto.DispatchListResponse;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

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
                                        "fireStateId": 1,
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
                                            "field": "fireStateId",
                                            "message": "소방서 ID는 필수 입력 항목입니다."
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
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "리소스 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "소방서 없음",
                            value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "FIRE_STATE_NOT_FOUND",
                                        "message": "존재하지 않는 소방서입니다."
                                      },
                                      "status": 404,
                                      "timestamp": "2023-11-13T09:16:00+09:00"
                                    }
                                    """
                    )
            )
    )
    @ApiUnauthorizedError
    @ApiInternalServerError
    ResponseEntity<? extends ApiResponse> createDispatch(@Valid DispatchCreateRequest request);

    @Operation(
            summary = "소방서 전체 출동 목록 조회",
            description = "현재 로그인한 구급대원이 소속된 소방서의 출동 내역을 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "출동 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DispatchListResponse.class),
                    examples = @ExampleObject(
                            name = "출동 목록 조회 성공",
                            value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "fire_state": {
                                          "id": 1,
                                          "name": "서울소방서"
                                        },
                                        "dispatches": [
                                          {
                                            "id": 123,
                                            "disasterNumber": "CB0000000662",
                                            "disasterType": "화재",
                                            "disasterSubtype": "고층건물(3층이상,아파트)",
                                            "reporterName": "홍길동",
                                            "reporterPhone": "010-1234-5678",
                                            "locationAddress": "충청북도 청주시 서원구 오창읍 양청리 45 송학타워 오창캠퍼스",
                                            "incidentDescription": "3층 주방에서 화재 발생",
                                            "dispatchLevel": "실전",
                                            "dispatchOrder": 1,
                                            "dispatchStation": "서울소방서",
                                            "date": "2023-11-13T09:16:00Z"
                                          },
                                          {
                                            "id": 122,
                                            "disasterNumber": "CB0000000661",
                                            "disasterType": "구급",
                                            "disasterSubtype": "교통사고",
                                            "reporterName": "김철수",
                                            "reporterPhone": "010-9876-5432",
                                            "locationAddress": "서울시 강남구 테헤란로 123",
                                            "incidentDescription": "차량 2대 추돌사고, 부상자 1명",
                                            "dispatchLevel": "실전",
                                            "dispatchOrder": 1,
                                            "dispatchStation": "서울소방서",
                                            "date": "2023-11-12T14:30:00Z"
                                          }
                                        ],
                                        "pagination": {
                                          "next_cursor": "eyJpZCI6MTIyLCJkYXRlIjoiMjAyMy0xMS0xMlQxNDozMDowMFoifQ",
                                          "has_more": true
                                        }
                                      },
                                      "timestamp": "2023-11-13T10:00:00Z"
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
                                            "field": "limit",
                                            "message": "limit은 1에서 100 사이의 값이어야 합니다."
                                          }
                                        ]
                                      },
                                      "status": 400,
                                      "timestamp": "2023-11-13T10:00:00Z"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "리소스 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "소방서 없음",
                            value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "FIRE_STATE_NOT_FOUND",
                                        "message": "존재하지 않는 소방서입니다."
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
    ResponseEntity<? extends ApiResponse> getDispatchList(
            @Parameter(description = """
                    조회 조건
                    - cursor: 다음 페이지 커서 (첫 요청 시 생략, 이후 응답의 next_cursor 값 사용)
                    - limit: 페이지당 개수 (기본값: 10, 최소: 1, 최대: 100)
                    """)
            @Valid
            DispatchListQueryRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    );
}
