package com.ssairen.domain.firestation.controller;

import com.ssairen.config.swagger.annotation.ApiInternalServerError;
import com.ssairen.config.swagger.annotation.ApiUnauthorizedError;
import com.ssairen.domain.firestation.dto.ParamedicInfo;
import com.ssairen.domain.firestation.dto.ParamedicRegisterRequest;
import com.ssairen.domain.firestation.dto.ParamedicRegisterResponse;
import com.ssairen.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Tag(name = "Paramedics", description = "구급대원 관련 API")
public interface ParamedicApi {

    @Operation(
            summary = "구급대원 회원가입",
            description = "새로운 구급대원을 등록합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "회원가입 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ParamedicRegisterResponse.class),
                    examples = @ExampleObject(
                            name = "회원가입 성공",
                            value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "id": 5,
                                        "studentNumber": "20240005",
                                        "name": "김철수",
                                        "rank": "소방사",
                                        "status": "ACTIVE",
                                        "fireStateId": 1,
                                        "fireStateName": "강남소방서"
                                      },
                                      "message": "회원가입이 완료되었습니다.",
                                      "timestamp": "2025-10-29T12:00:00+09:00"
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
                                            "field": "studentNumber",
                                            "message": "학번은 필수 입력 항목입니다.",
                                            "rejectedValue": null
                                          }
                                        ]
                                      },
                                      "status": 400,
                                      "timestamp": "2025-10-29T12:00:00+09:00"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "소방서를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "소방서 없음",
                            value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "FIRE_STATION_NOT_FOUND",
                                        "message": "소방서를 찾을 수 없습니다."
                                      },
                                      "status": 404,
                                      "timestamp": "2025-10-29T12:00:00+09:00"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "이미 존재하는 학번",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "학번 중복",
                            value = """
                                    {
                                      "success": false,
                                      "error": {
                                        "code": "PARAMEDIC_ALREADY_EXISTS",
                                        "message": "이미 존재하는 학번입니다."
                                      },
                                      "status": 409,
                                      "timestamp": "2025-10-29T12:00:00+09:00"
                                    }
                                    """
                    )
            )
    )
    @ApiInternalServerError
    ResponseEntity<ApiResponse<ParamedicRegisterResponse>> register(ParamedicRegisterRequest request);

    @Operation(
            summary = "전체 구급대원 조회",
            description = "전체 구급대원 리스트를 조회합니다."
//            security =
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "구급대원 리스트 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = ParamedicInfo.class)),
                    examples = @ExampleObject(
                            name = "전체 구급대원 조회 성공",
                            value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                          "paramedicId": 1,
                                          "name": "김철수",
                                          "rank": "소방사",
                                          "status": "ACTIVE",
                                          "studentNumber": "20240001",
                                          "fireStateId": 1
                                        },
                                        {
                                          "paramedicId": 2,
                                          "name": "이영희",
                                          "rank": "소방교",
                                          "status": "ACTIVE",
                                          "studentNumber": "20240002",
                                          "fireStateId": 1
                                        },
                                        {
                                          "paramedicId": 3,
                                          "name": "박민수",
                                          "rank": "소방장",
                                          "status": "ON_DUTY",
                                          "studentNumber": "20240003",
                                          "fireStateId": 2
                                        }
                                      ],
                                      "message": "전체 구급대원 목록을 조회했습니다.",
                                      "timestamp": "2025-10-23T10:46:20+09:00"
                                    }
                                    """
                    )
            )
    )
    @ApiInternalServerError
    @ApiUnauthorizedError
    ResponseEntity<? extends ApiResponse> getAllParamedics();
}
