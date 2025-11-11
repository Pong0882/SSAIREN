package com.example.ssairen_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ssairen_app.data.websocket.DispatchMessage
import com.example.ssairen_app.data.websocket.WebSocketManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * WebSocket 상태 관리 ViewModel
 *
 * 사용법:
 * ```
 * val viewModel: WebSocketViewModel = viewModel()
 *
 * // WebSocket 연결
 * viewModel.connect(accessToken, paramedicId)
 *
 * // 출동 지령 수신 관찰
 * val dispatchMessage by viewModel.dispatchMessage.collectAsState()
 * dispatchMessage?.let { dispatch ->
 *     // 출동 지령 처리
 * }
 *
 * // 연결 상태 관찰
 * val isConnected by viewModel.isConnected.collectAsState()
 *
 * // WebSocket 연결 해제
 * viewModel.disconnect()
 * ```
 */
class WebSocketViewModel : ViewModel() {

    companion object {
        private const val TAG = "WebSocketViewModel"
    }

    // 연결 상태
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // 수신된 출동 지령
    private val _dispatchMessage = MutableStateFlow<DispatchMessage?>(null)
    val dispatchMessage: StateFlow<DispatchMessage?> = _dispatchMessage.asStateFlow()

    // 에러 메시지
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 연결 시도 횟수 (재연결 로직용)
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5

    /**
     * WebSocket 연결
     *
     * @param accessToken JWT 액세스 토큰
     * @param paramedicId 구급대원 ID
     */
    fun connect(accessToken: String, paramedicId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔌 Attempting to connect WebSocket for paramedic ID: $paramedicId")

                WebSocketManager.connect(
                    accessToken = accessToken,
                    paramedicId = paramedicId,
                    onDispatchReceived = { dispatch ->
                        Log.d(TAG, "📩 Dispatch received: $dispatch")
                        _dispatchMessage.value = dispatch
                    },
                    onError = { error ->
                        Log.e(TAG, "❌ WebSocket error: $error")
                        _errorMessage.value = error
                        _isConnected.value = false

                        // 재연결 로직
                        handleReconnect(accessToken, paramedicId)
                    },
                    onConnectionStatusChanged = { connected ->
                        Log.d(TAG, "🔌 Connection status changed: $connected")
                        _isConnected.value = connected

                        if (connected) {
                            // 연결 성공 시 재연결 카운터 초기화
                            reconnectAttempts = 0
                            _errorMessage.value = null
                        }
                    }
                )

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to connect WebSocket: ${e.message}")
                _errorMessage.value = "WebSocket 연결 실패: ${e.message}"
                _isConnected.value = false
            }
        }
    }

    /**
     * WebSocket 연결 해제
     */
    fun disconnect() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🔌 Disconnecting WebSocket...")
                WebSocketManager.disconnect()
                _isConnected.value = false
                _dispatchMessage.value = null
                reconnectAttempts = 0

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to disconnect WebSocket: ${e.message}")
            }
        }
    }

    /**
     * 재연결 로직
     */
    private fun handleReconnect(accessToken: String, paramedicId: Long) {
        if (reconnectAttempts >= maxReconnectAttempts) {
            Log.e(TAG, "❌ Max reconnect attempts reached. Giving up.")
            _errorMessage.value = "WebSocket 재연결 실패 (최대 시도 횟수 초과)"
            return
        }

        reconnectAttempts++
        Log.d(TAG, "🔄 Reconnecting WebSocket... Attempt $reconnectAttempts/$maxReconnectAttempts")

        // 3초 후 재연결 시도
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            connect(accessToken, paramedicId)
        }
    }

    /**
     * 출동 지령 메시지 초기화 (모달 닫을 때 사용)
     */
    fun clearDispatchMessage() {
        _dispatchMessage.value = null
    }

    /**
     * 에러 메시지 초기화
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
     * ViewModel 종료 시 WebSocket 연결 해제
     */
    override fun onCleared() {
        super.onCleared()
        disconnect()
    }
}
