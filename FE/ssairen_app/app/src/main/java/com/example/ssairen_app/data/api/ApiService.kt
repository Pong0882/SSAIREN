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
import com.example.ssairen_app.data.model.request.PatientInfoRequest
import com.example.ssairen_app.data.model.request.PatientTypeRequest
import com.example.ssairen_app.data.model.request.PatientEvaRequest
import com.example.ssairen_app.data.model.request.FirstAidRequest
import com.example.ssairen_app.data.model.request.DispatchRequest
import com.example.ssairen_app.data.model.response.DispatchResponse
import com.example.ssairen_app.data.model.request.MedicalGuidanceRequest
import com.example.ssairen_app.data.model.response.MedicalGuidanceResponse
import com.example.ssairen_app.data.model.request.TransportRequest
import com.example.ssairen_app.data.model.response.TransportResponse
import com.example.ssairen_app.data.model.request.DetailReportRequest
import com.example.ssairen_app.data.model.response.DetailReportResponse

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

    // 새 일지 등록 API
    @POST("api/emergency-reports/{dispatch_id}")
    suspend fun createReport(
        @Path("dispatch_id") dispatchId: Int,
        @Header("Authorization") token: String
    ): Response<CreateReportResponse>

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

    // 구급출동
    @GET("api/emergency-reports/{emergencyReportId}/sections/DISPATCH")
    suspend fun getDispatch(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String
    ): Response<DispatchResponse>

    @PATCH("api/emergency-reports/{emergencyReportId}/sections/PATIENT_INFO")
    suspend fun updatePatientInfo(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String,
        @Body request: PatientInfoRequest
    ): Response<PatientInfoResponse>

    @PATCH("api/emergency-reports/{emergencyReportId}/sections/INCIDENT_TYPE")
    suspend fun updatePatientType(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String,
        @Body request: PatientTypeRequest
    ): Response<PatientTypeResponse>

    @PATCH("api/emergency-reports/{emergencyReportId}/sections/ASSESSMENT")
    suspend fun updatePatientEva(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String,
        @Body request: PatientEvaRequest
    ): Response<PatientEvaResponse>

    @PATCH("api/emergency-reports/{emergencyReportId}/sections/TREATMENT")
    suspend fun updateFirstAid(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String,
        @Body request: FirstAidRequest
    ): Response<FirstAidResponse>

    @PATCH("api/emergency-reports/{emergencyReportId}/sections/DISPATCH")
    suspend fun updateDispatch(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String,
        @Body request: DispatchRequest
    ): Response<DispatchResponse>

    // 의료지도
    @GET("api/emergency-reports/{emergencyReportId}/sections/MEDICAL_GUIDANCE")
    suspend fun getMedicalGuidance(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String
    ): Response<MedicalGuidanceResponse>

    @PATCH("api/emergency-reports/{emergencyReportId}/sections/MEDICAL_GUIDANCE")
    suspend fun updateMedicalGuidance(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String,
        @Body request: MedicalGuidanceRequest
    ): Response<MedicalGuidanceResponse>

    // 환자이송
    @GET("api/emergency-reports/{emergencyReportId}/sections/TRANSPORT")
    suspend fun getTransport(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String
    ): Response<TransportResponse>

    @PATCH("api/emergency-reports/{emergencyReportId}/sections/TRANSPORT")
    suspend fun updateTransport(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String,
        @Body request: TransportRequest
    ): Response<TransportResponse>

    // 세부사항
    @GET("api/emergency-reports/{emergencyReportId}/sections/DETAIL_REPORT")
    suspend fun getDetailReport(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String
    ): Response<DetailReportResponse>

    @PATCH("api/emergency-reports/{emergencyReportId}/sections/DETAIL_REPORT")
    suspend fun updateDetailReport(
        @Path("emergencyReportId") emergencyReportId: Int,
        @Header("Authorization") token: String,
        @Body request: DetailReportRequest
    ): Response<DetailReportResponse>

}