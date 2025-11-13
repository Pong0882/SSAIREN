package com.example.ssairen_app.data.dto

import com.google.gson.annotations.SerializedName

/**
 * STT 구조화된 응답 DTO
 * /api/files/stt/local/full-to-json 응답
 */
data class SttResponse(
    @SerializedName("ReportSectionType")
    val reportSectionType: ReportSectionType
)

data class ReportSectionType(
    @SerializedName("patientInfo")
    val patientInfo: PatientInfo,

    @SerializedName("dispatch")
    val dispatch: Dispatch,

    @SerializedName("incidentType")
    val incidentType: IncidentType,

    @SerializedName("assessment")
    val assessment: Assessment
)

// ==========================================
// 환자 정보
// ==========================================
data class PatientInfo(
    @SerializedName("reporter")
    val reporter: Reporter,

    @SerializedName("patient")
    val patient: Patient,

    @SerializedName("guardian")
    val guardian: Guardian,

    @SerializedName("incidentLocation")
    val incidentLocation: IncidentLocation
)

data class Reporter(
    @SerializedName("phone")
    val phone: String?,

    @SerializedName("reportMethod")
    val reportMethod: String?,

    @SerializedName("value")
    val value: Any?
)

data class Patient(
    @SerializedName("name")
    val name: String?,

    @SerializedName("gender")
    val gender: String?,

    @SerializedName("ageYears")
    val ageYears: Int?,

    @SerializedName("birthDate")
    val birthDate: String?,

    @SerializedName("address")
    val address: String?
)

data class Guardian(
    @SerializedName("name")
    val name: String?,

    @SerializedName("relation")
    val relation: String?,

    @SerializedName("phone")
    val phone: String?
)

data class IncidentLocation(
    @SerializedName("text")
    val text: String?
)

// ==========================================
// 출동 정보
// ==========================================
data class Dispatch(
    @SerializedName("reportDatetime")
    val reportDatetime: String?,

    @SerializedName("departureTime")
    val departureTime: String?,

    @SerializedName("arrivalSceneTime")
    val arrivalSceneTime: String?,

    @SerializedName("contactTime")
    val contactTime: String?,

    @SerializedName("distanceKm")
    val distanceKm: Double?,

    @SerializedName("departureSceneTime")
    val departureSceneTime: String?,

    @SerializedName("arrivalHospitalTime")
    val arrivalHospitalTime: String?,

    @SerializedName("returnTime")
    val returnTime: String?,

    @SerializedName("dispatchType")
    val dispatchType: String?,

    @SerializedName("sceneLocation")
    val sceneLocation: SceneLocation,

    @SerializedName("symptoms")
    val symptoms: Symptoms
)

data class SceneLocation(
    @SerializedName("name")
    val name: String?,

    @SerializedName("value")
    val value: Any?
)

data class Symptoms(
    @SerializedName("disease")
    val disease: List<String>?,

    @SerializedName("trauma")
    val trauma: List<String>?,

    @SerializedName("otherSymptoms")
    val otherSymptoms: List<String>?
)

// ==========================================
// 사고 유형
// ==========================================
data class IncidentType(
    @SerializedName("medicalHistory")
    val medicalHistory: MedicalHistory,

    @SerializedName("category")
    val category: String?,

    @SerializedName("subCategory")
    val subCategory: SubCategory,

    @SerializedName("legalSuspicion")
    val legalSuspicion: LegalSuspicion
)

data class MedicalHistory(
    @SerializedName("status")
    val status: String?,

    @SerializedName("items")
    val items: List<String>?
)

data class SubCategory(
    @SerializedName("type")
    val type: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("value")
    val value: Any?
)

data class LegalSuspicion(
    @SerializedName("name")
    val name: String?
)

// ==========================================
// 평가
// ==========================================
data class Assessment(
    @SerializedName("consciousness")
    val consciousness: Consciousness,

    @SerializedName("pupilReaction")
    val pupilReaction: PupilReaction,

    @SerializedName("vitalSigns")
    val vitalSigns: VitalSigns,

    @SerializedName("patientLevel")
    val patientLevel: String?,

    @SerializedName("notes")
    val notes: Notes
)

data class Consciousness(
    @SerializedName("first")
    val first: ConsciousnessState,

    @SerializedName("second")
    val second: ConsciousnessState
)

data class ConsciousnessState(
    @SerializedName("time")
    val time: String?,

    @SerializedName("state")
    val state: String?
)

data class PupilReaction(
    @SerializedName("left")
    val left: PupilState,

    @SerializedName("right")
    val right: PupilState
)

data class PupilState(
    @SerializedName("status")
    val status: String?,

    @SerializedName("reaction")
    val reaction: String?
)

data class VitalSigns(
    @SerializedName("available")
    val available: Boolean?,

    @SerializedName("first")
    val first: VitalSignsData,

    @SerializedName("second")
    val second: VitalSignsData
)

data class VitalSignsData(
    @SerializedName("time")
    val time: String?,

    @SerializedName("bloodPressure")
    val bloodPressure: String?,

    @SerializedName("pulse")
    val pulse: Int?,

    @SerializedName("respiration")
    val respiration: Int?,

    @SerializedName("temperature")
    val temperature: Double?,

    @SerializedName("spo2")
    val spo2: Int?,

    @SerializedName("bloodSugar")
    val bloodSugar: Int?
)

data class Notes(
    @SerializedName("cheifComplaint")
    val cheifComplaint: String?,

    @SerializedName("onset")
    val onset: String?,

    @SerializedName("note")
    val note: String?
)