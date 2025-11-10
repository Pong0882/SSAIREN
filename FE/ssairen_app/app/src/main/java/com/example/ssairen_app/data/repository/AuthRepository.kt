package com.example.ssairen_app.data.repository

import android.content.Context
import android.util.Log
import com.example.ssairen_app.data.ApiVideoUploader
import com.example.ssairen_app.data.api.RetrofitInstance
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.model.request.LoginRequest
import com.example.ssairen_app.data.model.response.LoginData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthRepository(
    private val authManager: AuthManager,
    private val context: Context
) {

    private val api = RetrofitInstance.apiService

    companion object {
        private const val TAG = "AuthRepository"
    }

    // 로그인 API 호출
    suspend fun login(studentNumber: String, password: String): Result<LoginData> { // <--- 3. 반환 타입 LoginData로 변경
        return try {
            Log.d(TAG, "=== 로그인 시작 ===")
            Log.d(TAG, "학번: $studentNumber")
            Log.d(TAG, "비밀번호 길이: ${password.length}")

            // <--- 4. userType을 포함하여 LoginRequest 생성
            val request = LoginRequest(
                userType = "PARAMEDIC", // JSON 예시에 있던 userType 추가
                username = studentNumber, // 또는 LoginRequest에서 username으로 필드명을 바꿨다면 username = studentNumber
                password = password
            )
            Log.d(TAG, "요청 생성 완료")

            val response = api.login(request)
            Log.d(TAG, "응답 코드: ${response.code()}")
            Log.d(TAG, "응답 성공 여부: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success && body.data != null) {
                    // ✅ 로그인 성공
                    Log.d(TAG, "✅ 로그인 성공!")
                    Log.d(TAG, "Access Token: ${body.data.accessToken.take(20)}...")
                    // <--- 5. paramedic 객체 없이 LoginData에서 바로 name 접근
                    Log.d(TAG, "Paramedic: ${body.data.name}")

                    // Access Token과 Refresh Token 모두 저장
                    authManager.saveLoginInfo(
                        // <--- 6. LoginData에 정의한 username (또는 studentNumber) 필드 사용
                        userId = body.data.username,
                        userName = body.data.name,
                        accessToken = body.data.accessToken,
                        refreshToken = body.data.refreshToken,
                        loginUsername = studentNumber,
                        loginPassword = password,
                        loginUserType = "PARAMEDIC"
                    )

                    // 로그인 성공 시 로컬에 저장된 미업로드 비디오 자동 업로드 (백그라운드)
                    uploadPendingVideosInBackground()

                    // LoginData 객체 전체를 성공 결과로 반환
                    Result.success(body.data)
                } else {
                    // ❌ success=false인 경우
                    val errorMessage = body.error?.message
                        ?: body.message
                        ?: "로그인에 실패했습니다"

                    Log.e(TAG, "❌ 로그인 실패: $errorMessage")
                    if (body.error != null) {
                        Log.e(TAG, "에러 코드: ${body.error.code}")
                    }

                    Result.failure(Exception(errorMessage))
                }
            } else {
                // HTTP 오류
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    401 -> "아이디 또는 비밀번호가 일치하지 않습니다"
                    404 -> "서버를 찾을 수 없습니다"
                    500 -> "서버 내부 오류가 발생했습니다"
                    else -> "서버 오류: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 예외 발생!", e)
            Log.e(TAG, "예외 메시지: ${e.message}")

            val errorMsg = when {
                e.message?.contains("Unable to resolve host") == true ->
                    "인터넷 연결을 확인해주세요"
                e.message?.contains("timeout") == true ->
                    "서버 응답 시간이 초과되었습니다"
                else ->
                    "네트워크 오류: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    // ✅ 로그아웃 API 호출 추가
    suspend fun logout(): Result<String> {
        return try {
            Log.d(TAG, "=== 로그아웃 시작 ===")

            val accessToken = authManager.getAccessToken()

            if (accessToken != null) {
                Log.d(TAG, "Access Token 존재 - API 호출")

                val response = api.logout("Bearer $accessToken")
                Log.d(TAG, "응답 코드: ${response.code()}")

                // API 호출 성공 여부와 관계없이 로컬 데이터 삭제
                authManager.logout()
                Log.d(TAG, "🗑️ 로컬 데이터 삭제 완료")

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!

                    if (body.success) {
                        Log.d(TAG, "✅ 로그아웃 API 성공: ${body.message}")
                        Result.success(body.data ?: body.message ?: "로그아웃 성공")
                    } else {
                        Log.w(TAG, "⚠️ 로그아웃 API 실패: ${body.error?.message}")
                        Result.success("로컬 로그아웃 완료")
                    }
                } else {
                    Log.w(TAG, "⚠️ 로그아웃 API 오류: ${response.code()}")
                    Result.success("로컬 로그아웃 완료")
                }
            } else {
                Log.d(TAG, "ℹ️ Access Token 없음 - 로컬 데이터만 삭제")
                authManager.logout()
                Result.success("로컬 로그아웃 완료")
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 로그아웃 예외 발생", e)
            // 예외 발생해도 로컬 데이터는 삭제
            authManager.logout()
            Result.success("로컬 로그아웃 완료")
        }
    }

    // 로그인 상태 확인
    fun isLoggedIn(): Boolean {
        return authManager.isLoggedIn()
    }

    // 저장된 사용자 ID 가져오기
    fun getSavedUserId(): String? {
        return authManager.getSavedUserId()
    }

    // Access Token 가져오기
    fun getAccessToken(): String? {
        return authManager.getAccessToken()
    }

    // Refresh Token 가져오기
    fun getRefreshToken(): String? {
        return authManager.getRefreshToken()
    }

    /**
     * 로컬에 저장된 미업로드 비디오 파일들을 백그라운드로 자동 업로드
     */
    private fun uploadPendingVideosInBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting pending videos upload after login...")

                val uploader = ApiVideoUploader()
                val result = uploader.uploadPendingVideos(context) { current, total ->
                    Log.d(TAG, "Uploading pending videos: $current/$total")
                }

                result.onSuccess { (success, fail) ->
                    Log.d(TAG, "Pending videos upload completed: success=$success, fail=$fail")
                }.onFailure { error ->
                    Log.e(TAG, "Failed to upload pending videos", error)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during pending videos upload", e)
            }
        }
    }
}