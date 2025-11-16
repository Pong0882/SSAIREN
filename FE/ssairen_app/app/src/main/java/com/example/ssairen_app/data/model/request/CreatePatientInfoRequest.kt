package com.example.ssairen_app.data.model.request

import com.google.gson.annotations.SerializedName

/**
 * 환자 정보 생성 API 요청 모델
 * POST /api/patient-info
 */
data class CreatePatientInfoRequest(
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
