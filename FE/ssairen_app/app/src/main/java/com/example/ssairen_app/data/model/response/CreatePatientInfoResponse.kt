package com.example.ssairen_app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 환자 정보 생성 API 응답 모델
 * POST /api/patient-info
 */
data class CreatePatientInfoResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: CreatedPatientInfoData?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("timestamp")
    val timestamp: String
)

data class CreatedPatientInfoData(
    @SerializedName("emergencyReportId")
    val emergencyReportId: Int,

    @SerializedName("gender")
    val gender: String?,

    @SerializedName("age")
    val age: Int?,

    @SerializedName("recordTime")
    val recordTime: String,

    @SerializedName("mentalStatus")
    val mentalStatus: String?,

    @SerializedName("chiefComplaint")
    val chiefComplaint: String?,

    @SerializedName("hr")
    val hr: Int?,

    @SerializedName("bp")
    val bp: String?,

    @SerializedName("spo2")
    val spo2: Int?,

    @SerializedName("rr")
    val rr: Int?,

    @SerializedName("bt")
    val bt: Double?,

    @SerializedName("hasGuardian")
    val hasGuardian: Boolean,

    @SerializedName("hx")
    val hx: String?,

    @SerializedName("onsetTime")
    val onsetTime: String?,

    @SerializedName("lnt")
    val lnt: String?
)
