package com.example.ssairen.wear

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.gms.wearable.*
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

private const val TAG = "MainActivity"

// Data Layer 경로 정의
private const val HR_MSG_PATH = "/hr_msg"
private const val HR_DATA_PATH = "/heart_rate"
private const val HR_KEY = "heart_rate_value"
private const val SPO2_MSG_PATH = "/spo2_msg"
private const val SPO2_DATA_PATH = "/spo2"
private const val SPO2_KEY = "spo2_value"

// SpO2 측정 관련
private const val SPO2_TIMEOUT_MS = 30_000L
private const val PERIODIC_SPO2_INTERVAL_MS = 60_000L   // 1분
private const val SPO2_RETRY_DELAY_MS = 5_000L

// HR 전송 간격/최소 변화량
private const val MIN_DELTA = 1
private const val MIN_INTERVAL_MS = 1_000L

class MainActivity : ComponentActivity() {

    // UI 상태
    private var heartRate by mutableStateOf(0)
    private var spo2 by mutableStateOf(0)
    private var isSpo2Measuring by mutableStateOf(false)
    private var statusMessage by mutableStateOf("")
    private var hasHeartRateCapability by mutableStateOf(false)
    private var hasSpo2Capability by mutableStateOf(false)
    private var isPeriodicSpo2Active by mutableStateOf(false)
    private var isHrReactivating by mutableStateOf(false)

