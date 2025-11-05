//DispatchContext.kt
package com.example.ssairen_app.ui.context

import androidx.compose.runtime.*
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
    val isActive: Boolean = false
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
            id = "CB${System.currentTimeMillis()}",
            type = "구급출동 | 긴급",
            date = currentDate,
            location = "서울특별시 강남구 테헤란로 212 거리 3.2km",
            isActive = true
        )

        _activeDispatch = newDispatch
        _showDispatchModal = true
    }

    fun completeDispatch() {
        _activeDispatch = null
        _showDispatchModal = false
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
    autoCreateDispatch: Boolean = true,  // ⬅️ 추가
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