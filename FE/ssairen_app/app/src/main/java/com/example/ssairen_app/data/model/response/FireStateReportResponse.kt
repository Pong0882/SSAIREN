// FireStateReportResponse.kt
package com.example.ssairen_app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 관내 보고서 목록 조회 API 응답
 */
data class FireStateReportResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: List<FireStateReportData>?,

    @SerializedName("message")
    val message: String,

    @SerializedName("timestamp")
    val timestamp: String
)

data class FireStateReportData(
    @SerializedName("fireStateInfo")
    val fireStateInfo: FireStateInfoData,

    @SerializedName("emergencyReports")
    val emergencyReports: List<EmergencyReportData>
)

data class FireStateInfoData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

data class EmergencyReportData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("paramedicInfo")
    val paramedicInfo: ParamedicInfoData,

    @SerializedName("dispatchInfo")
    val dispatchInfo: DispatchInfoData,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("updatedAt")
    val updatedAt: String
)

data class ParamedicInfoData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

data class DispatchInfoData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("disasterNumber")
    val disasterNumber: String,

    @SerializedName("disasterType")
    val disasterType: String,

    @SerializedName("disasterSubtype")
    val disasterSubtype: String,

    @SerializedName("locationAddress")
    val locationAddress: String,

    @SerializedName("date")
    val date: String
)
