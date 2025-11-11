// data/model/request/PatientInfoRequest.kt
package com.example.ssairen_app.data.model.request

data class PatientInfoRequest(
    val data: PatientInfoRequestData
)

data class PatientInfoRequestData(
    val schemaVersion: Int = 1,
    val patientInfo: PatientInfoContent
)

data class PatientInfoContent(
    val reporter: ReporterInfo?,
    val patient: PatientInfoDetail?,
    val guardian: GuardianInfo?,
    val incidentLocation: IncidentLocation?,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class ReporterInfo(
    val phone: String?,
    val reportMethod: String?,
    val value: String? = null
)

data class PatientInfoDetail(
    val name: String?,
    val gender: String?,
    val ageYears: Int?,
    val birthDate: String?,
    val address: String?
)

data class GuardianInfo(
    val name: String?,
    val relation: String?,
    val phone: String?
)

data class IncidentLocation(
    val text: String?
)