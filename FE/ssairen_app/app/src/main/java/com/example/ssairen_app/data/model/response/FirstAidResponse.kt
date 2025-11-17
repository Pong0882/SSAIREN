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

    @SerializedName("message")
    val message: String,

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
    @SerializedName("treatment")
    val treatment: TreatmentData,

    @SerializedName("schemaVersion")
    val schemaVersion: Int
)

data class TreatmentData(
    @SerializedName("airwayManagement")
    val airwayManagement: AirwayManagementData?,

    @SerializedName("oxygenTherapy")
    val oxygenTherapy: OxygenTherapyData?,

    @SerializedName("cpr")
    val cpr: String?,  // "실시", "미실시" 등

    @SerializedName("aed")
    val aed: AEDData?,

    @SerializedName("ecg")
    val ecg: Boolean?,  // 심전도 모니터링

    @SerializedName("circulation")
    val circulation: CirculationData?,  // 순환 관리

    @SerializedName("woundCare")
    val woundCare: String?,  // "지혈", "상처 소독 처리" 등

    @SerializedName("fixed")
    val fixed: String?,  // 고정 부위: "목뼈", "척추" 등

    @SerializedName("drug")
    val drug: String?,  // 약물 투여

    @SerializedName("temperature")
    val temperature: String?,  // 체온 관리

    @SerializedName("deliverytime")
    val deliverytime: String?,  // 분만 시간

    @SerializedName("notes")
    val notes: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

data class AirwayManagementData(
    @SerializedName("methods")
    val methods: List<String>?  // ["기도유지", "두부후굴", "하악거상" 등]
)

data class OxygenTherapyData(
    @SerializedName("device")
    val device: String?,  // "비재호흡마스크", "비강캐뉼라", "백밸브마스크"

    @SerializedName("flowRateLpm")
    val flowRateLpm: Int?  // 산소 유량 (L/min)
)

data class AEDData(
    @SerializedName("type")
    val type: String?,  // "shock" (전기충격), "monitoring" (모니터링만)

    @SerializedName("value")
    val value: String?  // 추가 정보 (현재는 null)
)

data class CirculationData(
    @SerializedName("type")
    val type: String?,  // "수액공급 확보", "정맥로 확보" 등

    @SerializedName("value")
    val value: String?  // 수액량 등: "200", "500" 등
)