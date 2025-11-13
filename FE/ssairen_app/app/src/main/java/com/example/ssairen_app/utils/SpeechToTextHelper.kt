package com.example.ssairen_app.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

/**
 * Android Speech Recognition을 사용한 STT 헬퍼 클래스
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

    companion object {
        private const val TAG = "SpeechToTextHelper"
    }

    init {
        // SpeechRecognizer 인스턴스 생성
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        // RecognizerIntent 설정
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.KOREAN)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
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
                onError(errorMessage)

                // 에러 발생 시 자동으로 재시작 (타임아웃이나 매치 없음 제외)
                if (error != SpeechRecognizer.ERROR_NO_MATCH &&
                    error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT &&
                    isListening) {
                    restartListening()
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    Log.d(TAG, "📝 Final result: $text")

                    // 누적된 텍스트에 추가
                    if (accumulatedText.isNotEmpty()) {
                        accumulatedText.append(" ")
                    }
                    accumulatedText.append(text)

                    onResult(accumulatedText.toString())

                    // 계속 듣기 위해 재시작
                    if (isListening) {
                        restartListening()
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    Log.d(TAG, "📝 Partial result: $text")
                    onPartialResult(text)
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
        Thread.sleep(100) // 짧은 딜레이
        speechRecognizer?.startListening(recognizerIntent)
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
