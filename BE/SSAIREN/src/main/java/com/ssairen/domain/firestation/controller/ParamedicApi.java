package com.ssairen.domain.firestation.controller;

import com.ssairen.config.swagger.annotation.ApiInternalServerError;
import com.ssairen.config.swagger.annotation.ApiUnauthorizedError;
import com.ssairen.domain.firestation.dto.ParamedicInfo;
import com.ssairen.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Paramedics", description = "구급대원 관련 API")
public interface ParamedicApi {

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
