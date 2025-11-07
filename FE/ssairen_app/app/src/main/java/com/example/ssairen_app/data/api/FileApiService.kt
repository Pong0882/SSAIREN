package com.example.ssairen_app.data.api

import com.example.ssairen_app.data.dto.ApiResponse
import com.example.ssairen_app.data.dto.FileUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
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
}
