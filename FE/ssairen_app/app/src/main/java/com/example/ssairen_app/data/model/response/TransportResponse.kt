package com.example.ssairen_app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 환자이송 섹션 저장 응답 모델
 *
 * API: PATCH /api/emergency-reports/{emergencyReportId}/sections/TRANSPORT
 */
data class TransportResponse(
    val success: Boolean,
    val data: TransportResponseData? = null,
    val message: String? = null,
    val error: ErrorData? = null,
    val timestamp: String? = null
)

data class TransportResponseData(
    val id: Int,
    val emergencyReportId: Int,
    val type: String,                           // "TRANSPORT"
    val data: TransportDataWrapper,
    val version: Int,
    val createdAt: String
)

data class TransportDataWrapper(
    val schemaVersion: Int,
    @SerializedName("transport")
    val patientTransport: PatientTransportResponseInfo?
)

data class PatientTransportResponseInfo(
    val firstTransport: TransportDetailResponse?,
    val secondTransport: TransportDetailResponse?,
    val notes: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class TransportDetailResponse(
    val hospitalName: String?,
    val regionType: String?,
    val arrivalTime: String?,
    val distanceKm: Double?,
    val selectedBy: String?,
    val retransportReason: List<RetransportReasonResponse>?,
    val receiver: String?,
    val receiverSign: ReceiverSignResponse?
)

data class RetransportReasonResponse(
    val type: String,                           // 사유 타입 (병상부족, 전문의부재 등)
    val name: List<String>? = null,             // 병상부족일 경우만: 응급실, 중환자실 등
    val isCustom: Boolean? = null               // 기타 입력 여부
)

data class ReceiverSignResponse(
    val type: String?,                          // 파일 형식 (예: image/png)
    val data: String?                           // Base64 인코딩된 서명 이미지
)
