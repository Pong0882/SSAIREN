package com.example.ssairen_app.data.model.request

/**
 * 의료지도 섹션 저장 요청 모델
 *
 * API: PATCH /api/emergency-reports/{emergencyReportId}/sections/MEDICAL_GUIDANCE
 */
data class MedicalGuidanceRequest(
    val data: MedicalGuidanceRequestData
)

data class MedicalGuidanceRequestData(
    val medicalGuidance: MedicalGuidanceInfo
)

data class MedicalGuidanceInfo(
    val contactStatus: String,                  // 의료지도 연결 여부: 연결 | 미연결
    val requestTime: String,                    // 의료지도 요청 시각 (HH:mm)
    val requestMethod: RequestMethod,           // 요청 방법
    val guidanceAgency: GuidanceAgency,         // 의료지도 기관
    val guidanceDoctor: GuidanceDoctor,         // 의료지도 의사
    val guidanceContent: GuidanceContent,       // 의료지도 내용
    val createdAt: String,                      // 생성 시각 (ISO 8601)
    val updatedAt: String                       // 수정 시각 (ISO 8601)
)

data class RequestMethod(
    val type: String,                           // 요청 방법: 일반전화 | 휴대전화(음성/화상) | 무전기 | 기타
    val value: String? = null                   // 휴대전화 선택 시 음성/화상, 기타일 때 사용자 입력값
)

data class GuidanceAgency(
    val type: String,                           // 의료지도 기관: 소방 | 병원 | 기타
    val value: String? = null                   // 기타 선택 시 직접 입력
)

data class GuidanceDoctor(
    val name: String                            // 의료지도 의사 성명
)

data class GuidanceContent(
    val emergencyTreatment: List<TreatmentItem>,    // 응급처치 관련 지시 (복수 선택)
    val medication: List<TreatmentItem>,            // 약물 투여 관련 지시 (복수 선택)
    val hospitalRequest: Boolean,                   // 병원 선정
    val patientEvaluation: Boolean,                 // 환자 평가
    val cprTransfer: Boolean,                       // CPR 유보/중단 여부
    val transferRefusal: Boolean,                   // 이송 거절
    val transferRejection: Boolean,                 // 이송 거부
    val notes: String?                              // 기타 비고
)

data class TreatmentItem(
    val name: String,                           // 항목명
    val value: String? = null                   // 기타 선택 시 사용자 입력값, 그 외는 null
)