    // Data Layer
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }

    // 삼성 Health Tracking
    private lateinit var trackingService: HealthTrackingService
    private var heartRateTracker: HealthTracker? = null
    private var spo2Tracker: HealthTracker? = null
    private var isHrTracking = false
    private var isSpo2Tracking = false
    private var isConnecting = false

    // 반복 측정 Job
    private var periodicSpo2Job: Job? = null

    // HR 전송 쓰로틀
    private var lastSentHr: Int? = null
    private var lastSentAt: Long = 0L

    // ========= 생명주기 =========
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        trackingService = HealthTrackingService(connectionListener, this)

        setContent {
            HealthMeasureScreen(
                onPermissionGranted = {
                    if (!isConnecting) {
                        isConnecting = true
                        trackingService.connectService()
                    }
                },
                onTogglePeriodicSpo2Click = { togglePeriodicSpo2Measurement() },
                heartRate = heartRate,
                spo2 = spo2,
                isMeasuring = isSpo2Measuring,
                isPeriodicActive = isPeriodicSpo2Active,
                isHrReactivating = isHrReactivating,
                hasSensorCapability = hasHeartRateCapability || hasSpo2Capability,
                statusMessage = statusMessage
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPeriodicSpo2Measurement()
        stopHrTracking()
        try { trackingService.disconnectService() } catch (e: Exception) {
            Log.e(TAG, "Failed to disconnect service", e)
        }
    }

    // ========= 연결 리스너 =========
    private val connectionListener = object : ConnectionListener {
        override fun onConnectionSuccess() {
            isConnecting = false
            try {
                heartRateTracker = trackingService.getHealthTracker(HealthTrackerType.HEART_RATE)
                hasHeartRateCapability = heartRateTracker != null
                spo2Tracker = trackingService.getHealthTracker(HealthTrackerType.SPO2)
                hasSpo2Capability = spo2Tracker != null
                startHrTracking()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get trackers", e)
            }
        }
        override fun onConnectionFailed(e: HealthTrackerException) { isConnecting = false }
        override fun onConnectionEnded() { isConnecting = false }
    }

    // ========= HR 처리 =========
    private val heartRateListener = object : HealthTracker.TrackerEventListener {
        override fun onDataReceived(dataPoints: List<DataPoint>) {
            if (dataPoints.isEmpty()) return

            // 가장 최신 값 선택
            val latest = dataPoints.last()
            val hr = latest.getValue(ValueKey.HeartRateSet.HEART_RATE)
            if (hr > 0) handleHeartRate(hr)
        }
        override fun onError(error: HealthTracker.TrackerError) {
            Log.e(TAG, "HR Sensor onError: $error")
        }
        override fun onFlushCompleted() {}
    }

    private fun startHrTracking() {
        if (heartRateTracker == null) return
        isHrTracking = true
        heartRateTracker?.setEventListener(heartRateListener)
    }

    private fun stopHrTracking() {
        if (!isHrTracking) return
        try { heartRateTracker?.unsetEventListener() } catch (_: Exception) { }
        isHrTracking = false
    }

    private fun handleHeartRate(hr: Int) {
        val now = System.currentTimeMillis()
        val shouldSend =
            (lastSentHr == null) ||
                    (kotlin.math.abs(hr - lastSentHr!!) >= MIN_DELTA) ||
                    (now - lastSentAt >= MIN_INTERVAL_MS)

        if (shouldSend) {
            sendHeartRate(hr)
            lastSentHr = hr
            lastSentAt = now
        }

        heartRate = hr
        // HR 재정비 완료 시 플래그 해제
        if (isHrReactivating) isHrReactivating = false
    }

    // ========= SpO2 처리 =========
    private val spo2Listener = object : HealthTracker.TrackerEventListener {
        override fun onDataReceived(dataPoints: List<DataPoint>) {
            val dp = dataPoints.lastOrNull() ?: return
            val status = dp.getValue(ValueKey.SpO2Set.STATUS)
            if (status == 2) { // 성공
                spo2 = dp.getValue(ValueKey.SpO2Set.SPO2)
                sendSpo2Data(spo2)
                stopSpo2Tracking()
            } else if (status < 0) {
                stopSpo2Tracking(getErrorMessage(status) + " 잠시 후 재시도합니다.", shouldRetry = true)
            }
        }
        override fun onError(error: HealthTracker.TrackerError) {
            stopSpo2Tracking("산소포화도 센서 오류. 잠시 후 재시도합니다.", shouldRetry = true)
        }
        override fun onFlushCompleted() {}
    }

    private fun togglePeriodicSpo2Measurement() {
        // 1분 간격 자동 반복 측정
        if (isPeriodicSpo2Active) stopPeriodicSpo2Measurement() else startPeriodicSpo2Measurement()
    }

    private fun startPeriodicSpo2Measurement() {
        if (isPeriodicSpo2Active) return
        isPeriodicSpo2Active = true
        statusMessage = "산소포화도 1분 간격 측정 시작"

        periodicSpo2Job = lifecycleScope.launch {
            while (isActive) {
                if (!isSpo2Measuring) triggerSpo2Measurement()
                delay(PERIODIC_SPO2_INTERVAL_MS)
            }
        }
    }

    private fun stopPeriodicSpo2Measurement() {
        if (!isPeriodicSpo2Active) return
        periodicSpo2Job?.cancel()
        isPeriodicSpo2Active = false
        if (isSpo2Tracking) stopSpo2Tracking("반복 측정이 중지되었습니다.")
        else statusMessage = "반복 측정이 중지되었습니다."
    }

    private fun triggerSpo2Measurement() {
        if (isSpo2Tracking) return
        isSpo2Tracking = true
        isSpo2Measuring = true
        spo2 = 0
        statusMessage = "SpO₂ 측정 중…"
        spo2Tracker?.setEventListener(spo2Listener)

//        lifecycleScope.launch {
//            delay(SPO2_TIMEOUT_MS)
//            if (isSpo2Tracking) {
//                stopSpo2Tracking("측정 시간이 초과되었습니다. 잠시 후 재시도합니다.", shouldRetry = true)
//            }
//        }
    }

    private fun stopSpo2Tracking(message: String? = null, shouldRetry: Boolean = false) {
        if (!isSpo2Tracking) return
        isSpo2Tracking = false
        isSpo2Measuring = false
        try { spo2Tracker?.unsetEventListener() } catch (_: Exception) { }

        if (!message.isNullOrBlank()) {
            // 오류 메시지가 있으면 표시
            statusMessage = message
            if (shouldRetry && isPeriodicSpo2Active) {
                lifecycleScope.launch {
                    delay(SPO2_RETRY_DELAY_MS)
                    if (isPeriodicSpo2Active && !isSpo2Measuring) {
                        statusMessage = "측정 오류. 재시도합니다."
                        triggerSpo2Measurement()
                    }
                }
            }
            // 오류 발생 시 HR 재정비 안 함 (오류 메시지 유지)
        } else {
            // 정상 완료된 경우에만 HR 센서 재정비
            statusMessage = ""
            lifecycleScope.launch {
                isHrReactivating = true
                statusMessage = "심박수 센서 재정비 중..."
                // 약 10초 대기 (SDK가 자동으로 센서 복구할 시간)
                delay(10000)
                isHrReactivating = false
                statusMessage = ""
            }
        }
    }

    private fun getErrorMessage(status: Int): String = when (status) {
        -1, -2, -3, -4, -6 -> "측정 오류. 손목에 밀착 착용하고 움직임을 줄여주세요."
        -5 -> "워치를 손목에 착용하고 다시 시도해 주세요."
        else -> "알 수 없는 오류(코드: $status)"
    }

    // ========= 데이터 전송 =========

    /** 실시간 심박수 전송: 메시지(실시간) + DataItem(백업/초기값) */
    private fun sendHeartRate(hr: Int) {
        // 1) 실시간 메시지 (String)
        val payload = hr.toString().toByteArray(StandardCharsets.UTF_8)
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, HR_MSG_PATH, payload)
                    .addOnSuccessListener { Log.d(TAG, "HR msg -> ${node.displayName}") }
                    .addOnFailureListener { e -> Log.e(TAG, "HR msg FAILED -> ${node.displayName}", e) }
            }
        }

        // 2) DataItem (Float)
        val req = PutDataMapRequest.create(HR_DATA_PATH).apply {
            dataMap.putFloat(HR_KEY, hr.toFloat())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        dataClient.putDataItem(req)
    }

    /** 산소포화도 측정 완료 시 전송 */
    private fun sendSpo2Data(value: Int) {
        // 1) 실시간 메시지 (String)
        val payload = value.toString().toByteArray(StandardCharsets.UTF_8)
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, SPO2_MSG_PATH, payload)
                    .addOnSuccessListener { Log.d(TAG, "SpO2 msg -> ${node.displayName}") }
                    .addOnFailureListener { e -> Log.e(TAG, "SpO2 msg FAILED -> ${node.displayName}", e) }
            }
        }

        // 2) DataItem (Float)
        val req = PutDataMapRequest.create(SPO2_DATA_PATH).apply {
            dataMap.putFloat(SPO2_KEY, value.toFloat())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        dataClient.putDataItem(req)
    }
}

