// ui/viewmodel/LoginViewModel.kt
package com.example.ssairen_app.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ssairen_app.data.ApiVideoUploader
import com.example.ssairen_app.data.local.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(private val context: Context) : ViewModel() {

    private val authManager = AuthManager(context)

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun checkAutoLogin(): Boolean {
        return authManager.isAutoLoginEnabled()
    }

    fun login(userId: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            try {
                // TODO: API 호출
                kotlinx.coroutines.delay(500)

                val validUsers = mapOf(
                    "admin" to "1234",
                    "user1" to "pass1"
                )

                if (validUsers[userId] == password) {
                    authManager.saveLoginInfo(userId)
                    _loginState.value = LoginState(isSuccess = true)

                    // 로그인 성공 시 로컬에 남아있는 비디오 자동 업로드
                    uploadPendingVideos()
                } else {
                    _loginState.value = LoginState(
                        errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다."
                    )
                }

            } catch (e: Exception) {
                _loginState.value = LoginState(
                    errorMessage = "네트워크 오류: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        authManager.logout()
    }

    /**
     * 로컬에 저장된 미업로드 비디오 파일들을 백그라운드로 자동 업로드
     */
    private fun uploadPendingVideos() {
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Starting pending videos upload after login...")

                val uploader = ApiVideoUploader()
                val result = uploader.uploadPendingVideos(context) { current, total ->
                    Log.d("LoginViewModel", "Uploading pending videos: $current/$total")
                }

                result.onSuccess { (success, fail) ->
                    Log.d("LoginViewModel", "Pending videos upload completed: success=$success, fail=$fail")
                }.onFailure { error ->
                    Log.e("LoginViewModel", "Failed to upload pending videos", error)
                }

            } catch (e: Exception) {
                Log.e("LoginViewModel", "Error during pending videos upload", e)
            }
        }
    }
}