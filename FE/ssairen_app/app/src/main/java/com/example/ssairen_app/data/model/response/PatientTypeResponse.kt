// PatientTypeResponse.kt
package com.example.ssairen_app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 환자발생유형 섹션 조회 응답
 * GET /api/emergency-reports/{id}/sections/INCIDENT_TYPE
 */
data class PatientTypeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: PatientTypeSectionData,

    @SerializedName("timestamp")
    val timestamp: String
)

data class PatientTypeSectionData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("emergencyReportId")
    val emergencyReportId: Int,

    @SerializedName("type")
    val type: String,  // "INCIDENT_TYPE"

    @SerializedName("data")
    val data: PatientTypeDataWrapper,

    @SerializedName("version")
    val version: Int,

    @SerializedName("createdAt")
    val createdAt: String
)

data class PatientTypeDataWrapper(
    @SerializedName("schemaVersion")
    val schemaVersion: Int,

    @SerializedName("incidentType")
    val incidentType: IncidentTypeData
)

data class IncidentTypeData(
    @SerializedName("category")
    val category: String?,  // "질병", "질병외", "기타"

    @SerializedName("medicalHistory")
    val medicalHistory: MedicalHistoryData?,

    @SerializedName("externalCause")
    val externalCause: ExternalCauseData?,

    @SerializedName("trauma")
    val trauma: TraumaData?,

    @SerializedName("legalSuspicion")
    val legalSuspicion: LegalSuspicionData?,

    @SerializedName("other")
    val other: OtherData?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class MedicalHistoryData(
    @SerializedName("hasHistory")
    val hasHistory: String?,  // "있음", "없음", "미상"

    @SerializedName("details")
    val details: List<String>?,  // ["고혈압", "당뇨", "뇌혈관질환", ...]

    @SerializedName("notes")
    val notes: String?
)

data class ExternalCauseData(
    @SerializedName("type")
    val type: String?,  // "교통사고", "그 외 외상", "비외상성 손상"

    @SerializedName("subType")
    val subType: String?,  // "운전자", "동승자", "보행자", "자전거", "오토바이"

    @SerializedName("injurySeverity")
    val injurySeverity: String?  // "사상자"
)

data class TraumaData(
    @SerializedName("mainCause")
    val mainCause: List<String>?,  // ["절식", "기계", "화상", ...]

    @SerializedName("notes")
    val notes: String?
)

data class LegalSuspicionData(
    @SerializedName("isSuspected")
    val isSuspected: Boolean?,

    @SerializedName("actions")
    val actions: List<String>?,  // ["경찰통보", "경찰입회", "긴급이송", "관련기관 통보"]

    @SerializedName("notes")
    val notes: String?
)

data class OtherData(
    @SerializedName("category")
    val category: String?,  // "자연재해", "임신분만", "신생아", "단순구조", "기타"

    @SerializedName("notes")
    val notes: String?
)