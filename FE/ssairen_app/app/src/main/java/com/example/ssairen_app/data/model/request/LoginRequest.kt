package com.example.ssairen_app.data.model.request

data class LoginRequest(
    val userType: String,
    val username: String,
    val password: String,
    val fcmToken: String? = null
)