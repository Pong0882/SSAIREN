// ReportListResponse.kt
package com.example.ssairen_app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 보고서 목록 조회 API 응답
 */
data class ReportListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: MyReportsData?,

    @SerializedName("message")
    val message: String,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("error")
    val error: ApiError? = null
)

data class MyReportsData(
    @SerializedName("paramedicInfo")
    val paramedicInfo: MyParamedicInfo,

    @SerializedName("emergencyReports")
    val emergencyReports: List<MyEmergencyReport>
)

data class MyParamedicInfo(
    @SerializedName("paramedicId")
    val paramedicId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("rank")
    val rank: String,

    @SerializedName("fireStateId")
    val fireStateId: Int,

    @SerializedName("studentNumber")
    val studentNumber: String
)

// ✅ 중복 제거하고 isCompleted 필드 추가
data class MyEmergencyReport(
    @SerializedName("id")
    val id: Int,

    @SerializedName("dispatchInfo")
    val dispatchInfo: MyDispatchInfo,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("isCompleted")  // ✅ 추가
    val isCompleted: Boolean = false  // 기본값: 작성중
)

data class MyDispatchInfo(
    @SerializedName("dispatchId")
    val dispatchId: Int,

    @SerializedName("disasterNumber")
    val disasterNumber: String,

    @SerializedName("disasterType")
    val disasterType: String,

    @SerializedName("locationAddress")
    val locationAddress: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("fireStateInfo")
    val fireStateInfo: MyFireStateInfo
)

data class MyFireStateInfo(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

// ✅ 작성 완료 API 응답 (기존 구조 재활용)
data class CompleteReportResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: CompleteReportData?,

    @SerializedName("message")
    val message: String,

    @SerializedName("timestamp")
    val timestamp: String
)

data class CompleteReportData(
    @SerializedName("emergencyReportId")
    val emergencyReportId: Int,

    @SerializedName("isCompleted")
    val isCompleted: Boolean
)