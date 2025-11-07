package com.example.ssairen_app.data

import android.util.Log
import com.example.ssairen_app.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * 백엔드 API를 통한 비디오 업로더
 * 기존 MinIO 직접 업로드 대신 백엔드 API를 호출합니다.
 */
class ApiVideoUploader {

    companion object {
        private const val TAG = "ApiVideoUploader"
    }

    /**
     * 비디오 파일을 백엔드 API를 통해 업로드
     *
     * @param file 업로드할 비디오 파일
     * @param onProgress 업로드 진행률 콜백 (0~100) - 현재는 미사용
     * @return 업로드 성공 시 파일 정보, 실패 시 예외
     */
    suspend fun uploadVideo(
        file: File,
        onProgress: ((Int) -> Unit)? = null
    ): Result<VideoUploadResult> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                Log.e(TAG, "File does not exist: ${file.absolutePath}")
                return@withContext Result.failure(Exception("파일이 존재하지 않습니다."))
            }

            Log.d(TAG, "Starting video upload: ${file.name}, size: ${file.length()} bytes")
            onProgress?.invoke(0)

            // 세션 폴더 경로 포함한 파일명 생성 (예: 2025-01-06/12:30:45/12:30:45_12:37:45.mp4)
            val parentFolder = file.parentFile  // 시작시간 폴더 (예: 12:30:45)
            val grandParentFolder = parentFolder?.parentFile  // 날짜 폴더 (예: 2025-01-06)

            val fileNameWithPath = if (grandParentFolder != null &&
                                       grandParentFolder.name.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
                // 날짜 폴더 구조 감지됨: 2025-01-06/12:30:45/파일명.mp4
                "${grandParentFolder.name}/${parentFolder?.name}/${file.name}"
            } else {
                // 단일 파일 또는 구조 없음
                file.name
            }
            Log.d(TAG, "Upload file path: $fileNameWithPath")

            // Multipart 요청 생성
            val requestBody = file.asRequestBody("video/mp4".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "file",
                fileNameWithPath,  // 폴더 경로 포함된 파일명
                requestBody
            )

            // API 호출
            val response = RetrofitClient.fileApiService.uploadVideo(multipartBody)

            onProgress?.invoke(100)

            if (response.isSuccessful && response.body() != null) {
                val apiResponse = response.body()!!

                if (apiResponse.success && apiResponse.data != null) {
                    val uploadData = apiResponse.data
                    Log.d(TAG, "Upload successful: ${uploadData.fileName}")

                    Result.success(
                        VideoUploadResult(
                            fileName = uploadData.fileName,
                            originalFileName = uploadData.originalFileName,
                            fileSize = uploadData.fileSize,
                            fileUrl = uploadData.fileUrl,
                            bucketName = uploadData.bucketName
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
     * JWT 토큰이 필요하므로 실제로는 업로드 시도로 테스트
     */
    suspend fun testConnection(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // JWT 토큰 확인
            if (RetrofitClient.jwtToken.isNullOrEmpty()) {
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
     * 파일 업로드 후 로컬 파일 삭제 (선택적)
     */
    fun deleteLocalFile(file: File): Boolean {
        return try {
            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "Local file deleted: ${file.absolutePath}")
                } else {
                    Log.w(TAG, "Failed to delete local file: ${file.absolutePath}")
                }
                deleted
            } else {
                Log.w(TAG, "File does not exist, cannot delete: ${file.absolutePath}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file", e)
            false
        }
    }
}

/**
 * 비디오 업로드 결과
 */
data class VideoUploadResult(
    val fileName: String,
    val originalFileName: String,
    val fileSize: Long,
    val fileUrl: String,
    val bucketName: String
)
