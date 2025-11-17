// PatientEvaResponse.kt
package com.example.ssairen_app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 환자평가 섹션 조회 응답
 * GET /api/emergency-reports/{id}/sections/ASSESSMENT
 */
data class PatientEvaResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: PatientEvaSectionData,

    @SerializedName("message")
    val message: String?,

    @SerializedName("timestamp")
    val timestamp: String
)

data class PatientEvaSectionData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("emergencyReportId")
    val emergencyReportId: Int,

    @SerializedName("type")
    val type: String,  // "ASSESSMENT"

    @SerializedName("data")
    val data: PatientEvaDataWrapper,

    @SerializedName("version")
    val version: Int,

    @SerializedName("createdAt")
    val createdAt: String
)

data class PatientEvaDataWrapper(
    @SerializedName("schemaVersion")
    val schemaVersion: Int,

    @SerializedName("assessment")  // ✅ patientAssessment → assessment로 수정
    val assessment: PatientAssessmentData
)

data class PatientAssessmentData(
    @SerializedName("consciousness")
    val consciousness: ConsciousnessData?,

    @SerializedName("pupilReaction")
    val pupilReaction: PupilReactionData?,

    @SerializedName("vitalSigns")
    val vitalSigns: VitalSignsData?,

    @SerializedName("patientLevel")
    val patientLevel: String?,  // "LEVEL1", "LEVEL2", "LEVEL3"

    @SerializedName("notes")  // ✅ 객체 구조로 수정
    val notes: NotesData?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

// ✅ Notes 객체 추가
data class NotesData(
    @SerializedName("note")
    val note: String?,

    @SerializedName("onset")
    val onset: String?,  // 발병 시각

    @SerializedName("cheifComplaint")
    val cheifComplaint: String?  // 주 호소 (오타는 API 그대로 유지)
)

data class ConsciousnessData(
    @SerializedName("first")
    val first: ConsciousnessCheck?,

    @SerializedName("second")
    val second: ConsciousnessCheck?
)

data class ConsciousnessCheck(
    @SerializedName("time")
    val time: String?,  // "02:30"

    @SerializedName("state")
    val state: String?  // "A" (Alert), "V" (Verbal), "P" (Painful), "U" (Unresponsive)
)

data class PupilReactionData(
    @SerializedName("left")
    val left: PupilCheck?,

    @SerializedName("right")
    val right: PupilCheck?
)

data class PupilCheck(
    @SerializedName("status")
    val status: String?,  // "정상", "확대", "축소"

    @SerializedName("reaction")
    val reaction: String?  // "반응", "지연", "무반응"
)

data class VitalSignsData(
    @SerializedName("first")
    val first: VitalSignsMeasurement?,

    @SerializedName("second")
    val second: VitalSignsMeasurement?
)

data class VitalSignsMeasurement(
    @SerializedName("time")
    val time: String?,  // "02:35"

    @SerializedName("bloodPressure")
    val bloodPressure: String?,  // "120/80"

    @SerializedName("pulse")
    val pulse: Int?,  // 80

    @SerializedName("respiration")
    val respiration: Int?,  // 18

    @SerializedName("temperature")
    val temperature: Double?,  // 36.8

    @SerializedName("spo2")
    val spo2: Int?,  // 98

    @SerializedName("bloodSugar")
    val bloodSugar: Int?  // 110
)