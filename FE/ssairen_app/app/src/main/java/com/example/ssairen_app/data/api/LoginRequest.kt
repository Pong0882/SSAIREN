package com.example.ssairen_app.data.api

import com.google.gson.annotations.SerializedName

/**
 * 로그인 요청 DTO
 */
data class LoginRequest(
    @SerializedName("userType")
    val userType: String,  // "PARAMEDIC" or "HOSPITAL"

    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String
)
