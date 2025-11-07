package com.example.ssairen_app.data.api

import com.google.gson.annotations.SerializedName

/**
 * 토큰 응답 DTO
 */
data class TokenResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String?,

    @SerializedName("userType")
    val userType: String,

    @SerializedName("userId")
    val userId: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("tokenType")
    val tokenType: String = "Bearer",

    @SerializedName("name")
    val name: String? = null
)
