// FirstAidResponse.kt
package com.example.ssairen_app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 응급처치 섹션 조회 응답
 * GET /api/emergency-reports/{id}/sections/TREATMENT
 */
data class FirstAidResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: FirstAidSectionData,

    @SerializedName("timestamp")
    val timestamp: String
)

data class FirstAidSectionData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("emergencyReportId")
    val emergencyReportId: Int,

    @SerializedName("type")
    val type: String,  // "TREATMENT"

    @SerializedName("data")
    val data: FirstAidDataWrapper,

    @SerializedName("version")
    val version: Int,

    @SerializedName("createdAt")
    val createdAt: String
)

data class FirstAidDataWrapper(
    @SerializedName("schemaVersion")
    val schemaVersion: Int,

    @SerializedName("emergencyTreatment")
    val emergencyTreatment: EmergencyTreatmentData
)

data class EmergencyTreatmentData(
    @SerializedName("airwayManagement")
    val airwayManagement: AirwayManagementData?,

    @SerializedName("oxygenTherapy")
    val oxygenTherapy: OxygenTherapyData?,

    @SerializedName("cpr")
    val cpr: CPRData?,

    @SerializedName("bleedingControl")
    val bleedingControl: BleedingControlData?,

    @SerializedName("woundCare")
    val woundCare: WoundCareData?,

    @SerializedName("delivery")
    val delivery: DeliveryData?,

    @SerializedName("notes")
    val notes: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class AirwayManagementData(
    @SerializedName("methods")
    val methods: List<String>?,  // ["기도유지", "두부후굴", ...]

    @SerializedName("notes")
    val notes: String?
)

data class OxygenTherapyData(
    @SerializedName("applied")
    val applied: Boolean?,  // true/false

    @SerializedName("flowRateLpm")
    val flowRateLpm: Int?,  // 10

    @SerializedName("device")
    val device: String?  // "비재호흡마스크", "비강캐뉼라", "백밸브마스크"
)

data class CPRData(
    @SerializedName("performed")
    val performed: Boolean?,  // true/false

    @SerializedName("type")
    val type: String?,  // "1회 시행", "다회 시행", "DNR", "중단"

    @SerializedName("aed")
    val aed: AEDData?
)

data class AEDData(
    @SerializedName("used")
    val used: Boolean?,  // true/false

    @SerializedName("shock")
    val shock: Boolean?,  // true/false (전기충격 실시)

    @SerializedName("monitoring")
    val monitoring: Boolean?  // true/false (모니터링만)
)

data class BleedingControlData(
    @SerializedName("methods")
    val methods: List<String>?,  // ["직접압박", "지혈", ...]

    @SerializedName("notes")
    val notes: String?
)

data class WoundCareData(
    @SerializedName("types")
    val types: List<String>?,  // ["상처 소독 처리", "붕대", "드레싱", ...]

    @SerializedName("notes")
    val notes: String?
)

data class DeliveryData(
    @SerializedName("performed")
    val performed: Boolean?,  // true/false

    @SerializedName("time")
    val time: String?,  // "02:45"

    @SerializedName("babyCondition")
    val babyCondition: String?  // "양호", "불량"
)