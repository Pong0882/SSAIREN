package com.example.ssairen_app.data.dto

import com.google.gson.annotations.SerializedName

/**
 * 백엔드 API 공통 응답 형식
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: T? = null,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: ErrorResponse? = null,

    @SerializedName("status")
    val status: Int? = null,

    @SerializedName("timestamp")
    val timestamp: String? = null
)

/**
 * 에러 응답
 */
data class ErrorResponse(
    @SerializedName("code")
    val code: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("details")
    val details: List<ErrorDetail>? = null
)

/**
 * 에러 상세
 */
data class ErrorDetail(
    @SerializedName("field")
    val field: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("rejectedValue")
    val rejectedValue: Any?
)
