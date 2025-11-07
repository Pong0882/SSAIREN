package com.example.ssairen_app.data.api

import com.example.ssairen_app.data.model.request.LoginRequest
import com.example.ssairen_app.data.model.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    // suspend fun 비동기 호출
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>
}