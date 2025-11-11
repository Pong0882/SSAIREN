// data/model/request/PatientTypeRequest.kt
package com.example.ssairen_app.data.model.request

data class PatientTypeRequest(
    val data: PatientTypeRequestData
)

data class PatientTypeRequestData(
    val schemaVersion: Int = 1,
    val incidentType: IncidentTypeContent
)

data class IncidentTypeContent(
    val medicalHistory: MedicalHistory?,
    val category: String?,
    val subCategory_traffic: SubCategoryTraffic?,
    val subCategory_injury: SubCategoryInjury?,
    val subCategory_nonTrauma: SubCategoryNonTrauma?,
    val category_other: String?,
    val subCategory_other: SubCategoryOther?,
    val legalSuspicion: LegalSuspicion?,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class MedicalHistory(
    val status: String?,
    val items: List<MedicalItem>?
)

data class MedicalItem(
    val name: String,
    val value: String? = null
)

data class LegalSuspicion(
    val name: String?
)

data class SubCategoryTraffic(
    val type: String = "교통사고",
    val name: String?,
    val value: String? = null
)

data class SubCategoryInjury(
    val type: String = "그 외 손상",
    val name: String?
)

data class SubCategoryNonTrauma(
    val type: String = "비외상성 손상",
    val name: String?,
    val value: String?
)

data class SubCategoryOther(
    val name: String?,
    val value: String? = null
)