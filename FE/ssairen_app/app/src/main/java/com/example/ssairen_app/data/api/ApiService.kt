//ApiService
package com.example.ssairen_app.data.api

import com.example.ssairen_app.data.model.response.LogoutResponse
import com.example.ssairen_app.data.model.request.LoginRequest
import com.example.ssairen_app.data.model.response.LoginResponse
import com.example.ssairen_app.data.model.response.CreateReportResponse
import com.example.ssairen_app.data.model.response.ReportListResponse
import com.example.ssairen_app.data.model.response.PatientInfoResponse
import com.example.ssairen_app.data.model.response.PatientTypeResponse
import com.example.ssairen_app.data.model.response.PatientEvaResponse
import com.example.ssairen_app.data.model.response.FirstAidResponse


import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==========================================
    // 인증 API
    // ==========================================

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<LogoutResponse>

    // ✅ 토큰 갱신 API 추가
    @POST("api/auth/refresh")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<LoginResponse>

    // ==========================================
    // 보고서 API
    // ==========================================

    // ✅ 임시로 주석처리 - API 대신 모달창에서 직접 이동
//    @POST("api/emergency-reports/{dispatch_id}")
//    suspend fun createReport(
//        @Path("dispatch_id") dispatchId: Int,
//        @Header("Authorization") token: String
//    ): Response<CreateReportResponse>

    @GET("api/emergency-reports/me")
    suspend fun getReports(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<ReportListResponse>

    // ==========================================
    // 섹션 조회 API
    // ==========================================

    // 환자정보
    @GET("api/emergency-reports/{emergencyReportId}/sections/PATIENT_INFO")
    suspend fun getEmergencyReportSection(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String
    ): Response<PatientInfoResponse>

    // 환자발생유형
    @GET("api/emergency-reports/{emergencyReportId}/sections/INCIDENT_TYPE")
    suspend fun getPatientType(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String
    ): Response<PatientTypeResponse>
//
    // 환자평가
    @GET("api/emergency-reports/{id}/sections/ASSESSMENT")
    suspend fun getPatientEva(
        @Path("id") emergencyReportId: Int,
        @Header("Authorization") token: String  // ✅ Bearer 토큰 헤더 추가
    ): Response<PatientEvaResponse>
//
    // 응급처치
    @GET("api/emergency-reports/{emergencyReportId}/sections/TREATMENT")
    suspend fun getFirstAid(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String
    ): Response<FirstAidResponse>
}