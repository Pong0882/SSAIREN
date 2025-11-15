package com.example.ssairen_app.data.model.response

/**
 * 의료지도 섹션 저장 응답 모델
 *
 * API: PATCH /api/emergency-reports/{emergencyReportId}/sections/MEDICAL_GUIDANCE
 * API: GET /api/emergency-reports/{emergencyReportId}/sections/MEDICAL_GUIDANCE
 */
data class MedicalGuidanceResponse(
    val success: Boolean,
    val data: MedicalGuidanceResponseData,
    val message: String? = null,
    val error: ErrorData? = null,
    val timestamp: String? = null
)

data class MedicalGuidanceResponseData(
    val id: Int,
    val emergencyReportId: Int,
    val type: String,                           // "MEDICAL_GUIDANCE"
    val data: MedicalGuidanceDataWrapper,
    val version: Int,
    val createdAt: String
)

data class MedicalGuidanceDataWrapper(
    val medicalGuidance: MedicalGuidanceResponseInfo
)

data class MedicalGuidanceResponseInfo(
    val contactStatus: String?,
    val requestTime: String?,
    val requestMethod: RequestMethodResponse,
    val guidanceAgency: GuidanceAgencyResponse,
    val guidanceDoctor: GuidanceDoctorResponse,
    val guidanceContent: GuidanceContentResponse,
    val createdAt: String?,
    val updatedAt: String?
)

data class RequestMethodResponse(
    val type: String?,
    val value: String?
)

data class GuidanceAgencyResponse(
    val type: String?,
    val value: String?
)

data class GuidanceDoctorResponse(
    val name: String?
)

data class GuidanceContentResponse(
    val emergencyTreatment: List<TreatmentItemResponse>?,
    val medication: List<TreatmentItemResponse>?,
    val hospitalRequest: Boolean?,
    val patientEvaluation: Boolean?,
    val cprTransfer: Boolean?,
    val transferRefusal: Boolean?,
    val transferRejection: Boolean?,
    val notes: String?
)

data class TreatmentItemResponse(
    val name: String,
    val value: String?
)
