package com.example.ssairen_app.data.api

import com.example.ssairen_app.data.dto.ApiResponse
import com.example.ssairen_app.data.dto.FileUploadResponse
import com.example.ssairen_app.data.model.request.LoginRequest
import com.example.ssairen_app.data.dto.SttResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * 파일 업로드 API 서비스
 */
interface FileApiService {

    /**
     * 로그인
     *
     * @param loginRequest 로그인 요청 (사용자 타입, 사용자명, 비밀번호)
     * @return 토큰 응답 (accessToken, refreshToken 등)
     */
    @POST("/api/auth/login")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): Response<ApiResponse<TokenResponse>>

    /**
     * 토큰 갱신
     *
     * @param refreshToken 갱신 토큰 (Bearer 포함)
     * @return 새로운 토큰 응답 (accessToken, refreshToken 등)
     */
    @POST("/api/auth/refresh")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<ApiResponse<TokenResponse>>

    /**
     * 비디오 파일 업로드
     *
     * @param file 업로드할 비디오 파일 (Multipart)
     * @return 업로드 결과 (파일 정보)
     */
    @Multipart
    @POST("/api/files/upload-video")
    suspend fun uploadVideo(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<FileUploadResponse>>


    /**
     * 오디오 파일 업로드 및 STT 변환
     *
     * @param file 업로드할 오디오 파일 (Multipart)
     * @return 업로드 결과 + STT 변환된 텍스트
     */
    @Multipart
    @POST("/api/files/stt/local/full-to-json")
    suspend fun uploadAudioAndGetStructuredData(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<SttResponse>>

    /**
     * 텍스트를 JSON으로 변환
     *
     * @param text 변환할 텍스트 내용
     * @param maxNewTokens 최대 생성 토큰 수 (기본값: 700)
     * @param temperature 생성 온도 (0.0 ~ 1.0, 기본값: 0.1)
     * @return 구조화된 JSON 데이터
     */
    @POST("/api/files/text-to-json")
    suspend fun textToJson(
        @retrofit2.http.Query("text") text: String,
        @retrofit2.http.Query("maxNewTokens") maxNewTokens: Int = 700,
        @retrofit2.http.Query("temperature") temperature: Double = 0.1
    ): Response<ApiResponse<SttResponse>>
}
