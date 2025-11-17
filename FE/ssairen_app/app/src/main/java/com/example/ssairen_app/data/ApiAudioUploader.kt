package com.example.ssairen_app.data

import android.util.Log
import com.example.ssairen_app.data.api.RetrofitClient
import com.example.ssairen_app.data.dto.SttResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * 백엔드 API를 통한 오디오 업로더
 * ApiVideoUploader와 동일한 방식으로 백엔드 API를 호출합니다.
 * API: /api/files/stt/local/full-to-json
 */
class ApiAudioUploader {

    companion object {
        private const val TAG = "ApiAudioUploader"
    }

    /**
     * 오디오 파일을 백엔드 API를 통해 업로드 및 STT 구조화된 데이터 받기
     *
     * @param file 업로드할 오디오 파일 (.m4a, .wav, .mp3)
     * @param emergencyReportId 구급일지 ID
     * @param onProgress 업로드 진행률 콜백 (0~100)
     * @return 업로드 성공 시 파일 정보 + STT 구조화된 데이터
     */
    suspend fun uploadAudio(
        file: File,
        emergencyReportId: Long,
        onProgress: ((Int) -> Unit)? = null
    ): Result<AudioUploadResult> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: ${file.absolutePath}")
                return@withContext Result.failure(Exception("파일이 존재하지 않습니다."))
            }

            Log.d(TAG, "Starting audio upload: ${file.name}, size: ${file.length()} bytes")
            onProgress?.invoke(0)

            // 세션 폴더 경로 포함한 파일명 생성 (비디오와 동일한 구조)
            // 예: 2025-01-06/12:30:45_김민지/12:30:45.m4a
            val parentFolder = file.parentFile  // 시간_사용자명 폴더
            val grandParentFolder = parentFolder?.parentFile  // 날짜 폴더

            val fileNameWithPath = if (grandParentFolder != null &&
                grandParentFolder.name.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                // 날짜 폴더 구조 감지됨
                "${grandParentFolder.name}/${parentFolder?.name}/${file.name}"
            } else {
                // 단일 파일 또는 구조 없음
                file.name
            }
            Log.d(TAG, "Upload file path: $fileNameWithPath")

            // Multipart 요청 생성
            // ✅ 파일 확장자에 따라 Content-Type 자동 설정
            val contentType = when (file.extension.lowercase()) {
                "wav" -> "audio/wav"
                "m4a" -> "audio/mp4"
                "mp3" -> "audio/mpeg"
                else -> "audio/*"
            }.toMediaType()

            val requestBody = file.asRequestBody(contentType)
            val multipartBody = MultipartBody.Part.createFormData(
                "file",
                fileNameWithPath,
                requestBody
            )

            // API 호출 - /api/files/stt/local/full-to-json
            val response = RetrofitClient.fileApiService.uploadAudioAndGetStructuredData(
                multipartBody,
                emergencyReportId
            )

            onProgress?.invoke(100)

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!

                if (apiResponse.success && apiResponse.data != null) {
                    val sttData = apiResponse.data
                    Log.d(TAG, "Upload successful: ${file.name}")
                    Log.d(TAG, "STT Data received: Patient Name = ${sttData.reportSectionType.patientInfo.patient.name}")

                    Result.success(
                        AudioUploadResult(
                            fileName = file.name,
                            originalFileName = file.name,
                            fileSize = file.length(),
                            fileUrl = "",  // STT API는 파일 URL을 반환하지 않음
                            bucketName = "",  // STT API는 버킷 정보를 반환하지 않음
                            sttData = sttData
                        )
                    )
                } else {
                    val errorMsg = apiResponse.error?.message ?: "업로드 실패"
                    Log.e(TAG, "Upload failed: $errorMsg")
                    Result.failure(Exception(errorMsg))
                }
            } else {
                val errorMsg = "HTTP ${response.code()}: ${response.message()}"
                Log.e(TAG, "Upload failed: $errorMsg")
                Result.failure(Exception("업로드 실패: $errorMsg"))
            }

        } catch (e: Exception) {
            Log.e(TAG, "Upload exception", e)
            Result.failure(e)
        }
    }

    /**
     * 백엔드 API 연결 테스트
     */
    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // JWT 토큰 확인
            val token = try {
                RetrofitClient.getAuthManager().getAccessToken()
            } catch (e: Exception) {
                null
            }

            if (token.isNullOrEmpty()) {
                Log.e(TAG, "JWT token is not set")
                Result.failure(Exception("인증 토큰이 설정되지 않았습니다."))
            } else {
                Log.d(TAG, "JWT token is set, connection test passed")
                Result.success(true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Connection test failed", e)
            Result.failure(e)
        }
    }

    /**
     * 파일 업로드 후 로컬 파일 삭제
     */
    fun deleteLocalFile(file: File): Boolean {
        return try {
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "Local file deleted: ${file.absolutePath}")

                    // 빈 폴더도 정리
                    file.parentFile?.let { parent ->
                        if (parent.listFiles()?.isEmpty() == true) {
                            parent.delete()
                            Log.d(TAG, "Empty parent folder deleted: ${parent.name}")
                        }
                    }
                }
                deleted
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file", e)
            false
        }
    }
}

/**
 * 오디오 업로드 결과
 */
data class AudioUploadResult(
    val fileName: String,
    val originalFileName: String,
    val fileSize: Long,
    val fileUrl: String,
    val bucketName: String,
    val sttData: SttResponse? = null  // STT 구조화된 데이터
)