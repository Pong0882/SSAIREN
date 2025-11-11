// data/model/request/PatientEvaRequest.kt
package com.example.ssairen_app.data.model.request

data class PatientEvaRequest(
    val data: PatientEvaRequestData
)

data class PatientEvaRequestData(
    val schemaVersion: Int = 1,
    val assessment: AssessmentContent
)

data class AssessmentContent(
    val consciousness: ConsciousnessData?,
    val pupilReaction: PupilReactionData?,
    val vitalSigns: VitalSignsData?,
    val patientLevel: String?,
    val notes: AssessmentNotes?,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ConsciousnessData(
    val first: ConsciousnessState?,
    val second: ConsciousnessState?
)

data class ConsciousnessState(
    val time: String?,
    val state: String?
)

data class PupilReactionData(
    val left: PupilState?,
    val right: PupilState?
)

data class PupilState(
    val status: String?,
    val reaction: String?
)

data class VitalSignsData(
    val available: String? = null,
    val first: VitalSign?,
    val second: VitalSign?
)

data class VitalSign(
    val time: String?,
    val bloodPressure: String?,
    val pulse: Int?,
    val respiration: Int?,
    val temperature: Double?,
    val spo2: Int?,
    val bloodSugar: Int?
)

data class AssessmentNotes(
    val cheifComplaint: String?,
    val onset: String?,
    val note: String?
)