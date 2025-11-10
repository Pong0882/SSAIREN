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

    @SerializedName("patientAssessment")
    val patientAssessment: PatientAssessmentData
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

    @SerializedName("notes")
    val notes: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
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