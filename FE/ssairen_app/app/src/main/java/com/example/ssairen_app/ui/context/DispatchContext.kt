//DispatchContext.kt
package com.example.ssairen_app.ui.context

import androidx.compose.runtime.*
import com.example.ssairen_app.data.websocket.DispatchMessage
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// 출동 아이템 데이터 클래스
// ==========================================
data class DispatchItem(
    val id: String,
    val type: String,
    val date: String,
    val location: String,
    val isActive: Boolean = false,
    val emergencyReportId: Int = 0  // ✅ 출동보고서 ID 추가
)

// ==========================================
// Dispatch 상태 클래스
// ==========================================
class DispatchState {
    private var _activeDispatch by mutableStateOf<DispatchItem?>(null)
    val activeDispatch: DispatchItem?
        get() = _activeDispatch

    private var _showDispatchModal by mutableStateOf(false)  // ⬅️ false로 변경
    val showDispatchModal: Boolean
        get() = _showDispatchModal

    fun setActiveDispatch(dispatch: DispatchItem?) {
        _activeDispatch = dispatch
    }

    fun setShowDispatchModal(show: Boolean) {
        _showDispatchModal = show
    }

    fun closeDispatchModal() {
        _showDispatchModal = false
    }

    fun simulateNewDispatch() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)
        val currentDate = dateFormat.format(Date())

        val newDispatch = DispatchItem(
            id = "22",
            type = "구급출동 | 긴급",
            date = currentDate,
            location = "서울특별시 강남구 테헤란로 212 거리 3.2km",
            isActive = true,
            emergencyReportId = 23  // ✅ 23번으로 하드코딩
        )

        _activeDispatch = newDispatch
        _showDispatchModal = true
    }

    fun completeDispatch() {
        _activeDispatch = null
        _showDispatchModal = false
    }

    // ✅ WebSocket 메시지로부터 출동 생성
    fun createDispatchFromWebSocket(message: DispatchMessage) {
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
            id = message.disasterNumber,           // 재난 번호를 ID로 사용
            type = typeString,                     // "화재 | 실전 - 고층건물"
            date = formattedDate,                  // "2025-11-09 09:16"
            location = message.locationAddress,    // 출동 위치
            isActive = true
        )

        _activeDispatch = newDispatch
        _showDispatchModal = true
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
    initialShowModal: Boolean = false,
    autoCreateDispatch: Boolean = false,  // ⬅️ 추가
    content: @Composable () -> Unit
) {
    val dispatchState = remember {
        DispatchState().apply {
            if (autoCreateDispatch) {
                simulateNewDispatch()  // ⬅️ 자동으로 출동 생성
            } else {
                setShowDispatchModal(initialShowModal)
            }
        }
    }

    CompositionLocalProvider(LocalDispatchState provides dispatchState) {
        content()
    }
}

// ==========================================
// Hook 함수 (useDispatch와 동일)
// ==========================================
@Composable
fun rememberDispatchState(): DispatchState {
    return LocalDispatchState.current
}