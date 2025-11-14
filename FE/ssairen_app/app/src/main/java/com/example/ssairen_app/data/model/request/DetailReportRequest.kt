package com.example.ssairen_app.data.model.request

/**
 * 세부사항 섹션 저장 요청 모델
 *
 * API: PATCH /api/emergency-reports/{emergencyReportId}/sections/DETAIL_REPORT
 */
data class DetailReportRequest(
    val data: DetailReportRequestData
)

data class DetailReportRequestData(
    val schemaVersion: Int = 1,
    val detailReport: DetailReportInfo
)

data class DetailReportInfo(
    val doctor: ParamedicMember?,
    val paramedic1: ParamedicMember?,
    val paramedic2: ParamedicMember?,
    val driver: ParamedicMember?,
    val other: ParamedicMember?,
    val obstacles: List<ObstacleItem>,
    val createdAt: String,
    val updatedAt: String
)

data class ParamedicMember(
    val affiliation: String? = null,
    val name: String,
    val grade: String? = null,
    val rank: String? = null,
    val signature: String? = null
)

data class ObstacleItem(
    val type: String,
    val isCustom: Boolean,
    val value: String? = null
)
