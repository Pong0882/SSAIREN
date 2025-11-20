package com.example.ssairen_app.data.model.response

data class LoginResponse(
    val success: Boolean,
    val data: LoginData? = null,  // 성공 시 이 객체에 데이터가 담깁니다.
    val message: String? = null,
    val error: ErrorData? = null,  // 실패 시 이 객체에 에러 정보가 담깁니다.
    val status: Int? = null,
    val timestamp: String? = null
)

data class LoginData(
    val accessToken: String,
    val refreshToken: String,
    val userType: String,
    val userId: Int,
    val username: String,
    val tokenType: String,
    val name: String,
    val rank: String,
    val status: String,
    val fireStateId: Int,
    val fireStateName: String,
    val officialName: String // 추가된 필드
)

data class ErrorData(
    val code: String,
    val message: String
)