package com.ssairen.domain.firestation.controller;

import com.ssairen.config.swagger.annotation.ApiInternalServerError;
import com.ssairen.config.swagger.annotation.ApiUnauthorizedError;
import com.ssairen.domain.firestation.dto.ParamedicInfo;
import com.ssairen.domain.firestation.dto.ParamedicLoginRequest;
import com.ssairen.domain.firestation.dto.ParamedicLoginResponse;
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
            summary = "구급대원 로그인",
            description = "학번과 비밀번호로 구급대원 로그인을 수행합니다. (JWT는 추후 구현 예정)"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ParamedicLoginResponse.class),
                    examples = @ExampleObject(
                            name = "로그인 성공",
                            value = """
                                    {
                                      "success": true,
                                      "data": {
                                        "accessToken": null,
                                        "refreshToken": null,
                                        "paramedic": {
                                          "id": 1,
                                          "studentNumber": "20240001",
                                          "name": "홍길동",
                                          "rank": "FIREFIGHTER",
                                          "status": "ACTIVE",
                                          "fireStateId": 1,
                                          "fireStateName": "서울소방서"
                                        }
                                      },
                                      "message": "로그인에 성공했습니다.",
                                      "timestamp": "2025-10-29T14:30:00+09:00"
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
                                      "timestamp": "2025-10-29T14:30:00+09:00"
                                    }
                                    """
                    )
            )
    )
    @ApiUnauthorizedError
    @ApiInternalServerError
    ResponseEntity<ApiResponse<ParamedicLoginResponse>> login(ParamedicLoginRequest request);

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
