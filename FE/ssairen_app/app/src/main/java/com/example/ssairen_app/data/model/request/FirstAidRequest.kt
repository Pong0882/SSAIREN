// data/model/request/FirstAidRequest.kt
package com.example.ssairen_app.data.model.request

data class FirstAidRequest(
    val data: FirstAidRequestData
)

data class FirstAidRequestData(
    val schemaVersion: Int = 1,
    val treatment: TreatmentContent
)

data class TreatmentContent(
    val airwayManagement: AirwayManagement?,
    val oxygenTherapy: OxygenTherapy?,
    val cpr: String?,
    val ecg: Boolean?,
    val aed: AedData?,
    val notes: String?,
    val circulation: CirculationData?,
    val drug: DrugData?,
    val fixed: String?,
    val woundCare: String?,
    val deliverytime: String?,
    val temperature: String?,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class AirwayManagement(
    val methods: List<String>?
)

data class OxygenTherapy(
    val flowRateLpm: Int?,
    val device: String?
)

data class AedData(
    val type: String?,
    val value: String? = null
)

data class CirculationData(
    val type: String?,
    val value: String?
)

data class DrugData(
    val name: String?,
    val dosage: String?,
    val time: String?
)