// ======================= UI (동일) =======================
@Composable
private fun HealthMeasureScreen(
    onPermissionGranted: () -> Unit,
    onTogglePeriodicSpo2Click: () -> Unit,
    heartRate: Int,
    spo2: Int,
    isMeasuring: Boolean,
    isPeriodicActive: Boolean,
    isHrReactivating: Boolean,
    hasSensorCapability: Boolean,
    statusMessage: String
) {
    var hasPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
            if (granted) onPermissionGranted()
        }
    )

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED
        hasPermission = granted
        if (granted) onPermissionGranted() else permissionLauncher.launch(Manifest.permission.BODY_SENSORS)
    }

    // 📱 전체 화면 레이아웃 (중앙 정렬된 세로 배치)
    Column(
        modifier = Modifier
            .fillMaxSize()              // 화면 전체 크기
            .background(androidx.compose.ui.graphics.Color(0xFF0A1929))  // 파란 배경
            .padding(16.dp),            // 화면 가장자리 여백
        verticalArrangement = Arrangement.Center,      // 세로 중앙 정렬
        horizontalAlignment = Alignment.CenterHorizontally  // 가로 중앙 정렬
    ) {
        Spacer(Modifier.height(8.dp))

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 🎯 화면 중간: 센서 데이터 표시 영역
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        when {
            // 🚫 케이스 1: 센서 권한이 없을 때
            !hasPermission -> {
                Text("센서 권한을 허용해 주세요.", textAlign = TextAlign.Center)
            }

            // ✅ 케이스 2: 센서가 정상적으로 사용 가능할 때 (메인 화면)
            hasSensorCapability -> {
                // ❤️ 심박수(Heart Rate) 표시 영역
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isHrReactivating) {
                        // HR 센서 재활성화 중: "BPM: " + 작은 로딩 스피너
                        Text("BPM: ", style = MaterialTheme.typography.title1)
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        // HR 정상 측정 중: "72 BPM" 형식으로 표시
                        Text(
                            text = if (heartRate > 0) "$heartRate BPM" else "BPM: --",
                            style = MaterialTheme.typography.title1
                        )
                    }
                }

                // 💧 산소포화도(SpO2) 표시 영역
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isMeasuring) {
                        // 측정 중일 때: "SpO₂: " + 스피너
                        Text("SpO₂: ", style = MaterialTheme.typography.title1)
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        // 측정 안 할 때: "SpO₂: 98%" 또는 "SpO₂: --" 형식으로 표시
                        Text(
                            text = if (spo2 > 0) "SpO₂: ${spo2}%" else "SpO₂: --",
                            style = MaterialTheme.typography.title1
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 🔘 조용한 시작/중지 버튼 (하단에 작게)
                Button(
                    onClick = onTogglePeriodicSpo2Click,
                    enabled = hasPermission,
                    modifier = Modifier
                        .height(32.dp)  // 버튼 높이 작게
                        .width(80.dp),  // 버튼 너비 작게
                    colors = androidx.wear.compose.material.ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.3f)  // 반투명 배경
                    )
                ) {
                    Text(
                        text = if (isPeriodicActive) "중지" else "시작",
                        style = MaterialTheme.typography.body2
                    )
                }

                // 📢 상태 메시지 영역 (SpO2 측정 관련 메시지 표시)
                if (statusMessage.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = statusMessage,
                        color = if (statusMessage.contains("오류") || statusMessage.contains("착용") || statusMessage.contains("움직임"))
                            MaterialTheme.colors.error
                        else
                            MaterialTheme.colors.onSurface,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body2
                    )
                }
            }

            // ⏳ 케이스 3: 센서 초기화 중일 때
            else -> {
                Text("센서를 초기화하는 중입니다…", textAlign = TextAlign.Center)
            }
        }
    }
}
