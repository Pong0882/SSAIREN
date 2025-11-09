package com.ssairen.domain.emergency.controller;

import com.ssairen.config.swagger.annotation.ApiInternalServerError;
import com.ssairen.config.swagger.annotation.ApiUnauthorizedError;
import com.ssairen.domain.emergency.dto.EmergencyReportCreateResponse;
import com.ssairen.domain.emergency.dto.ParamedicEmergencyReportResponse;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Tag(name = "Emergency Report", description = "구급일지 API")
public interface EmergencyReportApi {

    @Operation(
            summary = "구급일지 생성",
            description = "현재 로그인한 구급대원을 출동지령에 배정하고 구급일지를 생성합니다."
//            security = @SecurityRequirement(name = "Bearer Authentication")
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
    @PostMapping("/{dispatch_id}")
    ResponseEntity<? extends ApiResponse> createEmergencyReport(
            @Parameter(description = "출동지령 ID", required = true, example = "1")
            @PathVariable("dispatch_id") @Positive(message = "출동지령 ID는 양의 정수여야 합니다.") Long dispatchId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    );

    @Operation(
            summary = "내 보고서 조회",
            description = "현재 로그인한 구급대원이 작성한 모든 보고서를 조회합니다."
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
                                      "data": {
                                        "paramedicInfo": {
                                          "paramedicId": 1,
                                          "name": "김철수",
                                          "rank": "소방장",
                                          "fireStateId": 1,
                                          "studentNumber": "20240001"
                                        },
                                        "emergencyReports": [
                                          {
                                            "id": 1,
                                            "dispatchInfo": {
                                              "dispatchId": 1,
                                              "disasterNumber": "CB0000000662",
                                              "disasterType": "화재",
                                              "locationAddress": "서울시 강남구",
                                              "date": "2025-10-30T17:43:28+09:00",
                                              "fireStateInfo": {
                                                "id": 1,
                                                "name": "강남소방서"
                                              }
                                            },
                                            "createdAt": "2025-10-30 17:43:28.235931"
                                          },
                                          {
                                            "id": 2,
                                            "dispatchInfo": {
                                              "dispatchId": 2,
                                              "disasterNumber": "CB0000000663",
                                              "disasterType": "구조",
                                              "locationAddress": "서울시 서초구",
                                              "date": "2025-11-01T21:00:41+09:00",
                                              "fireStateInfo": {
                                                "id": 1,
                                                "name": "강남소방서"
                                              }
                                            },
                                            "createdAt": "2025-11-01 21:00:41.284828"
                                          }
                                        ]
                                      },
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
    @GetMapping("/me")
    ResponseEntity<ApiResponse<ParamedicEmergencyReportResponse>> getEmergencyReportsByParamedic(
            @AuthenticationPrincipal CustomUserPrincipal principal
    );

    @Operation(
            summary = "구급일지 섹션 생성",
            description = "구급일지의 특정 섹션을 생성합니다. 섹션 타입에 맞는 스켈레톤 데이터가 자동으로 생성됩니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "구급일지 섹션 생성 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "환자 정보",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 789,
                                                    "emergencyReportId": 123,
                                                    "type": "PATIENT_INFO",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "patientInfo": {
                                                            "reporter": {
                                                                "phone": null,
                                                                "reportMethod": null
                                                            },
                                                            "patient": {
                                                                "name": null,
                                                                "gender": null,
                                                                "ageYears": null,
                                                                "birthDate": null,
                                                                "address": null
                                                            },
                                                            "guardian": {
                                                                "name": null,
                                                                "relation": null,
                                                                "phone": null
                                                            },
                                                            "incidentLocation": {
                                                                "text": null
                                                            },
                                                            "createdAt": null,
                                                            "updatedAt": null
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 저장되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "구급 출동",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 790,
                                                    "emergencyReportId": 123,
                                                    "type": "DISPATCH",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "dispatch": {
                                                            "reportDate": null,
                                                            "reportTime": null,
                                                            "departureTime": null,
                                                            "arrivalSceneTime": null,
                                                            "contactTime": null,
                                                            "distanceKm": null,
                                                            "departureSceneTime": null,
                                                            "arrivalHospitalTime": null,
                                                            "returnTime": null,
                                                            "dispatchType": null,
                                                            "sceneLocation": {
                                                                "primary": null,
                                                                "detail": null
                                                            },
                                                            "symptoms": {
                                                                "pain": null,
                                                                "trauma": null,
                                                                "otherSymptoms": null
                                                            },
                                                            "createdAt": null,
                                                            "updatedAt": null
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 저장되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 발생 유형",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 791,
                                                    "emergencyReportId": 123,
                                                    "type": "INCIDENT_TYPE",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "incidentType": {
                                                            "category": null,
                                                            "medicalHistory": {
                                                                "hasHistory": null,
                                                                "details": null,
                                                                "notes": null
                                                            },
                                                            "externalCause": {
                                                                "type": null,
                                                                "subType": null,
                                                                "injurySeverity": null
                                                            },
                                                            "trauma": {
                                                                "mainCause": null,
                                                                "notes": null
                                                            },
                                                            "legalSuspicion": {
                                                                "isSuspected": null,
                                                                "actions": null,
                                                                "notes": null
                                                            },
                                                            "other": {
                                                                "category": null,
                                                                "notes": null
                                                            },
                                                            "createdAt": null,
                                                            "updatedAt": null
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 저장되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 평가",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 792,
                                                    "emergencyReportId": 123,
                                                    "type": "ASSESSMENT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "patientAssessment": {
                                                            "consciousness": {
                                                                "first": {
                                                                    "time": null,
                                                                    "state": null
                                                                },
                                                                "second": {
                                                                    "time": null,
                                                                    "state": null
                                                                }
                                                            },
                                                            "pupilReaction": {
                                                                "left": {
                                                                    "status": null,
                                                                    "reaction": null
                                                                },
                                                                "right": {
                                                                    "status": null,
                                                                    "reaction": null
                                                                }
                                                            },
                                                            "vitalSigns": {
                                                                "first": {
                                                                    "time": null,
                                                                    "bloodPressure": null,
                                                                    "pulse": null,
                                                                    "respiration": null,
                                                                    "temperature": null,
                                                                    "spo2": null,
                                                                    "bloodSugar": null
                                                                },
                                                                "second": {
                                                                    "time": null,
                                                                    "bloodPressure": null,
                                                                    "pulse": null,
                                                                    "respiration": null,
                                                                    "temperature": null,
                                                                    "spo2": null,
                                                                    "bloodSugar": null
                                                                }
                                                            },
                                                            "patientLevel": null,
                                                            "notes": null,
                                                            "createdAt": null,
                                                            "updatedAt": null
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 저장되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "응급 처치",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 793,
                                                    "emergencyReportId": 123,
                                                    "type": "TREATMENT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "emergencyTreatment": {
                                                            "airwayManagement": {
                                                                "methods": null,
                                                                "notes": null
                                                            },
                                                            "oxygenTherapy": {
                                                                "applied": null,
                                                                "flowRateLpm": null,
                                                                "device": null
                                                            },
                                                            "cpr": {
                                                                "performed": null,
                                                                "type": null,
                                                                "aed": {
                                                                    "used": null,
                                                                    "shock": null,
                                                                    "monitoring": null
                                                                }
                                                            },
                                                            "bleedingControl": {
                                                                "methods": null,
                                                                "notes": null
                                                            },
                                                            "woundCare": {
                                                                "types": null,
                                                                "notes": null
                                                            },
                                                            "delivery": {
                                                                "performed": null,
                                                                "time": null,
                                                                "babyCondition": null
                                                            },
                                                            "notes": null,
                                                            "createdAt": null,
                                                            "updatedAt": null
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 저장되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "의료 지도",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 794,
                                                    "emergencyReportId": 123,
                                                    "type": "MEDICAL_GUIDANCE",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "medicalGuidance": {
                                                            "contactStatus": null,
                                                            "requestTime": null,
                                                            "guidanceAgency": {
                                                                "type": null,
                                                                "name": null
                                                            },
                                                            "guidanceDoctor": {
                                                                "name": null
                                                            },
                                                            "requestMethod": null,
                                                            "guidanceContent": {
                                                                "emergencyTreatment": null,
                                                                "medication": null,
                                                                "hospitalRequest": null,
                                                                "patientEvaluation": null,
                                                                "cprTransfer": null,
                                                                "transferInstructions": null
                                                            },
                                                            "notes": null,
                                                            "createdAt": null,
                                                            "updatedAt": null
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 저장되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 이송",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 795,
                                                    "emergencyReportId": 123,
                                                    "type": "TRANSPORT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "patientTransport": {
                                                            "firstTransport": {
                                                                "hospitalName": null,
                                                                "regionType": null,
                                                                "arrivalTime": null,
                                                                "distanceKm": null,
                                                                "selectedBy": null,
                                                                "retransportReason": null,
                                                                "receiver": null
                                                            },
                                                            "secondTransport": {
                                                                "hospitalName": null,
                                                                "regionType": null,
                                                                "arrivalTime": null,
                                                                "distanceKm": null,
                                                                "selectedBy": null,
                                                                "retransportReason": null,
                                                                "receiver": null
                                                            },
                                                            "notes": null,
                                                            "createdAt": null,
                                                            "updatedAt": null
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 저장되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "세부 상황표",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 796,
                                                    "emergencyReportId": 123,
                                                    "type": "DETAIL_REPORT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "dispatchMembers": {
                                                            "doctor": {
                                                                "affiliation": null,
                                                                "rank": null,
                                                                "name": null,
                                                                "signature": null
                                                            },
                                                            "paramedic1": {
                                                                "grade": null,
                                                                "affiliation": null,
                                                                "rank": null,
                                                                "name": null,
                                                                "signature": null
                                                            },
                                                            "paramedic2": {
                                                                "grade": null,
                                                                "affiliation": null,
                                                                "rank": null,
                                                                "name": null,
                                                                "signature": null
                                                            },
                                                            "driver": {
                                                                "grade": null,
                                                                "affiliation": null,
                                                                "rank": null,
                                                                "name": null,
                                                                "signature": null
                                                            },
                                                            "other": {
                                                                "grade": null,
                                                                "affiliation": null,
                                                                "rank": null,
                                                                "name": null,
                                                                "signature": null
                                                            },
                                                            "createdAt": null,
                                                            "updatedAt": null
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 저장되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            )
                    }
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
                                                    "field": "type",
                                                    "message": "유효하지 않은 섹션 유형입니다."
                                                }
                                            ]
                                        },
                                        "status": 400,
                                        "timestamp": "2023-11-13T09:25:00Z"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "구급일지를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "구급일지 없음",
                            value = """
                                    {
                                        "success": false,
                                        "error": {
                                            "code": "EMERGENCY_REPORT_NOT_FOUND",
                                            "message": "존재하지 않는 구급일지입니다."
                                        },
                                        "status": 404,
                                        "timestamp": "2023-11-13T09:25:00Z"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "섹션 중복 생성",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "섹션 중복",
                            value = """
                                    {
                                        "success": false,
                                        "error": {
                                            "code": "REPORT_SECTION_ALREADY_EXISTS",
                                            "message": "해당 구급일지에 이미 동일한 타입의 섹션이 존재합니다."
                                        },
                                        "status": 409,
                                        "timestamp": "2023-11-13T09:25:00Z"
                                    }
                                    """
                    )
            )
    )
    @ApiUnauthorizedError
    @ApiInternalServerError
    @PostMapping("/{emergencyReportId}/sections/{type}")
    ResponseEntity<? extends ApiResponse> createReportSection(
            @Parameter(description = "구급일지 ID", required = true, example = "5")
            @PathVariable("emergencyReportId") @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @Parameter(description = "섹션 유형", required = true, example = "PATIENT_INFO")
            @PathVariable("type") com.ssairen.domain.emergency.enums.ReportSectionType type,
            @Parameter(hidden = true) @org.springframework.security.core.annotation.AuthenticationPrincipal com.ssairen.global.security.dto.CustomUserPrincipal principal
    );

    @Operation(
            summary = "구급일지 특정 섹션 조회",
            description = "특정 구급일지의 특정 섹션 데이터를 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "구급일지 섹션 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "환자 정보",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 789,
                                                    "emergencyReportId": 5,
                                                    "type": "PATIENT_INFO",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "patientInfo": {
                                                            "reporter": {
                                                                "phone": "01012345678",
                                                                "reportMethod": "휴대전화"
                                                            },
                                                            "patient": {
                                                                "name": "홍길동",
                                                                "gender": "남성",
                                                                "ageYears": 45,
                                                                "birthDate": "1980-02-12",
                                                                "address": "서울특별시 중구 을지로 00"
                                                            },
                                                            "guardian": {
                                                                "name": "김철수",
                                                                "relation": "배우자",
                                                                "phone": "010-2222-3333"
                                                            },
                                                            "incidentLocation": {
                                                                "text": "서울특별시 중구 모 병원 앞 도로"
                                                            },
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 2,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "timestamp": "2023-11-13T10:00:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "구급 출동",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 790,
                                                    "emergencyReportId": 5,
                                                    "type": "DISPATCH",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "dispatch": {
                                                            "reportDate": "2022-08-11",
                                                            "reportTime": "02:26",
                                                            "departureTime": "02:28",
                                                            "arrivalSceneTime": "02:29",
                                                            "contactTime": "02:29",
                                                            "distanceKm": 2.0,
                                                            "departureSceneTime": "02:42",
                                                            "arrivalHospitalTime": "02:47",
                                                            "returnTime": "03:43",
                                                            "dispatchType": "정상",
                                                            "sceneLocation": {
                                                                "primary": "도로",
                                                                "detail": null
                                                            },
                                                            "symptoms": {
                                                                "pain": [
                                                                    {
                                                                        "name": "흉통",
                                                                        "isCustom": false
                                                                    },
                                                                    {
                                                                        "name": "치통",
                                                                        "isCustom": true
                                                                    }
                                                                ],
                                                                "trauma": [
                                                                    {
                                                                        "name": "골절",
                                                                        "isCustom": false
                                                                    },
                                                                    {
                                                                        "name": "치통",
                                                                        "isCustom": false
                                                                    }
                                                                ],
                                                                "otherSymptoms": [
                                                                    {
                                                                        "name": "의식장애",
                                                                        "isCustom": false
                                                                    },
                                                                    {
                                                                        "name": "구강 내 출혈",
                                                                        "isCustom": true
                                                                    }
                                                                ]
                                                            },
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "timestamp": "2023-11-13T10:00:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 발생 유형",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 791,
                                                    "emergencyReportId": 5,
                                                    "type": "INCIDENT_TYPE",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "incidentType": {
                                                            "category": "질병",
                                                            "medicalHistory": {
                                                                "hasHistory": "미상",
                                                                "details": ["고혈압", "당뇨"],
                                                                "notes": null
                                                            },
                                                            "externalCause": {
                                                                "type": "교통사고",
                                                                "subType": "운전자",
                                                                "injurySeverity": "사상자"
                                                            },
                                                            "trauma": {
                                                                "mainCause": ["절식", "기계"],
                                                                "notes": null
                                                            },
                                                            "legalSuspicion": {
                                                                "isSuspected": false,
                                                                "actions": ["경찰통보"],
                                                                "notes": null
                                                            },
                                                            "other": {
                                                                "category": null,
                                                                "notes": null
                                                            },
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "timestamp": "2023-11-13T10:00:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 평가",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 792,
                                                    "emergencyReportId": 5,
                                                    "type": "ASSESSMENT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "patientAssessment": {
                                                            "consciousness": {
                                                                "first": {
                                                                    "time": "02:30",
                                                                    "state": "A"
                                                                },
                                                                "second": {
                                                                    "time": "03:00",
                                                                    "state": "V"
                                                                }
                                                            },
                                                            "pupilReaction": {
                                                                "left": {
                                                                    "status": "정상",
                                                                    "reaction": "반응"
                                                                },
                                                                "right": {
                                                                    "status": "정상",
                                                                    "reaction": "반응"
                                                                }
                                                            },
                                                            "vitalSigns": {
                                                                "first": {
                                                                    "time": "02:35",
                                                                    "bloodPressure": "120/80",
                                                                    "pulse": 80,
                                                                    "respiration": 18,
                                                                    "temperature": 36.8,
                                                                    "spo2": 98,
                                                                    "bloodSugar": 110
                                                                },
                                                                "second": {
                                                                    "time": "03:10",
                                                                    "bloodPressure": "118/78",
                                                                    "pulse": 82,
                                                                    "respiration": 20,
                                                                    "temperature": 36.9,
                                                                    "spo2": 97,
                                                                    "bloodSugar": 108
                                                                }
                                                            },
                                                            "patientLevel": "LEVEL2",
                                                            "notes": "현장 내 반응 지연, 추가 측정 필요",
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "timestamp": "2023-11-13T10:00:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "응급 처치",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 793,
                                                    "emergencyReportId": 5,
                                                    "type": "TREATMENT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "emergencyTreatment": {
                                                            "airwayManagement": {
                                                                "methods": ["기도유지"],
                                                                "notes": null
                                                            },
                                                            "oxygenTherapy": {
                                                                "applied": true,
                                                                "flowRateLpm": 10,
                                                                "device": "비재호흡마스크"
                                                            },
                                                            "cpr": {
                                                                "performed": true,
                                                                "type": "1회 시행",
                                                                "aed": {
                                                                    "used": true,
                                                                    "shock": true,
                                                                    "monitoring": true
                                                                }
                                                            },
                                                            "bleedingControl": {
                                                                "methods": ["직접압박", "지혈"],
                                                                "notes": null
                                                            },
                                                            "woundCare": {
                                                                "types": ["상처 소독 처리"],
                                                                "notes": null
                                                            },
                                                            "delivery": {
                                                                "performed": false,
                                                                "time": null,
                                                                "babyCondition": null
                                                            },
                                                            "notes": null,
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "timestamp": "2023-11-13T10:00:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "의료 지도",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 794,
                                                    "emergencyReportId": 5,
                                                    "type": "MEDICAL_GUIDANCE",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "medicalGuidance": {
                                                            "contactStatus": "연결",
                                                            "requestTime": "02:55",
                                                            "guidanceAgency": {
                                                                "type": "병원",
                                                                "name": null
                                                            },
                                                            "guidanceDoctor": {
                                                                "name": "이의사"
                                                            },
                                                            "requestMethod": "휴대전화",
                                                            "guidanceContent": {
                                                                "emergencyTreatment": [
                                                                    "기관삽관",
                                                                    "성문의 기도유지기",
                                                                    "정맥로 확보",
                                                                    "인공호흡기"
                                                                ],
                                                                "medication": [
                                                                    "NTG",
                                                                    "NS"
                                                                ],
                                                                "hospitalRequest": true,
                                                                "patientEvaluation": true,
                                                                "cprTransfer": false,
                                                                "transferInstructions": [
                                                                    "이송계속"
                                                                ]
                                                            },
                                                            "notes": null,
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "timestamp": "2023-11-13T10:00:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 이송",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 795,
                                                    "emergencyReportId": 5,
                                                    "type": "TRANSPORT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "patientTransport": {
                                                            "firstTransport": {
                                                                "hospitalName": "OO병원",
                                                                "regionType": "관할",
                                                                "arrivalTime": "03:10",
                                                                "distanceKm": 5.2,
                                                                "selectedBy": "119상황실",
                                                                "retransportReason": [
                                                                    "병상부족",
                                                                    "회로장비 고장"
                                                                ],
                                                                "receiver": "의사"
                                                            },
                                                            "secondTransport": {
                                                                "hospitalName": "△△병원",
                                                                "regionType": "타시·도",
                                                                "arrivalTime": "04:05",
                                                                "distanceKm": 23.8,
                                                                "selectedBy": "구급대",
                                                                "retransportReason": ["중환자실"],
                                                                "receiver": "간호사"
                                                            },
                                                            "notes": null,
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "timestamp": "2023-11-13T10:00:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "세부 상황표",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 796,
                                                    "emergencyReportId": 5,
                                                    "type": "DETAIL_REPORT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "dispatchMembers": {
                                                            "doctor": {
                                                                "affiliation": "소방",
                                                                "rank": "의사",
                                                                "name": "홍길동",
                                                                "signature": null
                                                            },
                                                            "paramedic1": {
                                                                "grade": "1급",
                                                                "affiliation": "소방",
                                                                "rank": "소방교",
                                                                "name": "김철수",
                                                                "signature": null
                                                            },
                                                            "paramedic2": {
                                                                "grade": "2급",
                                                                "affiliation": "소방",
                                                                "rank": "소방사",
                                                                "name": "박영희",
                                                                "signature": null
                                                            },
                                                            "driver": {
                                                                "grade": null,
                                                                "affiliation": "소방",
                                                                "rank": "소방교",
                                                                "name": "이운전",
                                                                "signature": null
                                                            },
                                                            "other": {
                                                                "grade": "기타",
                                                                "affiliation": "소방",
                                                                "rank": null,
                                                                "name": "최지원",
                                                                "signature": null
                                                            },
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 1,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "timestamp": "2023-11-13T10:00:00Z"
                                            }
                                            """
                            )
                    }
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
                                                    "field": "type",
                                                    "message": "유효하지 않은 섹션 유형입니다."
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
            description = "구급일지를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "구급일지 없음",
                            value = """
                                    {
                                        "success": false,
                                        "error": {
                                            "code": "EMERGENCY_REPORT_NOT_FOUND",
                                            "message": "구급일지를 찾을 수 없습니다."
                                        },
                                        "status": 404,
                                        "timestamp": "2023-11-13T10:00:00Z"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "섹션을 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "섹션 없음",
                            value = """
                                    {
                                        "success": false,
                                        "error": {
                                            "code": "REPORT_SECTION_NOT_FOUND",
                                            "message": "구급일지 섹션을 찾을 수 없습니다."
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
    @GetMapping("/{emergencyReportId}/sections/{type}")
    ResponseEntity<? extends ApiResponse> getReportSection(
            @Parameter(description = "구급일지 ID", required = true, example = "5")
            @PathVariable("emergencyReportId") @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @Parameter(description = "섹션 유형", required = true, example = "PATIENT_INFO")
            @PathVariable("type") com.ssairen.domain.emergency.enums.ReportSectionType type,
            @Parameter(hidden = true) @org.springframework.security.core.annotation.AuthenticationPrincipal com.ssairen.global.security.dto.CustomUserPrincipal principal
    );

    @Operation(
            summary = "소속 소방서 보고서 조회",
            description = "현재 로그인한 구급대원이 소속된 소방서의 모든 구급일지를 조회합니다."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "소방서 보고서 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "소방서 보고서 조회 성공",
                            value = """
                                    {
                                      "success": true,
                                      "data": [
                                        {
                                            "fireStateInfo": {
                                                "id": 1,
                                                "name": "강남소방서"
                                            },
                                            "emergencyReports": [
                                                {
                                                    "id": 1,
                                                    "paramedicInfo": {
                                                        "id": 1,
                                                        "name": "김철수"
                                                    },
                                                    "dispatchInfo": {
                                                        "id": 1,
                                                        "disasterNumber": "CB0000000662",
                                                        "disasterType": "화재",
                                                        "disasterSubtype": "건물화재",
                                                        "locationAddress": "서울특별시 강남구 테헤란로 123",
                                                        "date": "2025-10-30 17:43:28.235931"
                                                    },
                                                    "createdAt": "2025-10-30 17:43:28.235931",
                                                    "updatedAt": "2025-10-30 17:43:28.235931"
                                                },
                                                {
                                                    "id": 2,
                                                    "paramedicInfo": {
                                                        "id": 1,
                                                        "name": "김철수"
                                                    },
                                                    "dispatchInfo": {
                                                        "id": 2,
                                                        "disasterNumber": "CB0000000662",
                                                        "disasterType": "구조구급",
                                                        "disasterSubtype": "교통사고",
                                                        "locationAddress": "서울특별시 서초구 서초대로 456",
                                                        "date": "2025-11-01 21:00:41.284828"
                                                    },
                                                    "createdAt": "2025-11-01 21:00:41.284828",
                                                    "updatedAt": "2025-11-01 21:00:41.284828"
                                                }
                                            ]
                                        }
                                      ],
                                      "message": "소방서 보고서 조회를 완료하였습니다.",
                                      "timestamp": "2025-10-23T10:46:20+09:00"
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
    @GetMapping("/fire-state")
    ResponseEntity<? extends ApiResponse> getEmergencyReportsByFireState(
            @AuthenticationPrincipal CustomUserPrincipal principal
    );

    @Operation(
            summary = "구급일지 섹션 수정",
            description = "구급일지의 특정 섹션 데이터를 수정합니다. 요청 본문에는 수정할 필드만 포함하거나 전체 필드를 포함할 수 있습니다. 기존 데이터와 병합되어 저장됩니다."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "섹션 수정 요청",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "환자 정보",
                                    value = """
                                            {
                                                "data": {
                                                    "schemaVersion": 1,
                                                    "patientInfo": {
                                                        "reporter": {
                                                            "phone": "01012345678",
                                                            "reportMethod": "휴대전화",
                                                            "value": null
                                                        },
                                                        "patient": {
                                                            "name": "홍길동",
                                                            "gender": "남성",
                                                            "ageYears": 45,
                                                            "birthDate": "1980-02-12",
                                                            "address": "서울특별시 중구 을지로 00"
                                                        },
                                                        "guardian": {
                                                            "name": "김철수",
                                                            "relation": "배우자",
                                                            "phone": "010-2222-3333"
                                                        },
                                                        "incidentLocation": {
                                                            "text": "서울특별시 중구 모 병원 앞 도로"
                                                        },
                                                        "createdAt": "2025-10-21T09:00:00Z",
                                                        "updatedAt": "2025-10-21T09:00:00Z"
                                                    }
                                                }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "구급 출동",
                                    value = """
                                            {
                                                "data": {
                                                    "schemaVersion": 1,
                                                    "dispatch": {
                                                        "reportDatetime": "2022-08-11T02:26:00",
                                                        "departureTime": "02:00",
                                                        "arrivalSceneTime": "02:27",
                                                        "contactTime": "02:29",
                                                        "distanceKm": 2.0,
                                                        "departureSceneTime": "02:42",
                                                        "arrivalHospitalTime": "02:47",
                                                        "returnTime": "03:43",
                                                        "dispatchType": "정상",
                                                        "sceneLocation": {
                                                            "name": "도로",
                                                            "value": null
                                                        },
                                                        "symptoms": {
                                                            "pain": [
                                                                {
                                                                    "name": "흉통",
                                                                    "value": null
                                                                },
                                                                {
                                                                    "name": "치통",
                                                                    "value": null
                                                                }
                                                            ],
                                                            "trauma": [
                                                                {
                                                                    "name": "골절",
                                                                    "value": null
                                                                },
                                                                {
                                                                    "name": "손톱 빠짐",
                                                                    "value": null
                                                                }
                                                            ],
                                                            "otherSymptoms": [
                                                                {
                                                                    "name": "의식장애",
                                                                    "value": null
                                                                },
                                                                {
                                                                    "name": "기타",
                                                                    "value": "구강 내 출혈"
                                                                }
                                                            ]
                                                        },
                                                        "createdAt": "2025-10-21T09:00:00Z",
                                                        "updatedAt": "2025-10-21T09:00:00Z"
                                                    }
                                                }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 발생 유형",
                                    value = """
                                            {
                                                "data": {
                                                    "schemaVersion": 1,
                                                    "incidentType": {
                                                        "medicalHistory": {
                                                            "status": "있음",
                                                            "items": [
                                                                {
                                                                    "name": "고혈압",
                                                                    "value": null
                                                                },
                                                                {
                                                                    "name": "당뇨",
                                                                    "value": null
                                                                },
                                                                {
                                                                    "name": "암",
                                                                    "value": "폐암"
                                                                },
                                                                {
                                                                    "name": "감염병",
                                                                    "value": "결핵"
                                                                },
                                                                {
                                                                    "name": "신부전",
                                                                    "value": "예"
                                                                },
                                                                {
                                                                    "name": "기타",
                                                                    "value": "과거 수술 이력 있음"
                                                                }
                                                            ]
                                                        },
                                                        "category": "질병외",
                                                        "subCategory_traffic": {
                                                            "type": "교통사고",
                                                            "name": "운전자",
                                                            "value": null
                                                        },
                                                        "subCategory_injury": {
                                                            "type": "그 외 손상",
                                                            "name": "추락"
                                                        },
                                                        "subCategory_nonTrauma": {
                                                            "type": "비외상성 손상",
                                                            "name": "동물/곤충",
                                                            "value": "살모사"
                                                        },
                                                        "category_other": "기타",
                                                        "subCategory_other": {
                                                            "name": "자연재해",
                                                            "value": null
                                                        },
                                                        "legalSuspicion": {
                                                            "name": "경찰통보"
                                                        },
                                                        "createdAt": "2025-10-21T09:00:00Z",
                                                        "updatedAt": "2025-10-21T09:00:00Z"
                                                    }
                                                }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 평가",
                                    value = """
                                            {
                                                "data": {
                                                    "schemaVersion": 1,
                                                    "assessment": {
                                                        "consciousness": {
                                                            "first": {
                                                                "time": "02:30",
                                                                "state": "A"
                                                            },
                                                            "second": {
                                                                "time": "03:00",
                                                                "state": "V"
                                                            }
                                                        },
                                                        "pupilReaction": {
                                                            "left": {
                                                                "status": "정상",
                                                                "reaction": "반응"
                                                            },
                                                            "right": {
                                                                "status": "정상",
                                                                "reaction": "반응"
                                                            }
                                                        },
                                                        "vitalSigns": {
                                                            "available": null,
                                                            "first": {
                                                                "time": "02:35",
                                                                "bloodPressure": "120/80",
                                                                "pulse": 80,
                                                                "respiration": 18,
                                                                "temperature": 36.8,
                                                                "spo2": 98,
                                                                "bloodSugar": 110
                                                            },
                                                            "second": {
                                                                "time": "02:50",
                                                                "bloodPressure": "118/78",
                                                                "pulse": 82,
                                                                "respiration": 20,
                                                                "temperature": 36.9,
                                                                "spo2": 97,
                                                                "bloodSugar": 108
                                                            }
                                                        },
                                                        "patientLevel": "LEVEL2",
                                                        "notes": {
                                                            "cheifComplaint": "두통",
                                                            "onset": "02:31",
                                                            "note": "상기 환자 고혈압 두통을 앓고 있던 환자로, 현장 도착 시 침대에 앙와위로 누워 있었으며\\n극심한 두통을 호소하고 있었음. 2일 전 00병원 방문하여 진단 후 아세트아미노펜 750mg 복용 중인 상태로\\n작일 오후 11시에 두통이 심해 8알을 먹었다고 함. 의료 지도 하 활성탄 50g PO 02:37분 시작하였으며 ㅁㅁ병원으로 이송"
                                                        },
                                                        "createdAt": "2025-10-21T09:00:00Z",
                                                        "updatedAt": "2025-10-21T09:00:00Z"
                                                    }
                                                }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "응급 처치",
                                    value = """
                                            {
                                                "data": {
                                                    "schemaVersion": 1,
                                                    "treatment": {
                                                        "airwayManagement": {
                                                            "methods": ["기도유지"]
                                                        },
                                                        "oxygenTherapy": {
                                                            "flowRateLpm": 10,
                                                            "device": "비재호흡마스크"
                                                        },
                                                        "cpr": "실시",
                                                        "ecg": true,
                                                        "aed": {
                                                            "type": "shock",
                                                            "value": null
                                                        },
                                                        "notes": null,
                                                        "circulation": {
                                                            "type": "수액공급 확보",
                                                            "value": "200"
                                                        },
                                                        "drug": {
                                                            "name": "아스피린",
                                                            "dosage": "500mg",
                                                            "time": "14:30"
                                                        },
                                                        "fixed": "목뼈",
                                                        "woundCare": "지혈",
                                                        "deliverytime": null,
                                                        "temperature": null,
                                                        "createdAt": "2025-10-21T09:00:00Z",
                                                        "updatedAt": "2025-10-21T09:00:00Z"
                                                    }
                                                }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "의료 지도",
                                    value = """
                                            {
                                                "data": {
                                                    "schemaVersion": 1,
                                                    "medicalGuidance": {
                                                        "contactStatus": "연결",
                                                        "requestTime": "02:55",
                                                        "requestMethod": {
                                                            "type": "일반전화",
                                                            "value": null
                                                        },
                                                        "guidanceAgency": {
                                                            "type": "병원",
                                                            "value": null
                                                        },
                                                        "guidanceDoctor": {
                                                            "name": "이의사"
                                                        },
                                                        "guidanceContent": {
                                                            "emergencyTreatment": [
                                                                {
                                                                    "name": "기관삽관",
                                                                    "value": null
                                                                },
                                                                {
                                                                    "name": "기타",
                                                                    "value": "드레싱"
                                                                }
                                                            ],
                                                            "medication": [
                                                                {
                                                                    "name": "N/S",
                                                                    "value": null
                                                                },
                                                                {
                                                                    "name": "기타",
                                                                    "value": "활성탄"
                                                                }
                                                            ],
                                                            "hospitalRequest": true,
                                                            "patientEvaluation": true,
                                                            "cprTransfer": false,
                                                            "transferRefusal": false,
                                                            "transferRejection": false,
                                                            "notes": null
                                                        },
                                                        "createdAt": "2025-10-21T09:00:00Z",
                                                        "updatedAt": "2025-10-21T09:00:00Z"
                                                    }
                                                }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 이송",
                                    value = """
                                            {
                                                "data": {
                                                    "schemaVersion": 1,
                                                    "transport": {
                                                        "firstTransport": {
                                                            "hospitalName": "OO병원",
                                                            "regionType": "관할",
                                                            "arrivalTime": "03:10",
                                                            "distanceKm": 5.2,
                                                            "selectedBy": "119상황실",
                                                            "retransportReason": [
                                                                {
                                                                    "type": "병상부족",
                                                                    "name": ["응급실", "중환자실"]
                                                                },
                                                                {
                                                                    "type": "전문의부재",
                                                                    "isCustom": false
                                                                },
                                                                {
                                                                    "type": "원내 CPR",
                                                                    "isCustom": true
                                                                }
                                                            ],
                                                            "receiver": "의사",
                                                            "receiverSign": {
                                                                "type": "image/png",
                                                                "data": "<base64-encoded-signature>"
                                                            }
                                                        },
                                                        "secondTransport": {
                                                            "hospitalName": "△△병원",
                                                            "regionType": "타시·도",
                                                            "arrivalTime": "04:05",
                                                            "distanceKm": 23.8,
                                                            "selectedBy": "구급대",
                                                            "retransportReason": [
                                                                {
                                                                    "type": "병상부족",
                                                                    "name": ["응급실", "중환자실"]
                                                                },
                                                                {
                                                                    "type": "전문의부재",
                                                                    "isCustom": false
                                                                },
                                                                {
                                                                    "type": "원내 CPR",
                                                                    "isCustom": true
                                                                }
                                                            ],
                                                            "receiver": "간호사",
                                                            "receiverSign": {
                                                                "type": "image/png",
                                                                "data": "<base64-encoded-signature>"
                                                            }
                                                        },
                                                        "createdAt": "2025-10-21T09:00:00Z",
                                                        "updatedAt": "2025-10-21T09:00:00Z"
                                                    }
                                                }
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "세부 상황표",
                                    value = """
                                            {
                                                "data": {
                                                    "schemaVersion": 1,
                                                    "detailReport": {
                                                        "doctor": {
                                                            "affiliation": "소방",
                                                            "name": "홍길동",
                                                            "signature": null
                                                        },
                                                        "paramedic1": {
                                                            "grade": "1급",
                                                            "rank": "교",
                                                            "name": "김철수",
                                                            "signature": null
                                                        },
                                                        "paramedic2": {
                                                            "grade": "2급",
                                                            "rank": "사",
                                                            "name": "박영희",
                                                            "signature": null
                                                        },
                                                        "driver": {
                                                            "grade": "구급교육",
                                                            "rank": "소방교",
                                                            "name": "이운전",
                                                            "signature": null
                                                        },
                                                        "other": {
                                                            "grade": "기타",
                                                            "rank": null,
                                                            "name": "최지원",
                                                            "signature": null
                                                        },
                                                        "obstacles": {
                                                            "type": "없음",
                                                            "isCustom": false
                                                        },
                                                        "createdAt": "2025-10-21T09:00:00Z",
                                                        "updatedAt": "2025-10-21T09:00:00Z"
                                                    }
                                                }
                                            }
                                            """
                            )
                    }
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "구급일지 섹션 수정 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "환자 정보",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 789,
                                                    "emergencyReportId": 5,
                                                    "type": "PATIENT_INFO",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "patientInfo": {
                                                            "reporter": {
                                                                "phone": "01012345678",
                                                                "reportMethod": "휴대전화"
                                                            },
                                                            "patient": {
                                                                "name": "홍길동",
                                                                "gender": "남성",
                                                                "ageYears": 45,
                                                                "birthDate": "1980-02-12",
                                                                "address": "서울특별시 중구 을지로 00"
                                                            },
                                                            "guardian": {
                                                                "name": "김철수",
                                                                "relation": "배우자",
                                                                "phone": "010-2222-3333"
                                                            },
                                                            "incidentLocation": {
                                                                "text": "서울특별시 중구 모 병원 앞 도로"
                                                            },
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 2,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 수정되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "구급 출동",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 790,
                                                    "emergencyReportId": 5,
                                                    "type": "DISPATCH",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "dispatch": {
                                                            "reportDate": "2022-08-11",
                                                            "reportTime": "02:26",
                                                            "departureTime": "02:28",
                                                            "arrivalSceneTime": "02:29",
                                                            "contactTime": "02:29",
                                                            "distanceKm": 2.0,
                                                            "departureSceneTime": "02:42",
                                                            "arrivalHospitalTime": "02:47",
                                                            "returnTime": "03:43",
                                                            "dispatchType": "정상",
                                                            "sceneLocation": {
                                                                "primary": "도로",
                                                                "detail": null
                                                            },
                                                            "symptoms": {
                                                                "pain": [
                                                                    {
                                                                        "name": "흉통",
                                                                        "isCustom": false
                                                                    }
                                                                ],
                                                                "trauma": [
                                                                    {
                                                                        "name": "골절",
                                                                        "isCustom": false
                                                                    }
                                                                ],
                                                                "otherSymptoms": [
                                                                    {
                                                                        "name": "의식장애",
                                                                        "isCustom": false
                                                                    }
                                                                ]
                                                            },
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 2,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 수정되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 발생 유형",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 791,
                                                    "emergencyReportId": 5,
                                                    "type": "INCIDENT_TYPE",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "incidentType": {
                                                            "category": "질병",
                                                            "medicalHistory": {
                                                                "hasHistory": "미상",
                                                                "details": ["고혈압", "당뇨"],
                                                                "notes": null
                                                            },
                                                            "externalCause": {
                                                                "type": "교통사고",
                                                                "subType": "운전자",
                                                                "injurySeverity": "사상자"
                                                            },
                                                            "trauma": {
                                                                "mainCause": ["절식", "기계"],
                                                                "notes": null
                                                            },
                                                            "legalSuspicion": {
                                                                "isSuspected": false,
                                                                "actions": ["경찰통보"],
                                                                "notes": null
                                                            },
                                                            "other": {
                                                                "category": null,
                                                                "notes": null
                                                            },
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 2,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 수정되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 평가",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 792,
                                                    "emergencyReportId": 5,
                                                    "type": "ASSESSMENT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "patientAssessment": {
                                                            "consciousness": {
                                                                "first": {
                                                                    "time": "02:30",
                                                                    "state": "A"
                                                                },
                                                                "second": {
                                                                    "time": "03:00",
                                                                    "state": "V"
                                                                }
                                                            },
                                                            "pupilReaction": {
                                                                "left": {
                                                                    "status": "정상",
                                                                    "reaction": "반응"
                                                                },
                                                                "right": {
                                                                    "status": "정상",
                                                                    "reaction": "반응"
                                                                }
                                                            },
                                                            "vitalSigns": {
                                                                "first": {
                                                                    "time": "02:35",
                                                                    "bloodPressure": "120/80",
                                                                    "pulse": 80,
                                                                    "respiration": 18,
                                                                    "temperature": 36.8,
                                                                    "spo2": 98,
                                                                    "bloodSugar": 110
                                                                },
                                                                "second": {
                                                                    "time": "03:10",
                                                                    "bloodPressure": "118/78",
                                                                    "pulse": 82,
                                                                    "respiration": 20,
                                                                    "temperature": 36.9,
                                                                    "spo2": 97,
                                                                    "bloodSugar": 108
                                                                }
                                                            },
                                                            "patientLevel": "LEVEL2",
                                                            "notes": "현장 내 반응 지연, 추가 측정 필요",
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 2,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 수정되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "응급 처치",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 793,
                                                    "emergencyReportId": 5,
                                                    "type": "TREATMENT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "emergencyTreatment": {
                                                            "airwayManagement": {
                                                                "methods": ["기도유지"],
                                                                "notes": null
                                                            },
                                                            "oxygenTherapy": {
                                                                "applied": true,
                                                                "flowRateLpm": 10,
                                                                "device": "비재호흡마스크"
                                                            },
                                                            "cpr": {
                                                                "performed": true,
                                                                "type": "1회 시행",
                                                                "aed": {
                                                                    "used": true,
                                                                    "shock": true,
                                                                    "monitoring": true
                                                                }
                                                            },
                                                            "bleedingControl": {
                                                                "methods": ["직접압박", "지혈"],
                                                                "notes": null
                                                            },
                                                            "woundCare": {
                                                                "types": ["상처 소독 처리"],
                                                                "notes": null
                                                            },
                                                            "delivery": {
                                                                "performed": false,
                                                                "time": null,
                                                                "babyCondition": null
                                                            },
                                                            "notes": null,
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 2,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 수정되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "의료 지도",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 794,
                                                    "emergencyReportId": 5,
                                                    "type": "MEDICAL_GUIDANCE",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "medicalGuidance": {
                                                            "contactStatus": "연결",
                                                            "requestTime": "02:55",
                                                            "guidanceAgency": {
                                                                "type": "병원",
                                                                "name": null
                                                            },
                                                            "guidanceDoctor": {
                                                                "name": "이의사"
                                                            },
                                                            "requestMethod": "휴대전화",
                                                            "guidanceContent": {
                                                                "emergencyTreatment": [
                                                                    "기관삽관",
                                                                    "성문의 기도유지기",
                                                                    "정맥로 확보",
                                                                    "인공호흡기"
                                                                ],
                                                                "medication": [
                                                                    "NTG",
                                                                    "NS"
                                                                ],
                                                                "hospitalRequest": true,
                                                                "patientEvaluation": true,
                                                                "cprTransfer": false,
                                                                "transferInstructions": [
                                                                    "이송계속"
                                                                ]
                                                            },
                                                            "notes": null,
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 2,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 수정되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "환자 이송",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 795,
                                                    "emergencyReportId": 5,
                                                    "type": "TRANSPORT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "patientTransport": {
                                                            "firstTransport": {
                                                                "hospitalName": "OO병원",
                                                                "regionType": "관할",
                                                                "arrivalTime": "03:10",
                                                                "distanceKm": 5.2,
                                                                "selectedBy": "119상황실",
                                                                "retransportReason": [
                                                                    "병상부족",
                                                                    "회로장비 고장"
                                                                ],
                                                                "receiver": "의사"
                                                            },
                                                            "secondTransport": {
                                                                "hospitalName": "△△병원",
                                                                "regionType": "타시·도",
                                                                "arrivalTime": "04:05",
                                                                "distanceKm": 23.8,
                                                                "selectedBy": "구급대",
                                                                "retransportReason": ["중환자실"],
                                                                "receiver": "간호사"
                                                            },
                                                            "notes": null,
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 2,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 수정되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "세부 상황표",
                                    value = """
                                            {
                                                "success": true,
                                                "data": {
                                                    "id": 796,
                                                    "emergencyReportId": 5,
                                                    "type": "DETAIL_REPORT",
                                                    "data": {
                                                        "schemaVersion": 1,
                                                        "dispatchMembers": {
                                                            "doctor": {
                                                                "affiliation": "소방",
                                                                "rank": "의사",
                                                                "name": "홍길동",
                                                                "signature": null
                                                            },
                                                            "paramedic1": {
                                                                "grade": "1급",
                                                                "affiliation": "소방",
                                                                "rank": "소방교",
                                                                "name": "김철수",
                                                                "signature": null
                                                            },
                                                            "paramedic2": {
                                                                "grade": "2급",
                                                                "affiliation": "소방",
                                                                "rank": "소방사",
                                                                "name": "박영희",
                                                                "signature": null
                                                            },
                                                            "driver": {
                                                                "grade": null,
                                                                "affiliation": "소방",
                                                                "rank": "소방교",
                                                                "name": "이운전",
                                                                "signature": null
                                                            },
                                                            "other": {
                                                                "grade": "기타",
                                                                "affiliation": "소방",
                                                                "rank": null,
                                                                "name": "최지원",
                                                                "signature": null
                                                            },
                                                            "createdAt": "2025-10-21T09:00:00Z",
                                                            "updatedAt": "2025-10-21T09:00:00Z"
                                                        }
                                                    },
                                                    "version": 2,
                                                    "createdAt": "2023-11-13T09:25:00Z"
                                                },
                                                "message": "구급일지 섹션이 수정되었습니다.",
                                                "timestamp": "2023-11-13T09:25:00Z"
                                            }
                                            """
                            )
                    }
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
                                                    "field": "type",
                                                    "message": "유효하지 않은 섹션 유형입니다."
                                                }
                                            ]
                                        },
                                        "status": 400,
                                        "timestamp": "2023-11-13T09:25:00Z"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "구급일지를 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "구급일지 없음",
                            value = """
                                    {
                                        "success": false,
                                        "error": {
                                            "code": "EMERGENCY_REPORT_NOT_FOUND",
                                            "message": "존재하지 않는 구급일지입니다."
                                        },
                                        "status": 404,
                                        "timestamp": "2023-11-13T09:25:00Z"
                                    }
                                    """
                    )
            )
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404",
            description = "섹션을 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "섹션 없음",
                            value = """
                                    {
                                        "success": false,
                                        "error": {
                                            "code": "REPORT_SECTION_NOT_FOUND",
                                            "message": "구급일지 해당 섹션을 찾을 수 없습니다."
                                        },
                                        "status": 404,
                                        "timestamp": "2023-11-13T09:25:00Z"
                                    }
                                    """
                    )
            )
    )
    @ApiUnauthorizedError
    @ApiInternalServerError
    @PatchMapping("/{emergencyReportId}/sections/{type}")
    ResponseEntity<? extends ApiResponse> updateReportSection(
            @Parameter(description = "구급일지 ID", required = true, example = "5")
            @PathVariable("emergencyReportId") @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId,
            @Parameter(description = "섹션 유형", required = true, example = "PATIENT_INFO")
            @PathVariable("type") ReportSectionType type,
            @Valid @RequestBody com.ssairen.domain.emergency.dto.ReportSectionUpdateRequest request,
            @Parameter(hidden = true) @org.springframework.security.core.annotation.AuthenticationPrincipal com.ssairen.global.security.dto.CustomUserPrincipal principal
    );
}
