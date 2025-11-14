package com.example.ssairen_app.data.model.response

/**
 * 세부사항 섹션 조회/저장 응답 모델
 *
 * API:
 * - GET /api/emergency-reports/{emergencyReportId}/sections/DETAIL_REPORT
 * - PATCH /api/emergency-reports/{emergencyReportId}/sections/DETAIL_REPORT
 */
data class DetailReportResponse(
    val success: Boolean,
    val data: DetailReportResponseData? = null,
    val message: String? = null,
    val error: ErrorData? = null,
    val timestamp: String? = null
)

data class DetailReportResponseData(
    val id: Int,
    val emergencyReportId: Int,
    val type: String,
    val data: DetailReportDataWrapper,
    val version: Int,
    val createdAt: String
)

data class DetailReportDataWrapper(
    val schemaVersion: Int,
    val detailReport: DetailReportResponseInfo
)

data class DetailReportResponseInfo(
    val doctor: ParamedicMemberResponse?,
    val paramedic1: ParamedicMemberResponse?,
    val paramedic2: ParamedicMemberResponse?,
    val driver: ParamedicMemberResponse?,
    val other: ParamedicMemberResponse?,
    // TODO: 백엔드 수정 필요 - obstacles는 배열로 반환되어야 하지만, 현재 빈 데이터일 때 단일 객체로 반환됨
    // 현재: "obstacles": {"type": null, "isCustom": null}
    // 수정 후: "obstacles": []
    val obstacles: List<ObstacleItemResponse>? = null,
    val createdAt: String?,
    val updatedAt: String?
)

data class ParamedicMemberResponse(
    val affiliation: String? = null,
    val name: String?,
    val grade: String? = null,
    val rank: String? = null,
    val signature: String? = null
)

data class ObstacleItemResponse(
    val type: String,
    val isCustom: Boolean,
    val value: String? = null
)
