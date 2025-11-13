//DispatchContext.kt
package com.example.ssairen_app.ui.context

import android.util.Log
import androidx.compose.runtime.*
import com.example.ssairen_app.data.websocket.DispatchMessage
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// 출동 아이템 데이터 클래스
// ==========================================
data class DispatchItem(
    val id: String,                    // 재난 번호 (표시용)
    val type: String,                  // 출동 유형
    val date: String,                  // 출동 시간
    val location: String,              // 출동 위치
    val isActive: Boolean = false,     // 활성 상태
    val dispatchId: Int = 0            // 출동 ID (API 호출용) ⬅️ 중요!
)

// ==========================================
// Dispatch 상태 클래스
// ==========================================
class DispatchState {
    private var _activeDispatch by mutableStateOf<DispatchItem?>(null)
    val activeDispatch: DispatchItem?
        get() = _activeDispatch

    private var _showDispatchModal by mutableStateOf(false)
    val showDispatchModal: Boolean
        get() = _showDispatchModal

    /**
     * 출동 모달 닫기
     */
    fun closeDispatchModal() {
        _showDispatchModal = false
        _activeDispatch = null
    }

    /**
     * WebSocket 메시지로부터 출동 생성 (실제 사용)
     */
    fun createDispatchFromWebSocket(message: DispatchMessage) {
        Log.d("DispatchState", "========================================")
        Log.d("DispatchState", "🚨 WebSocket 출동 데이터 처리")
        Log.d("DispatchState", "message.id: ${message.id}")  // ⬅️ API 호출용 ID
        Log.d("DispatchState", "message.disasterNumber: ${message.disasterNumber}")
        Log.d("DispatchState", "========================================")

        // 출동 유형 문자열 생성
        val typeString = buildString {
            append(message.disasterType)
            message.dispatchLevel?.let { append(" | $it") }
            message.disasterSubtype?.let { append(" - $it") }
        }

        // 날짜 포맷 변환 (ISO 8601 → 읽기 쉬운 형식)
        val formattedDate = message.date?.let {
            try {
                val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.KOREA)
                val displayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
                val date = isoFormat.parse(it)
                date?.let { displayFormat.format(it) }
            } catch (e: Exception) {
                it // 파싱 실패 시 원본 반환
            }
        } ?: SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date())

        val newDispatch = DispatchItem(
            id = message.disasterNumber,           // 재난 번호 (표시용)
            type = typeString,                     // "화재 | 실전 - 고층건물"
            date = formattedDate,                  // "2025-11-09 09:16"
            location = message.locationAddress,    // 출동 위치
            isActive = true,
            dispatchId = message.id                // ⬅️⬅️⬅️ 출동 ID (API 호출용)
        )

        _activeDispatch = newDispatch
        _showDispatchModal = true

        Log.d("DispatchState", "✅ 출동 모달 상태 설정 완료")
        Log.d("DispatchState", "   - dispatchId: ${newDispatch.dispatchId}")
        Log.d("DispatchState", "   - showDispatchModal: $_showDispatchModal")
    }
}

// ==========================================
// CompositionLocal 정의
// ==========================================
val LocalDispatchState = compositionLocalOf<DispatchState> {
    error("DispatchState not provided")
}

// ==========================================
// Provider Composable
// ==========================================
@Composable
fun DispatchProvider(
    autoCreateDispatch: Boolean = false,  // 시뮬레이션 사용 여부 (기본: false)
    content: @Composable () -> Unit
) {
    val dispatchState = remember { DispatchState() }

    CompositionLocalProvider(LocalDispatchState provides dispatchState) {
        content()
    }
}

// ==========================================
// Hook 함수
// ==========================================
@Composable
fun rememberDispatchState(): DispatchState {
    return LocalDispatchState.current
}