package com.ssairen.domain.emergency.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.entity.ReportSection;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.repository.EmergencyReportRepository;
import com.ssairen.domain.emergency.repository.ReportSectionRepository;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 시연용 데모 Controller
 */
@Tag(name = "Demo", description = "시연용 API (개발/테스트 전용)")
@Slf4j
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
@Validated
public class DemoController {

    private final EmergencyReportRepository emergencyReportRepository;
    private final ReportSectionRepository reportSectionRepository;
    private final ObjectMapper objectMapper;

    /**
     * 시연용 샘플 데이터 업데이트
     */
    @Operation(
            summary = "시연용 샘플 데이터 업데이트",
            description = "emergency_report_id를 받아서 해당 report의 4개 타입 섹션 data를 샘플 데이터로 업데이트합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "샘플 데이터 업데이트 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "구급일지 또는 섹션을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PutMapping("/sample-data/{emergencyReportId}")
    @Transactional
    public ResponseEntity<ApiResponse<String>> updateSampleData(
            @Parameter(description = "구급일지 ID", required = true, example = "1")
            @PathVariable @Positive(message = "구급일지 ID는 양의 정수여야 합니다.") Long emergencyReportId
    ) {
        log.info("Updating sample data for emergency report ID: {}", emergencyReportId);

        // 1. 구급일지 존재 확인
        EmergencyReport emergencyReport = emergencyReportRepository.findById(emergencyReportId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_REPORT_NOT_FOUND));

        try {
            // 2. PATIENT_INFO 섹션 업데이트
            String patientInfoJson = """
                    {
                        "patientInfo": {
                            "patient": {
                                "name": "신정운",
                                "gender": "여성",
                                "address": null,
                                "ageYears": 28,
                                "birthDate": null
                            },
                            "guardian": {
                                "name": null,
                                "phone": null,
                                "relation": null
                            },
                            "reporter": {
                                "phone": null,
                                "value": null,
                                "reportMethod": null
                            },
                            "createdAt": null,
                            "updatedAt": null,
                            "incidentLocation": {
                                "text": null
                            }
                        },
                        "schemaVersion": 1
                    }
                    """;
            JsonNode patientInfoData = objectMapper.readTree(patientInfoJson);
            ReportSection patientInfoSection = reportSectionRepository
                    .findByEmergencyReportAndType(emergencyReport, ReportSectionType.PATIENT_INFO)
                    .orElseThrow(() -> new CustomException(ErrorCode.REPORT_SECTION_NOT_FOUND));
            patientInfoSection.updateData(patientInfoData);

            // 3. ASSESSMENT 섹션 업데이트
            String assessmentJson = """
                    {
                        "assessment": {
                            "notes": {
                                "note": null,
                                "onset": null,
                                "cheifComplaint": null
                            },
                            "createdAt": null,
                            "updatedAt": null,
                            "vitalSigns": {
                                "first": {
                                    "spo2": 99,
                                    "time": null,
                                    "pulse": 85,
                                    "bloodSugar": null,
                                    "respiration": 16,
                                    "temperature": 36.5,
                                    "bloodPressure": "120"
                                },
                                "second": {
                                    "spo2": null,
                                    "time": null,
                                    "pulse": null,
                                    "bloodSugar": null,
                                    "respiration": null,
                                    "temperature": null,
                                    "bloodPressure": null
                                },
                                "available": null
                            },
                            "patientLevel": null,
                            "consciousness": {
                                "first": {
                                    "time": null,
                                    "state": "A"
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
                            }
                        },
                        "schemaVersion": 1
                    }
                    """;
            JsonNode assessmentData = objectMapper.readTree(assessmentJson);
            ReportSection assessmentSection = reportSectionRepository
                    .findByEmergencyReportAndType(emergencyReport, ReportSectionType.ASSESSMENT)
                    .orElseThrow(() -> new CustomException(ErrorCode.REPORT_SECTION_NOT_FOUND));
            assessmentSection.updateData(assessmentData);

            // 4. DISPATCH 섹션 업데이트
            String dispatchJson = """
                    {
                        "dispatch": {
                            "symptoms": {
                                "pain": [{"name": "두통"}],
                                "trauma": [],
                                "otherSymptoms": []
                            },
                            "createdAt": "2025-11-19T10:22:31",
                            "updatedAt": "2025-11-19T10:22:31",
                            "distanceKm": 0.0,
                            "returnTime": "00:00",
                            "contactTime": "00:00",
                            "dispatchType": "정상",
                            "departureTime": "2025-11-19T10:20:34",
                            "sceneLocation": {
                                "name": "집",
                                "value": null
                            },
                            "reportDatetime": "2023-11-13T09:16:00",
                            "arrivalSceneTime": "00:00",
                            "departureSceneTime": "00:00",
                            "arrivalHospitalTime": "00:00"
                        }
                    }
                    """;
            JsonNode dispatchData = objectMapper.readTree(dispatchJson);
            ReportSection dispatchSection = reportSectionRepository
                    .findByEmergencyReportAndType(emergencyReport, ReportSectionType.DISPATCH)
                    .orElseThrow(() -> new CustomException(ErrorCode.REPORT_SECTION_NOT_FOUND));
            dispatchSection.updateData(dispatchData);

            // 5. INCIDENT_TYPE 섹션 업데이트
            String incidentTypeJson = """
                    {
                        "incidentType": {
                            "category": null,
                            "createdAt": null,
                            "updatedAt": null,
                            "category_other": null,
                            "legalSuspicion": {
                                "name": null
                            },
                            "medicalHistory": {
                                "items": [{"name": "고혈압"}, {"name": "당뇨"}],
                                "status": "있음"
                            },
                            "subCategory_other": {
                                "name": null,
                                "value": null
                            },
                            "subCategory_injury": {
                                "name": null,
                                "type": null
                            },
                            "subCategory_traffic": {
                                "name": null,
                                "type": null,
                                "value": null
                            },
                            "subCategory_nonTrauma": {
                                "name": null,
                                "type": null,
                                "value": null
                            }
                        },
                        "schemaVersion": 1
                    }
                    """;
            JsonNode incidentTypeData = objectMapper.readTree(incidentTypeJson);
            ReportSection incidentTypeSection = reportSectionRepository
                    .findByEmergencyReportAndType(emergencyReport, ReportSectionType.INCIDENT_TYPE)
                    .orElseThrow(() -> new CustomException(ErrorCode.REPORT_SECTION_NOT_FOUND));
            incidentTypeSection.updateData(incidentTypeData);

            log.info("Sample data updated successfully for emergency report ID: {}", emergencyReportId);
            return ResponseEntity.ok(
                    ApiResponse.success("샘플 데이터 업데이트 완료", "4개의 report sections data가 업데이트되었습니다.")
            );

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to update sample data for emergency report ID: {}", emergencyReportId, e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "샘플 데이터 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
