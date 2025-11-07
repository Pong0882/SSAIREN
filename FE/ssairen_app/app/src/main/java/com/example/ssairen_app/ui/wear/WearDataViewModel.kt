// WearDataViewModel.kt
package com.example.ssairen_app.ui.wear

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Wear OS에서 전송된 심박수 및 산소포화도 데이터를 관리하는 ViewModel
 */
class WearDataViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "WearDataViewModel"
        private const val MAX_HISTORY_SIZE = 50 // 최대 50개 데이터 포인트

        // Singleton 인스턴스 (Service에서 접근용)
        private var instance: WearDataViewModel? = null

        fun getInstance(application: Application): WearDataViewModel {
            return instance ?: WearDataViewModel(application).also {
                instance = it
                Log.d(TAG, "✅ WearDataViewModel 인스턴스 생성")
            }
        }

        fun getInstanceOrNull(): WearDataViewModel? = instance
    }

    // ========= 심박수 상태 =========
    private val _heartRate = MutableStateFlow(0)
    val heartRate: StateFlow<Int> = _heartRate.asStateFlow()

    // ========= 심박수 히스토리 (그래프용) =========
    private val _heartRateHistory = MutableStateFlow<List<Int>>(emptyList())
    val heartRateHistory: StateFlow<List<Int>> = _heartRateHistory.asStateFlow()

    // ========= 산소포화도 상태 =========
    private val _spo2 = MutableStateFlow(0)
    val spo2: StateFlow<Int> = _spo2.asStateFlow()

    // ========= SpO2 에러 메시지 =========
    private val _spo2ErrorMessage = MutableStateFlow("")
    val spo2ErrorMessage: StateFlow<String> = _spo2ErrorMessage.asStateFlow()

    // ========= HR 재정비 상태 메시지 =========
    private val _hrStatusMessage = MutableStateFlow("")
    val hrStatusMessage: StateFlow<String> = _hrStatusMessage.asStateFlow()

    // ========= 연결 상태 =========
    private val _isWearConnected = MutableStateFlow(false)
    val isWearConnected: StateFlow<Boolean> = _isWearConnected.asStateFlow()

    // ========= 마지막 업데이트 시간 =========
    private val _lastUpdateTime = MutableStateFlow(0L)
    val lastUpdateTime: StateFlow<Long> = _lastUpdateTime.asStateFlow()

    init {
        Log.d(TAG, "🚀 WearDataViewModel init 블록 시작")

        // WearDataService 시작
        val serviceIntent = android.content.Intent(application, WearDataService::class.java)
        application.startService(serviceIntent)

        Log.d(TAG, "✅ WearDataService 시작 요청 완료")
    }

    /**
     * Wear에서 전송된 심박수 업데이트
     * WearDataService에서 호출됨
     */
    fun updateHeartRate(hr: Int) {
        Log.d(TAG, "💓 updateHeartRate 호출됨: $hr BPM")
        viewModelScope.launch {
            _heartRate.value = hr
            _lastUpdateTime.value = System.currentTimeMillis()

            // 히스토리에 추가 (0보다 큰 값만)
            if (hr > 0) {
                val currentHistory = _heartRateHistory.value.toMutableList()
                currentHistory.add(hr)

                // 최대 개수 초과 시 가장 오래된 데이터 제거
                if (currentHistory.size > MAX_HISTORY_SIZE) {
                    currentHistory.removeAt(0)
                }

                _heartRateHistory.value = currentHistory
                Log.d(TAG, "📊 히스토리 업데이트: ${currentHistory.size}개 데이터 포인트")
            }

            Log.d(TAG, "✅ StateFlow 업데이트 완료: heartRate = ${_heartRate.value}")
        }
    }

    /**
     * Wear에서 전송된 산소포화도 업데이트
     * WearDataService에서 호출됨
     */
    fun updateSpO2(value: Int) {
        Log.d(TAG, "💨 updateSpO2 호출됨: $value%")
        viewModelScope.launch {
            _spo2.value = value
            _lastUpdateTime.value = System.currentTimeMillis()
            Log.d(TAG, "✅ StateFlow 업데이트 완료: spo2 = ${_spo2.value}%")
        }
    }

    /**
     * SpO2 에러 메시지 업데이트
     * WearDataService에서 호출됨
     */
    fun updateSpo2ErrorMessage(message: String) {
        Log.d(TAG, "⚠️ updateSpo2ErrorMessage 호출됨: $message")
        viewModelScope.launch {
            _spo2ErrorMessage.value = message
            Log.d(TAG, "✅ StateFlow 업데이트 완료: spo2ErrorMessage = '$message'")
        }
    }

    /**
     * HR 상태 메시지 업데이트 (재정비 중 등)
     * WearDataService에서 호출됨
     */
    fun updateHrStatusMessage(message: String) {
        Log.d(TAG, "🔧 updateHrStatusMessage 호출됨: $message")
        viewModelScope.launch {
            _hrStatusMessage.value = message
            Log.d(TAG, "✅ StateFlow 업데이트 완료: hrStatusMessage = '$message'")
        }
    }

    /**
     * Wear 연결 상태 업데이트
     */
    fun updateConnectionStatus(isConnected: Boolean) {
        viewModelScope.launch {
            _isWearConnected.value = isConnected
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 WearDataViewModel onCleared 호출")
        // 서비스 정리는 Application에서 관리
    }
}
