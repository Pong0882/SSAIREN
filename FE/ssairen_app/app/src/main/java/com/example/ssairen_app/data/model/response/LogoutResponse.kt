// LogoutResponse.kt
package com.example.ssairen_app.data.model.response

data class LogoutResponse(
    val success: Boolean,
    val data: String?,
    val message: String?,
    val error: ApiError?,
    val status: Int,
    val timestamp: String
)

data class ApiError(
    val code: String?,
    val message: String?,
    val details: List<ErrorDetail>?
)

data class ErrorDetail(
    val field: String?,
    val message: String?,
    val rejectedValue: String?
)