package com.example.ssairen_app.utils

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * STT 상태를 전역으로 관리하는 싱글톤 객체
 * 화면 전환 시에도 녹음 상태가 유지됨
 */
object SttManager {
    private const val TAG = "SttManager"

    // STT 녹음 상태
    var isSttRecording by mutableStateOf(false)
        private set

    // 누적된 텍스트
    var sttText by mutableStateOf("")
        private set

    // SpeechToTextHelper 인스턴스
    var sttHelper: SpeechToTextHelper? = null
        private set

    /**
     * STT 헬퍼 초기화
     */
    fun initializeSttHelper(
        context: Context,
        onResult: (String) -> Unit,
        onPartialResult: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (sttHelper == null) {
            sttHelper = SpeechToTextHelper(
                context = context,
                onResult = { text ->
                    sttText = text
                    onResult(text)
                    Log.d(TAG, "📝 STT Result: $text")
                },
                onPartialResult = { text ->
                    onPartialResult(text)
                    Log.d(TAG, "📝 STT Partial: $text")
                },
                onError = { error ->
                    onError(error)
                    Log.e(TAG, "❌ STT Error: $error")
                }
            )
            Log.d(TAG, "✅ SttHelper initialized")
        }
    }

    /**
     * STT 녹음 시작
     */
    fun startRecording() {
        isSttRecording = true
        sttHelper?.startListening()
        Log.d(TAG, "🎤 STT Recording Started")
    }

    /**
     * STT 녹음 중지
     * @return 마지막 누적된 텍스트
     */
    fun stopRecording(): String {
        sttHelper?.stopListening()
        isSttRecording = false
        Log.d(TAG, "🛑 STT Recording Stopped")

        // 녹음 중지 전에 마지막 텍스트 가져오기
        val finalText = getAccumulatedText()
        Log.d(TAG, "📝 최종 누적 텍스트: $finalText")

        // 녹음 중지 후 누적 텍스트 초기화
        sttHelper?.clearAccumulatedText()
        sttText = ""

        return finalText
    }

    /**
     * 누적된 텍스트 가져오기
     */
    fun getAccumulatedText(): String {
        return sttHelper?.getAccumulatedText() ?: sttText
    }

    /**
     * 텍스트 업데이트
     */
    fun updateSttText(text: String) {
        sttText = text
    }

    /**
     * 리소스 정리
     */
    fun cleanup() {
        sttHelper?.destroy()
        sttHelper = null
        isSttRecording = false
        sttText = ""
        Log.d(TAG, "🗑️ SttManager cleaned up")
    }
}
