// AuthViewModel.kt
package com.example.ssairen_app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.repository.AuthRepository
import com.example.ssairen_app.data.websocket.DispatchMessage
import com.example.ssairen_app.data.websocket.WebSocketManager
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authManager = AuthManager(application)

    private val repository = AuthRepository(
        authManager = authManager,
        context = application
    )

    companion object {
        private const val TAG = "AuthViewModel"
        private const val BASE_URL = "http://localhost:9090"  // WebSocket용 BASE_URL
    }

    init {
        // WebSocketManager 초기화
        WebSocketManager.init(BASE_URL)
    }

    // 로그인 상태
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    // 로그인 여부
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    // ✅ 로그아웃 상태 추가
    private val _logoutState = MutableLiveData<LogoutState>()
    val logoutState: LiveData<LogoutState> = _logoutState

    // ✅ WebSocket 연결 성공 LiveData 추가
    private val _webSocketConnected = MutableLiveData<Boolean>()
    val webSocketConnected: LiveData<Boolean> = _webSocketConnected

    // ✅ 수신된 출동 메시지 LiveData 추가
    private val _dispatchMessage = MutableLiveData<DispatchMessage?>()
    val dispatchMessage: LiveData<DispatchMessage?> = _dispatchMessage

    init {
        checkLoginStatus()
    }

    // DB에서 로그인 상태 확인
    fun checkLoginStatus() {
        _isLoggedIn.value = repository.isLoggedIn()
    }

    // 로그인
    fun login(studentNumber: String, password: String) {
        // 입력값 검증
        if (studentNumber.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("학번과 비밀번호를 입력해주세요")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val result = repository.login(studentNumber, password)
            result.onSuccess { loginData ->
                _loginState.value = LoginState.Success(loginData)
                _isLoggedIn.value = true

                // ✅ 로그인 성공 시 WebSocket 연결
                connectWebSocket(loginData.accessToken, loginData.userId.toLong())
            }.onFailure { error ->
                _loginState.value = LoginState.Error(error.message ?: "로그인 실패")
                _isLoggedIn.value = false
            }
        }
    }

    // ✅ 로그아웃 (API 호출 포함)
    fun logout() {
        viewModelScope.launch {
            _logoutState.value = LogoutState.Loading
            Log.d(TAG, "🚪 로그아웃 시작...")

            try {
                // ✅ WebSocket 연결 해제
                disconnectWebSocket()

                val result = repository.logout()

                result.onSuccess { message ->
                    _isLoggedIn.value = false
                    _loginState.value = LoginState.Idle
                    _logoutState.value = LogoutState.Success(message)
                    Log.d(TAG, "✅ 로그아웃 완료: $message")
                }.onFailure { error ->
                    // 실패해도 로컬은 로그아웃 처리됨
                    _isLoggedIn.value = false
                    _loginState.value = LoginState.Idle
                    _logoutState.value = LogoutState.Error(error.message ?: "로그아웃 실패")
                    Log.w(TAG, "⚠️ 로그아웃 경고: ${error.message}")
                }
            } catch (e: Exception) {
                // 예외 발생해도 로컬은 로그아웃 처리
                _isLoggedIn.value = false
                _loginState.value = LoginState.Idle
                _logoutState.value = LogoutState.Error(e.message ?: "로그아웃 오류")
                Log.e(TAG, "❌ 로그아웃 예외", e)
            }
        }
    }

    // ✅ WebSocket 연결
    private fun connectWebSocket(accessToken: String, paramedicId: Long) {
        Log.d(TAG, "🔌 Connecting WebSocket for paramedic ID: $paramedicId")

        WebSocketManager.connect(
            accessToken = accessToken,
            paramedicId = paramedicId,
            onDispatchReceived = { dispatch ->
                Log.d(TAG, "📩 Dispatch received: $dispatch")
                // ✅ 출동 메시지를 LiveData로 전달 (MainActivity에서 관찰)
                _dispatchMessage.postValue(dispatch)
            },
            onError = { error ->
                Log.e(TAG, "❌ WebSocket error: $error")
                _webSocketConnected.postValue(false)
            },
            onConnectionStatusChanged = { connected ->
                Log.d(TAG, "🔌 WebSocket connection status: $connected")
                _webSocketConnected.postValue(connected)
            }
        )
    }

    // ✅ 출동 메시지 처리 완료 (모달 띄운 후 호출)
    fun clearDispatchMessage() {
        _dispatchMessage.value = null
    }

    // ✅ WebSocket 연결 해제
    private fun disconnectWebSocket() {
        Log.d(TAG, "🔌 Disconnecting WebSocket...")
        WebSocketManager.disconnect()
        _webSocketConnected.value = false
    }

    // ViewModel 종료 시 WebSocket 연결 해제
    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }
}

// ✅ 로그아웃 상태 sealed class 추가
sealed class LogoutState {
    object Idle : LogoutState()
    object Loading : LogoutState()
    data class Success(val message: String) : LogoutState()
    data class Error(val message: String) : LogoutState()
}