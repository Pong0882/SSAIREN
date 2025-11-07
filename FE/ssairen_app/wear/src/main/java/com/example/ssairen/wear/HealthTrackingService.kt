package com.example.ssairen.wear

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.wearable.*
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService as SamsungHealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import kotlinx.coroutines.*
import java.nio.charset.StandardCharsets

private const val TAG = "HealthTrackingService"
private const val NOTIFICATION_ID = 1001
private const val CHANNEL_ID = "health_tracking_channel"

// Data Layer 경로
private const val HR_MSG_PATH = "/hr_msg"
private const val HR_DATA_PATH = "/heart_rate"
private const val HR_KEY = "heart_rate_value"
private const val SPO2_MSG_PATH = "/spo2_msg"
private const val SPO2_DATA_PATH = "/spo2"
private const val SPO2_KEY = "spo2_value"

// 전송 제어
private const val MIN_DELTA = 1
private const val MIN_INTERVAL_MS = 1_000L
private const val PERIODIC_SPO2_INTERVAL_MS = 300_000L   // 5분

class HealthTrackingForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // WakeLock (화면 꺼져도 센서 유지)
    private lateinit var wakeLock: PowerManager.WakeLock

    // Data Layer
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }

    // Samsung Health Tracking
    private lateinit var trackingService: SamsungHealthTrackingService
    private var heartRateTracker: HealthTracker? = null
    private var spo2Tracker: HealthTracker? = null
    private var isHrTracking = false
    private var isSpo2Tracking = false

    // HR 전송 제어
    private var lastSentHr: Int? = null
    private var lastSentAt: Long = 0L

    // SpO2 주기 측정
    private var periodicSpo2Job: Job? = null
    private var isPeriodicSpo2Active = false

    companion object {
        // UI 업데이트를 위한 콜백 (MainActivity에서 설정)
        var onHeartRateUpdate: ((Int) -> Unit)? = null
        var onSpo2Update: ((Int) -> Unit)? = null
        var onStatusUpdate: ((String) -> Unit)? = null
        var onConnectionStateUpdate: ((String) -> Unit)? = null
        var isServiceRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
        isServiceRunning = true

        // WakeLock 획득 (화면을 어둡게 유지하여 센서 작동)
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.SCREEN_DIM_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "HealthTracking::WakeLock"
        ).apply {
            acquire()
            Log.d(TAG, "⭐ SCREEN_DIM_WAKE_LOCK acquired - 화면 어둡게 유지하여 센서 계속 작동")
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("센서 연결 중..."))

        onConnectionStateUpdate?.invoke("센서 연결 중...")

        trackingService = SamsungHealthTrackingService(connectionListener, this)
        trackingService.connectService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service onStartCommand")

        when (intent?.action) {
            "ACTION_START_PERIODIC_SPO2" -> startPeriodicSpo2Measurement()
            "ACTION_STOP_PERIODIC_SPO2" -> stopPeriodicSpo2Measurement()
            "ACTION_STOP_SERVICE" -> stopSelf()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service onDestroy")
        isServiceRunning = false
        stopPeriodicSpo2Measurement()
        stopHrTracking()
        try {
            trackingService.disconnectService()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect service", e)
        }

        // WakeLock 해제
        if (wakeLock.isHeld) {
            wakeLock.release()
            Log.d(TAG, "WakeLock released")
        }

        serviceScope.cancel()
    }

    // ========= 연결 리스너 =========
    private val connectionListener = object : ConnectionListener {
        override fun onConnectionSuccess() {
            Log.d(TAG, "Connected to Samsung Health Service")
            onConnectionStateUpdate?.invoke("심박수 센서 초기화 중...")

            try {
                heartRateTracker = trackingService.getHealthTracker(HealthTrackerType.HEART_RATE)
                spo2Tracker = trackingService.getHealthTracker(HealthTrackerType.SPO2)
                startHrTracking()
                updateNotification("심박수 모니터링 중")

                // 첫 데이터 수신 대기 메시지
                serviceScope.launch {
                    delay(3000)
                    if (onConnectionStateUpdate != null && heartRate == 0) {
                        onConnectionStateUpdate?.invoke("첫 심박수 측정 중... (최대 15초 소요)")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get trackers", e)
                onConnectionStateUpdate?.invoke("센서 초기화 실패")
            }
        }

        override fun onConnectionFailed(e: HealthTrackerException) {
            Log.e(TAG, "Connection failed", e)
            updateNotification("센서 연결 실패")
            onConnectionStateUpdate?.invoke("센서 연결 실패: 워치를 손목에 착용하세요")
        }

        override fun onConnectionEnded() {
            Log.d(TAG, "Connection ended")
            onConnectionStateUpdate?.invoke("센서 연결 종료됨")
        }
    }

    private var heartRate = 0

    // ========= HR 처리 =========
    private val heartRateListener = object : HealthTracker.TrackerEventListener {
        override fun onDataReceived(dataPoints: List<DataPoint>) {
            Log.d(TAG, "📊 심박수 데이터 수신됨 - 데이터 개수: ${dataPoints.size}")
            if (dataPoints.isEmpty()) return

            val latest = dataPoints.last()
            val hr = latest.getValue(ValueKey.HeartRateSet.HEART_RATE)
            Log.d(TAG, "💓 측정된 심박수: $hr BPM (현재 시각: ${System.currentTimeMillis()})")
            if (hr > 0) handleHeartRate(hr)
        }

        override fun onError(error: HealthTracker.TrackerError) {
            Log.e(TAG, "❌ HR Sensor error: $error")
        }

        override fun onFlushCompleted() {
            Log.d(TAG, "HR Flush completed")
        }
    }

    private fun startHrTracking() {
        if (heartRateTracker == null || isHrTracking) return
        isHrTracking = true
        heartRateTracker?.setEventListener(heartRateListener)
        Log.d(TAG, "✅ 심박수 추적 시작됨 (Tracking Started)")
    }

    private fun stopHrTracking() {
        if (!isHrTracking) return
        try {
            heartRateTracker?.unsetEventListener()
        } catch (_: Exception) {}
        isHrTracking = false
        Log.d(TAG, "HR tracking stopped")
    }

    private fun handleHeartRate(hr: Int) {
        // 첫 심박수 수신 시 연결 상태 메시지 클리어
        if (heartRate == 0) {
            Log.d(TAG, "🎉 첫 심박수 데이터 수신 완료!")
            onConnectionStateUpdate?.invoke("")
        }
        heartRate = hr

        // 측정된 모든 데이터 전송 (스킵 없음)
        Log.d(TAG, "📤 심박수 전송: $hr BPM")
        sendHeartRate(hr)
        lastSentHr = hr
        lastSentAt = System.currentTimeMillis()

        // UI 업데이트 콜백
        onHeartRateUpdate?.invoke(hr)
    }

    // ========= SpO2 처리 =========
    private val spo2Listener = object : HealthTracker.TrackerEventListener {
        override fun onDataReceived(dataPoints: List<DataPoint>) {
            val dp = dataPoints.lastOrNull() ?: return
            val status = dp.getValue(ValueKey.SpO2Set.STATUS)
            if (status == 2) { // 측정 성공
                val spo2 = dp.getValue(ValueKey.SpO2Set.SPO2)
                sendSpo2Data(spo2)
                onSpo2Update?.invoke(spo2)
                stopSpo2Tracking()

                // HR 센서 재정비
                serviceScope.launch {
                    onStatusUpdate?.invoke("센서 재정비 중...")
                    delay(10000)
                    onStatusUpdate?.invoke("")
                }
            } else if (status < 0) {
                val errorMsg = getErrorMessage(status)
                onStatusUpdate?.invoke(errorMsg)
                stopSpo2Tracking()

                // 재시도 로직
                if (isPeriodicSpo2Active) {
                    serviceScope.launch {
                        delay(5000)
                        if (isPeriodicSpo2Active && !isSpo2Tracking) {
                            triggerSpo2Measurement()
                        }
                    }
                }
            }
        }

        override fun onError(error: HealthTracker.TrackerError) {
            Log.e(TAG, "SpO2 Sensor error: $error")
            onStatusUpdate?.invoke("산소포화도 센서 오류")
            stopSpo2Tracking()
        }

        override fun onFlushCompleted() {}
    }

    private fun startPeriodicSpo2Measurement() {
        if (isPeriodicSpo2Active) return
        isPeriodicSpo2Active = true
        onStatusUpdate?.invoke("5분 간격 측정 시작")

        periodicSpo2Job = serviceScope.launch {
            while (isActive) {
                if (!isSpo2Tracking) triggerSpo2Measurement()
                delay(PERIODIC_SPO2_INTERVAL_MS)
            }
        }
    }

    private fun stopPeriodicSpo2Measurement() {
        if (!isPeriodicSpo2Active) return
        periodicSpo2Job?.cancel()
        isPeriodicSpo2Active = false
        if (isSpo2Tracking) stopSpo2Tracking()
        onStatusUpdate?.invoke("반복 측정 중지")
    }

    private fun triggerSpo2Measurement() {
        if (isSpo2Tracking || spo2Tracker == null) return
        isSpo2Tracking = true
        onStatusUpdate?.invoke("SpO₂ 측정 중...")
        spo2Tracker?.setEventListener(spo2Listener)
    }

    private fun stopSpo2Tracking() {
        if (!isSpo2Tracking) return
        isSpo2Tracking = false
        try {
            spo2Tracker?.unsetEventListener()
        } catch (_: Exception) {}
    }

    private fun getErrorMessage(status: Int): String = when (status) {
        -1, -2, -3, -4, -6 -> "측정 실패: 워치 밀착 확인"
        -5 -> "손목 착용 확인"
        else -> "측정 오류"
    }

    // ========= 데이터 전송 =========
    private fun sendHeartRate(hr: Int) {
        Log.d(TAG, "🔄 심박수 전송 시작: $hr BPM")

        // 메시지 전송 (실시간)
        val payload = hr.toString().toByteArray(StandardCharsets.UTF_8)
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            Log.d(TAG, "📱 연결된 기기 수: ${nodes.size}")
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, HR_MSG_PATH, payload)
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ 메시지 전송 성공 -> ${node.displayName}: $hr BPM")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ 메시지 전송 실패 -> ${node.displayName}", e)
                    }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "❌ 연결된 노드 조회 실패", e)
        }

        // DataItem 전송 (백업)
        val req = PutDataMapRequest.create(HR_DATA_PATH).apply {
            dataMap.putFloat(HR_KEY, hr.toFloat())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        dataClient.putDataItem(req)
            .addOnSuccessListener {
                Log.d(TAG, "✅ DataItem 전송 성공: $hr BPM")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ DataItem 전송 실패", e)
            }
    }

    private fun sendSpo2Data(value: Int) {
        // 메시지 전송 (실시간)
        val payload = value.toString().toByteArray(StandardCharsets.UTF_8)
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, SPO2_MSG_PATH, payload)
                    .addOnSuccessListener { Log.d(TAG, "SpO2 sent: $value") }
                    .addOnFailureListener { e -> Log.e(TAG, "SpO2 send failed", e) }
            }
        }

        // DataItem 전송 (백업)
        val req = PutDataMapRequest.create(SPO2_DATA_PATH).apply {
            dataMap.putFloat(SPO2_KEY, value.toFloat())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        dataClient.putDataItem(req)
    }

    // ========= Notification =========
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Health Tracking",
                NotificationManager.IMPORTANCE_DEFAULT  // LOW -> DEFAULT로 변경
            ).apply {
                description = "백그라운드 센서 모니터링"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC  // 잠금화면에도 표시
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("건강 모니터링")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)  // 우선순위 설정
            .setCategory(NotificationCompat.CATEGORY_SERVICE)  // 서비스 카테고리
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)  // 잠금화면 표시
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)  // 즉시 표시
            .build()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification(text))
    }
}
