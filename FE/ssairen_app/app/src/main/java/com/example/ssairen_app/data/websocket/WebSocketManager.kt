package com.example.ssairen_app.data.websocket

import android.util.Log
import com.google.gson.Gson
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompHeader

/**
 * WebSocket (STOMP) 연결 관리자
 *
 * 사용법:
 * 1. WebSocketManager.init(baseUrl) - 초기화
 * 2. connect(accessToken, paramedicId) - 연결 및 구독
 * 3. disconnect() - 연결 해제
 *
 * 예시:
 * ```
 * WebSocketManager.init("http://localhost:9090")
 * WebSocketManager.connect(
 *     accessToken = "your-jwt-token",
 *     paramedicId = 123,
 *     onDispatchReceived = { dispatch ->
 *         // 출동 지령 수신 처리
 *     }
 * )
 * ```
 */
object WebSocketManager {

    private const val TAG = "WebSocketManager"

    // WebSocket 엔드포인트
    private var wsUrl = "ws://localhost:9090/ws"

    // STOMP 클라이언트
    private var stompClient: StompClient? = null

    // RxJava Disposables
    private val compositeDisposable = CompositeDisposable()

    // Gson (JSON 파싱용)
    private val gson = Gson()

    // 연결 상태
    private var isConnected = false

    /**
     * WebSocket 초기화
     * @param baseUrl HTTP URL (예: "http://localhost:9090")
     */
    fun init(baseUrl: String) {
        // HTTP URL을 WebSocket URL로 변환
        wsUrl = baseUrl.replace("http://", "ws://")
            .replace("https://", "wss://") + "/ws"

        Log.d(TAG, "WebSocket URL initialized: $wsUrl")
    }

    /**
     * WebSocket 연결 및 채널 구독
     *
     * @param accessToken JWT 액세스 토큰
     * @param paramedicId 구급대원 ID
     * @param onDispatchReceived 출동 지령 수신 콜백
     * @param onError 에러 콜백
     * @param onConnectionStatusChanged 연결 상태 변경 콜백
     */
    fun connect(
        accessToken: String,
        paramedicId: Long,
        onDispatchReceived: (DispatchMessage) -> Unit,
        onError: (String) -> Unit = {},
        onConnectionStatusChanged: (Boolean) -> Unit = {}
    ) {
        if (isConnected) {
            Log.w(TAG, "WebSocket already connected")
            return
        }

        try {
            // STOMP 클라이언트 생성
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl)

            // STOMP 헤더 설정 (JWT 토큰 포함)
            val headers = listOf(
                StompHeader("Authorization", "Bearer $accessToken")
            )

            // 연결 상태 모니터링
            val lifecycleDisposable: Disposable = stompClient!!.lifecycle()
                .subscribe { lifecycleEvent ->
                    when (lifecycleEvent.type) {
                        ua.naiksoftware.stomp.dto.LifecycleEvent.Type.OPENED -> {
                            Log.d(TAG, "✅ WebSocket OPENED")
                            isConnected = true
                            onConnectionStatusChanged(true)
                        }
                        ua.naiksoftware.stomp.dto.LifecycleEvent.Type.CLOSED -> {
                            Log.d(TAG, "❌ WebSocket CLOSED")
                            isConnected = false
                            onConnectionStatusChanged(false)
                        }
                        ua.naiksoftware.stomp.dto.LifecycleEvent.Type.ERROR -> {
                            Log.e(TAG, "⚠️ WebSocket ERROR: ${lifecycleEvent.exception?.message}")
                            isConnected = false
                            onConnectionStatusChanged(false)
                            onError(lifecycleEvent.exception?.message ?: "WebSocket connection error")
                        }
                        ua.naiksoftware.stomp.dto.LifecycleEvent.Type.FAILED_SERVER_HEARTBEAT -> {
                            Log.w(TAG, "💔 WebSocket FAILED_SERVER_HEARTBEAT")
                        }
                        else -> {
                            Log.d(TAG, "WebSocket lifecycle: ${lifecycleEvent.type}")
                        }
                    }
                }
            compositeDisposable.add(lifecycleDisposable)

            // 채널 구독: /topic/paramedic.{paramedicId}
            val destination = "/topic/paramedic.$paramedicId"
            Log.d(TAG, "Subscribing to channel: $destination")

            val topicDisposable: Disposable = stompClient!!.topic(destination)
                .subscribe({ stompMessage ->
                    try {
                        val payload = stompMessage.payload
                        Log.d(TAG, "📩 Message received from $destination: $payload")

                        // JSON 파싱
                        val dispatch = gson.fromJson(payload, DispatchMessage::class.java)
                        onDispatchReceived(dispatch)

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Failed to parse message: ${e.message}")
                        onError("메시지 파싱 실패: ${e.message}")
                    }
                }, { error ->
                    Log.e(TAG, "❌ Topic subscription error: ${error.message}")
                    onError("구독 오류: ${error.message}")
                })
            compositeDisposable.add(topicDisposable)

            // STOMP 연결 시작
            stompClient!!.connect(headers)
            Log.d(TAG, "🔌 Connecting to WebSocket...")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to connect WebSocket: ${e.message}")
            onError("WebSocket 연결 실패: ${e.message}")
        }
    }

    /**
     * WebSocket 연결 해제
     */
    fun disconnect() {
        try {
            Log.d(TAG, "🔌 Disconnecting WebSocket...")

            // STOMP 연결 해제
            stompClient?.disconnect()

            // Disposables 정리
            compositeDisposable.clear()

            isConnected = false
            stompClient = null

            Log.d(TAG, "✅ WebSocket disconnected")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to disconnect WebSocket: ${e.message}")
        }
    }

    /**
     * 연결 상태 확인
     */
    fun isConnected(): Boolean = isConnected

    /**
     * 메시지 전송 (필요시 사용)
     * @param destination 전송할 경로 (예: "/app/dispatch/accept")
     * @param data 전송할 데이터
     */
    fun <T> sendMessage(destination: String, data: T) {
        if (!isConnected) {
            Log.w(TAG, "⚠️ Cannot send message: WebSocket not connected")
            return
        }

        try {
            val json = gson.toJson(data)
            stompClient?.send(destination, json)?.subscribe(
                {
                    Log.d(TAG, "✅ Message sent to $destination: $json")
                },
                { error ->
                    Log.e(TAG, "❌ Failed to send message: ${error.message}")
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error sending message: ${e.message}")
        }
    }
}

/**
 * 출동 지령 메시지 데이터 클래스
 * WebSocket으로 수신되는 출동 데이터 (백엔드 JSON 형식)
 */
data class DispatchMessage(
    val fireStateId: Int,                    // 소방서 ID
    val paramedicId: Int,                    // 구급대원 ID
    val disasterNumber: String,              // 재난 번호 (출동 ID로 사용)
    val disasterType: String,                // 재난 유형 (화재, 구조 등)
    val disasterSubtype: String? = null,     // 재난 세부 유형
    val reporterName: String? = null,        // 신고자 이름
    val reporterPhone: String? = null,       // 신고자 전화번호
    val locationAddress: String,             // 출동 위치
    val incidentDescription: String? = null, // 사건 설명
    val dispatchLevel: String? = null,       // 출동 등급 (실전, 대응 등)
    val dispatchOrder: Int? = null,          // 출동 순서
    val dispatchStation: String? = null,     // 출동 센터
    val date: String? = null                 // 발생 시간
)
