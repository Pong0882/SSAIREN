// CreateReportResponse.kt
package com.example.ssairen_app.data.model.response

data class CreateReportResponse(
    val success: Boolean,
    val data: CreatedReportData?,
    val message: String?,
    val error: ApiError?,
    val timestamp: String
)

data class CreatedReportData(
    val emergencyReportId: Int,
    val paramedicInfo: ParamedicInfo,
    val dispatchInfo: DispatchInfo,
    val createdAt: String
)

data class ParamedicInfo(
    val paramedicId: Int,
    val name: String,
    val rank: String,
    val fireStateId: Int,
    val studentNumber: String
)

data class DispatchInfo(
    val dispatchId: Int,
    val disasterNumber: String,
    val disasterType: String,
    val locationAddress: String,
    val date: String
)