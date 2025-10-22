package com.ssairen.domain.firestation.controller;

import com.ssairen.config.swagger.annotation.ApiInternalServerError;
import com.ssairen.config.swagger.annotation.ApiUnauthorizedError;
import com.ssairen.domain.firestation.dto.ParamedicListResponse;
import com.ssairen.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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
                    schema = @Schema(implementation = ParamedicListResponse.class),
                    examples = @ExampleObject(
                            name = "전체 구급대원 조회 성공",
                            value = """
                                    {
                                      "code": "",
                                      "message": "",
                                      "status": 200,
                                      "timestamp": "2025-09-07T12:20:00Z",
                                      "data": {
                                        "paramedics": [
                                          {
                                            "schoolId": 1,
                                            "name": "가락고등학교",x
                                            "city": "서울특별시",
                                            "totalTax": 67300
                                          },
                                        ]
                                      }
                                    
                                    }
                                    """
                    )
            )
    )
    @ApiInternalServerError
    @ApiUnauthorizedError
    ResponseEntity<? extends ApiResponse> getAllParamedics();
}
