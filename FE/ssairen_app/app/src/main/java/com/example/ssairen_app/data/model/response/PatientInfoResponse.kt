package com.example.ssairen_app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * API 응답 최상위 래퍼
 */
data class PatientInfoResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: PatientInfoSection,

    @SerializedName("timestamp")
    val timestamp: String
)

/**
 * Section 정보 (id, emergencyReportId, type 등 포함)
 */
data class PatientInfoSection(
    @SerializedName("id")
    val id: Int,

    @SerializedName("emergencyReportId")
    val emergencyReportId: Int,

    @SerializedName("type")
    val type: String, // "PATIENT_INFO"

    @SerializedName("data")
    val data: PatientInfoData,

    @SerializedName("version")
    val version: Int,

    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * 실제 환자 정보 데이터 (schemaVersion, patientInfo 포함)
 */
data class PatientInfoData(
    @SerializedName("schemaVersion")
    val schemaVersion: Int,

    @SerializedName("patientInfo")
    val patientInfo: PatientInfo
)

/**
 * 환자 정보 상세
 */
data class PatientInfo(
    @SerializedName("reporter")
    val reporter: Reporter?,

    @SerializedName("patient")
    val patient: Patient?,

    @SerializedName("guardian")
    val guardian: Guardian?,

    @SerializedName("incidentLocation")
    val incidentLocation: IncidentLocation?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

/**
 * 신고자 정보
 */
data class Reporter(
    @SerializedName("phone")
    val phone: String?,

    @SerializedName("reportMethod")
    val reportMethod: String? // 예: "휴대전화"
)

/**
 * 환자 정보
 */
data class Patient(
    @SerializedName("name")
    val name: String?,

    @SerializedName("gender")
    val gender: String?, // 예: "남성", "여성"

    @SerializedName("ageYears")
    val ageYears: Int?,

    @SerializedName("birthDate")
    val birthDate: String?, // 예: "1980-02-12"

    @SerializedName("address")
    val address: String?
)

/**
 * 보호자 정보
 */
data class Guardian(
    @SerializedName("name")
    val name: String?,

    @SerializedName("relation")
    val relation: String?, // 예: "배우자"

    @SerializedName("phone")
    val phone: String?
)

/**
 * 사고 발생 장소
 */
data class IncidentLocation(
    @SerializedName("text")
    val text: String?
)