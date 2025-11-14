package com.example.ssairen_app.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

/**
 * Android Speech Recognition을 사용한 STT 헬퍼 클래스
 * 긴박한 대화 상황에 최적화됨
 */
class SpeechToTextHelper(
    private val context: Context,
    private val onResult: (String) -> Unit,
    private val onPartialResult: (String) -> Unit = {},
    private val onError: (String) -> Unit = {}
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val recognizerIntent: Intent
    private var isListening = false
    private val accumulatedText = StringBuilder()
    private val handler = Handler(Looper.getMainLooper())
    private var lastPartialResult = ""

    companion object {
        private const val TAG = "SpeechToTextHelper"
        private const val RESTART_DELAY = 50L  // 재시작 딜레이를 매우 짧게
    }

    init {
        // SpeechRecognizer 인스턴스 생성
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        // RecognizerIntent 설정 - 긴박한 대화에 최적화
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)  // 부분 결과 활성화
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

            // ✅ 침묵 감지 시간을 짧게 설정 - 빠른 대화 포착
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)

            // 온라인 모드 사용 (정확도 우선)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
        }

        // RecognitionListener 설정
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "✅ Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "🎤 Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // 음성 레벨 변화 (필요시 사용)
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // 버퍼 수신 (필요시 사용)
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "🛑 Speech ended")
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                    SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "권한 부족"
                    SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트워크 타임아웃"
                    SpeechRecognizer.ERROR_NO_MATCH -> "인식 실패"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "음성인식 서비스 사용 중"
                    SpeechRecognizer.ERROR_SERVER -> "서버 에러"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "음성 입력 없음"
                    else -> "알 수 없는 에러: $error"
                }
                Log.e(TAG, "❌ Error: $errorMessage")

                // ✅ 대화 중에는 모든 에러에서 빠르게 재시작 (긴박한 상황 대응)
                if (isListening) {
                    when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH,
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                            // 침묵이나 매칭 실패는 즉시 재시작 (대화 끊김 방지)
                            Log.d(TAG, "⚡ Quick restart for continuous listening")
                            restartListeningFast()
                        }
                        else -> {
                            // 다른 에러도 재시작
                            onError(errorMessage)
                            restartListening()
                        }
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    Log.d(TAG, "📝 Final result: $text")
                    Log.d(TAG, "📝 Last partial result: $lastPartialResult")
                    Log.d(TAG, "📝 Current accumulated: $accumulatedText")

                    // ✅ 최종 결과는 항상 추가 (이미 포함되어 있지 않으면)
                    if (text.isNotBlank()) {
                        val currentAccumulated = accumulatedText.toString()

                        // 이미 같은 텍스트로 끝나지 않으면 추가
                        if (!currentAccumulated.endsWith(text)) {
                            if (accumulatedText.isNotEmpty()) {
                                accumulatedText.append(" ")
                            }
                            accumulatedText.append(text)
                            Log.d(TAG, "✅ Added to accumulated: $text")
                            Log.d(TAG, "✅ New accumulated: $accumulatedText")
                        } else {
                            Log.d(TAG, "⚠️ Already in accumulated, skipping")
                        }

                        onResult(accumulatedText.toString())
                    }

                    lastPartialResult = ""

                    // ✅ 계속 듣기 위해 빠르게 재시작
                    if (isListening) {
                        restartListeningFast()
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]

                    // ✅ 부분 결과를 실시간으로 누적 텍스트에 반영
                    if (text.isNotBlank() && text != lastPartialResult) {
                        Log.d(TAG, "📝 Partial result: $text")
                        lastPartialResult = text

                        // 현재까지 누적 + 부분 결과 표시 (accumulatedText는 수정하지 않고 표시만)
                        val currentDisplay = if (accumulatedText.isEmpty()) {
                            text
                        } else {
                            "$accumulatedText $text"
                        }
                        onResult(currentDisplay)
                        onPartialResult(text)
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // 이벤트 처리 (필요시 사용)
            }
        })
    }

    /**
     * 음성 인식 시작
     */
    fun startListening() {
        if (!isListening) {
            isListening = true
            accumulatedText.clear()
            Log.d(TAG, "🎤 Starting speech recognition")
            speechRecognizer?.startListening(recognizerIntent)
        }
    }

    /**
     * 음성 인식 중지
     */
    fun stopListening() {
        if (isListening) {
            isListening = false
            Log.d(TAG, "🛑 Stopping speech recognition")
            speechRecognizer?.stopListening()
        }
    }

    /**
     * 음성 인식 재시작 (연속 인식을 위해)
     */
    private fun restartListening() {
        speechRecognizer?.cancel()
        handler.postDelayed({
            if (isListening) {
                speechRecognizer?.startListening(recognizerIntent)
            }
        }, 100)
    }

    /**
     * ✅ 매우 빠른 재시작 (긴박한 대화용)
     */
    private fun restartListeningFast() {
        speechRecognizer?.cancel()
        handler.postDelayed({
            if (isListening) {
                speechRecognizer?.startListening(recognizerIntent)
            }
        }, RESTART_DELAY)
    }

    /**
     * 누적된 전체 텍스트 가져오기
     */
    fun getAccumulatedText(): String {
        return accumulatedText.toString()
    }

    /**
     * 누적된 텍스트 초기화
     */
    fun clearAccumulatedText() {
        accumulatedText.clear()
    }

    /**
     * 리소스 정리
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
