package com.example.ssairen_app.data.model.response

/**
 * 구급출동 섹션 저장 응답 모델
 *
 * API: PATCH /api/emergency-reports/{emergencyReportId}/sections/DISPATCH
 * API: GET /api/emergency-reports/{emergencyReportId}/sections/DISPATCH
 */
data class DispatchResponse(
    val success: Boolean,
    val data: DispatchResponseData,
    val message: String? = null,
    val error: ErrorData? = null,
    val timestamp: String? = null
)

data class DispatchResponseData(
    val id: Int,
    val emergencyReportId: Int,
    val type: String,                     // "DISPATCH"
    val data: DispatchDataWrapper,
    val version: Int,
    val createdAt: String
)

data class DispatchDataWrapper(
    val dispatch: DispatchResponseInfo
)

data class DispatchResponseInfo(
    val reportDatetime: String?,          // "2025-11-10T12:00:00"
    val departureTime: String?,           // "02:28"
    val arrivalSceneTime: String?,        // "02:29"
    val contactTime: String?,             // "02:29"
    val distanceKm: Double?,              // 2.0
    val departureSceneTime: String?,      // "02:42"
    val arrivalHospitalTime: String?,     // "02:47"
    val returnTime: String?,              // "03:43"
    val dispatchType: String?,            // "정상"
    val sceneLocation: SceneLocationResponse,
    val symptoms: SymptomsResponse,
    val createdAt: String?,
    val updatedAt: String?
)

data class SceneLocationResponse(
    val name: String?,                    // 장소 주 분류 (예: "도로")
    val value: String?                    // 장소 세부 사항 ("기타" 선택 시에만 값이 있음)
)

data class SymptomsResponse(
    val pain: List<SymptomItemResponse>? = null,
    val trauma: List<SymptomItemResponse>? = null,
    val otherSymptoms: List<SymptomItemResponse>? = null
)

data class SymptomItemResponse(
    val name: String,                     // 증상 이름
    val value: String? = null             // "그 밖의 통증"/"기타"일 때 사용자 실제 입력값
)
