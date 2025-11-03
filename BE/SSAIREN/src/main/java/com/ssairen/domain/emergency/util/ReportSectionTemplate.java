package com.ssairen.domain.emergency.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;

/**
 * 구급일지 섹션 타입별 스켈레톤 JSON 템플릿 제공
 * TODO: 스키마 변경되면 맞춰서 수정할 것
 * 모든 값은 null로 초기화되며, 추후 PATCH 메서드로 값을 입력
 */
public class ReportSectionTemplate {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 환자 정보
    private static final String PATIENT_INFO_TEMPLATE = """
            {
                "schemaVersion": 1,
                "patientInfo": {
                    "reporter": {
                        "phone": null,
                        "reportMethod": null,
                        "value": null
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
            }
            """;

    // 구급 출동
    private static final String DISPATCH_TEMPLATE = """
            {
                "schemaVersion": 1,
                "dispatch": {
                    "reportDatetime": null,
                    "departureTime": null,
                    "arrivalSceneTime": null,
                    "contactTime": null,
                    "distanceKm": null,
                    "departureSceneTime": null,
                    "arrivalHospitalTime": null,
                    "returnTime": null,
                    "dispatchType": null,
                    "sceneLocation": {
                        "name": null,
                        "value": null
                    },
                    "symptoms": {
                        "pain": null,
                        "trauma": null,
                        "otherSymptoms": null
                    },
                    "createdAt": null,
                    "updatedAt": null
                }
            }
            """;

    // 환자 발생 유형
    private static final String INCIDENT_TYPE_TEMPLATE = """
            {
                "schemaVersion": 1,
                "incidentType": {
                    "medicalHistory": {
                        "status": null,
                        "items": null
                    },
                    "category": null,
                    "subCategory_traffic": {
                        "type": null,
                        "name": null,
                        "value": null
                    },
                    "subCategory_injury": {
                        "type": null,
                        "name": null
                    },
                    "subCategory_nonTrauma": {
                        "type": null,
                        "name": null,
                        "value": null
                    },
                    "category_other": null,
                    "subCategory_other": {
                        "name": null,
                        "value": null
                    },
                    "legalSuspicion": {
                        "name": null
                    },
                    "createdAt": null,
                    "updatedAt": null
                }
            }
            """;

    // 환자 평가
    private static final String ASSESSMENT_TEMPLATE = """
            {
                "schemaVersion": 1,
                "assessment": {
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
                        "available": null,
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
                    "notes": {
                        "cheifComplaint": null,
                        "onset": null,
                        "note": null
                    },
                    "createdAt": null,
                    "updatedAt": null
                }
            }
            """;

    // 응급 처치
    private static final String TREATMENT_TEMPLATE = """
            {
                "schemaVersion": 1,
                "treatment": {
                    "airwayManagement": {
                        "methods": null
                    },
                    "oxygenTherapy": {
                        "flowRateLpm": null,
                        "device": null
                    },
                    "cpr": null,
                    "ecg": null,
                    "aed": {
                        "type": null,
                        "value": null
                    },
                    "notes": null,
                    "circulation": {
                        "type": null,
                        "value": null
                    },
                    "drug": null,
                    "fixed": null,
                    "woundCare": null,
                    "deliverytime": null,
                    "temperature": null,
                    "createdAt": null,
                    "updatedAt": null
                }
            }
            """;

    // 의료 지도
    private static final String MEDICAL_GUIDANCE_TEMPLATE = """
            {
                "schemaVersion": 1,
                "medicalGuidance": {
                    "contactStatus": null,
                    "requestTime": null,
                    "requestMethod": {
                        "type": null,
                        "value": null
                    },
                    "guidanceAgency": {
                        "type": null,
                        "value": null
                    },
                    "guidanceDoctor": {
                        "name": null
                    },
                    "guidanceContent": {
                        "emergencyTreatment": null,
                        "medication": null,
                        "hospitalRequest": null,
                        "patientEvaluation": null,
                        "cprTransfer": null,
                        "transferRefusal": null,
                        "transferRejection": null,
                        "notes": null
                    },
                    "createdAt": null,
                    "updatedAt": null
                }
            }
            """;

    // 환자 이송
    private static final String TRANSPORT_TEMPLATE = """
            {
                "schemaVersion": 1,
                "transport": {
                    "firstTransport": {
                        "hospitalName": null,
                        "regionType": null,
                        "arrivalTime": null,
                        "distanceKm": null,
                        "selectedBy": null,
                        "retransportReason": null,
                        "receiver": null,
                        "receiverSign": {
                            "type": null,
                            "data": null
                        }
                    },
                    "secondTransport": {
                        "hospitalName": null,
                        "regionType": null,
                        "arrivalTime": null,
                        "distanceKm": null,
                        "selectedBy": null,
                        "retransportReason": null,
                        "receiver": null,
                        "receiverSign": {
                            "type": null,
                            "data": null
                        }
                    },
                    "createdAt": null,
                    "updatedAt": null
                }
            }
            """;

    // 세부 상황표 (출동 대원)
    private static final String DETAIL_REPORT_TEMPLATE = """
            {
                "schemaVersion": 1,
                "detailReport": {
                    "doctor": {
                        "affiliation": null,
                        "name": null,
                        "signature": null
                    },
                    "paramedic1": {
                        "grade": null,
                        "rank": null,
                        "name": null,
                        "signature": null
                    },
                    "paramedic2": {
                        "grade": null,
                        "rank": null,
                        "name": null,
                        "signature": null
                    },
                    "driver": {
                        "grade": null,
                        "rank": null,
                        "name": null,
                        "signature": null
                    },
                    "other": {
                        "grade": null,
                        "rank": null,
                        "name": null,
                        "signature": null
                    },
                    "obstacles": {
                        "type": null,
                        "isCustom": null
                    },
                    "createdAt": null,
                    "updatedAt": null
                }
            }
            """;

    /**
     * 섹션 타입에 따른 스켈레톤 JSON 템플릿 반환
     *
     * @param type 섹션 타입 (SUMMATION 제외)
     * @return 스켈레톤 JsonNode (모든 값 null)
     * @throws CustomException JSON 파싱 실패 시
     */
    public static JsonNode getTemplate(ReportSectionType type) {
        String template = switch (type) {
            case PATIENT_INFO -> PATIENT_INFO_TEMPLATE;
            case DISPATCH -> DISPATCH_TEMPLATE;
            case INCIDENT_TYPE -> INCIDENT_TYPE_TEMPLATE;
            case ASSESSMENT -> ASSESSMENT_TEMPLATE;
            case TREATMENT -> TREATMENT_TEMPLATE;
            case MEDICAL_GUIDANCE -> MEDICAL_GUIDANCE_TEMPLATE;
            case TRANSPORT -> TRANSPORT_TEMPLATE;
            case DETAIL_REPORT -> DETAIL_REPORT_TEMPLATE;
        };

        try {
            return objectMapper.readTree(template);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INVALID_JSONB_FORMAT);
        }
    }
}
