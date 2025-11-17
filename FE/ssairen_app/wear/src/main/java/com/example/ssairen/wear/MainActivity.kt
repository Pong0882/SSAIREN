package com.example.ssairen.wear

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
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
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.google.android.gms.wearable.*
import java.nio.charset.StandardCharsets
import kotlin.random.Random

private const val TAG = "MainActivity"
private const val ERROR_MESSAGE_TIMEOUT_MS = 5_000L // 오류 메시지 5초 자동 제거

// 더미 데이터 전송용 경로
private const val HR_MSG_PATH = "/hr_msg"
private const val HR_DATA_PATH = "/heart_rate"
private const val SPO2_MSG_PATH = "/spo2_msg"
private const val SPO2_DATA_PATH = "/spo2"

class MainActivity : ComponentActivity() {

    // UI 상태
    private var heartRate by mutableStateOf(0)
    private var spo2 by mutableStateOf(0)
    private var currentMessage by mutableStateOf<PriorityMessage?>(null)  // 우선순위 기반 메시지
    private var isPeriodicSpo2Active by mutableStateOf(false)

    // 메시지 타임아웃 관리
    private var messageTimeoutJob: kotlinx.coroutines.Job? = null

    // 🧪 테스트 모드 관련
    private var isDummyModeActive by mutableStateOf(false)
    private var dummyDataJob: kotlinx.coroutines.Job? = null
    private val dataClient by lazy { Wearable.getDataClient(this) }
    private val messageClient by lazy { Wearable.getMessageClient(this) }
    private val nodeClient by lazy { Wearable.getNodeClient(this) }

    // ========= 생명주기 =========
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 화면 켜짐 유지 (센서 작동 유지를 위해)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Log.d(TAG, "⭐ 화면 켜짐 유지 설정 완료 (FLAG_KEEP_SCREEN_ON)")

        // Service에서 UI 업데이트 콜백 설정
        HealthTrackingForegroundService.onHeartRateUpdate = { hr ->
            heartRate = hr
        }
        HealthTrackingForegroundService.onSpo2Update = { value ->
            spo2 = value
        }
        HealthTrackingForegroundService.onStatusUpdate = { msg ->
            updateMessage(msg)
        }
        HealthTrackingForegroundService.onConnectionStateUpdate = { msg ->
            updateMessage(msg)
        }

        setContent {
            HealthMeasureScreen(
                onPermissionGranted = {
                    // 🧪 테스트 모드: 실제 센서는 시작하지 않음 (더미 데이터만 사용)
                    // startHealthTrackingService()
                },
                onTogglePeriodicSpo2Click = { togglePeriodicSpo2Measurement() },
                onToggleDummyModeClick = { toggleDummyDataMode() },
                heartRate = heartRate,
                spo2 = spo2,
                isPeriodicActive = isPeriodicSpo2Active,
                isDummyActive = isDummyModeActive,
                currentMessage = currentMessage?.content ?: ""
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 서비스는 백그라운드에서 계속 실행되므로 여기서 중지하지 않음
        HealthTrackingForegroundService.onHeartRateUpdate = null
        HealthTrackingForegroundService.onSpo2Update = null
        HealthTrackingForegroundService.onStatusUpdate = null
        HealthTrackingForegroundService.onConnectionStateUpdate = null
        messageTimeoutJob?.cancel()

        // 🧪 더미 데이터 전송 중지
        stopDummyDataTransmission()
    }

    /**
     * 우선순위 기반 메시지 업데이트 로직
     */
    private fun updateMessage(message: String) {
        val newMessage = PriorityMessage(message)

        // 우선순위 비교 후 업데이트 결정
        if (newMessage.hasHigherPriorityThan(currentMessage)) {
            val oldMessage = currentMessage?.content ?: "없음"
            Log.d(TAG, "📢 메시지 업데이트: '$oldMessage' → '$message' (우선순위: ${newMessage.priority.level})")

            // 메시지 업데이트
            currentMessage = if (message.isEmpty()) null else newMessage

            // 기존 타임아웃 취소
            messageTimeoutJob?.cancel()

            // 오류 메시지는 5초 후 자동 제거
            if (newMessage.isError()) {
                messageTimeoutJob = lifecycleScope.launch {
                    delay(ERROR_MESSAGE_TIMEOUT_MS)
                    // 같은 메시지가 여전히 표시 중이면 제거
                    if (currentMessage?.content == message) {
                        Log.d(TAG, "⏰ 오류 메시지 타임아웃 (5초): '$message'")
                        currentMessage = null
                    }
                }
            }
        } else {
            Log.d(TAG, "🚫 메시지 무시: '$message' (현재: '${currentMessage?.content}', 우선순위 낮음)")
        }
    }

    private fun startHealthTrackingService() {
        if (HealthTrackingForegroundService.isServiceRunning) {
            Log.d(TAG, "서비스가 이미 실행 중입니다")
            return
        }

        // 권한 확인
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BODY_SENSORS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.w(TAG, "⚠️ BODY_SENSORS 권한이 없어 서비스를 시작할 수 없습니다")
            return
        }

        Log.d(TAG, "✅ 권한 확인 완료, Foreground Service 시작")
        val intent = Intent(this, HealthTrackingForegroundService::class.java)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Foreground Service 시작 실패", e)
        }
    }

    private fun togglePeriodicSpo2Measurement() {
        isPeriodicSpo2Active = !isPeriodicSpo2Active

        val intent = Intent(this, HealthTrackingForegroundService::class.java).apply {
            action = if (isPeriodicSpo2Active) {
                "ACTION_START_PERIODIC_SPO2"
            } else {
                "ACTION_STOP_PERIODIC_SPO2"
            }
        }
        startService(intent)
    }

    // 🧪 더미 데이터 전송 모드 토글
    private fun toggleDummyDataMode() {
        isDummyModeActive = !isDummyModeActive

        if (isDummyModeActive) {
            startDummyDataTransmission()
        } else {
            stopDummyDataTransmission()
        }
    }

    // 🧪 더미 데이터 전송 시작
    private fun startDummyDataTransmission() {
        Log.d(TAG, "🧪 더미 데이터 전송 시작!")

        dummyDataJob = lifecycleScope.launch {
            var counter = 0
            while (isDummyModeActive) {
                counter++

                // 심박수: 60~100 사이 랜덤값
                val dummyHr = Random.nextInt(60, 101)
                heartRate = dummyHr
                sendDummyHeartRate(dummyHr)

                // 10초마다 SpO2도 전송 (95~99%)
                if (counter % 10 == 0) {
                    val dummySpo2 = Random.nextInt(95, 100)
                    spo2 = dummySpo2
                    sendDummySpO2(dummySpo2)
                }

                delay(1000) // 1초마다 전송
            }
        }
    }

    // 🧪 더미 데이터 전송 중지
    private fun stopDummyDataTransmission() {
        Log.d(TAG, "🧪 더미 데이터 전송 중지!")
        dummyDataJob?.cancel()
        dummyDataJob = null
        currentMessage = null
    }

    // 🧪 더미 심박수 전송
    private fun sendDummyHeartRate(hr: Int) {
        Log.d(TAG, "🧪 더미 심박수 전송: $hr BPM")

        // 메시지 전송
        val payload = hr.toString().toByteArray(StandardCharsets.UTF_8)
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            Log.d(TAG, "📱 연결된 기기 수: ${nodes.size}")
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, HR_MSG_PATH, payload)
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ 더미 심박수 전송 성공 -> ${node.displayName}: $hr BPM")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ 더미 심박수 전송 실패 -> ${node.displayName}", e)
                    }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "❌ 연결된 노드 조회 실패", e)
        }

        // DataItem 전송 (백업)
        val req = PutDataMapRequest.create(HR_DATA_PATH).apply {
            dataMap.putFloat("heart_rate_value", hr.toFloat())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        dataClient.putDataItem(req)
    }

    // 🧪 더미 SpO2 전송
    private fun sendDummySpO2(spo2: Int) {
        Log.d(TAG, "🧪 더미 SpO2 전송: $spo2%")

        // 메시지 전송
        val payload = spo2.toString().toByteArray(StandardCharsets.UTF_8)
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, SPO2_MSG_PATH, payload)
                    .addOnSuccessListener {
                        Log.d(TAG, "✅ 더미 SpO2 전송 성공 -> ${node.displayName}: $spo2%")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "❌ 더미 SpO2 전송 실패", e)
                    }
            }
        }

        // DataItem 전송 (백업)
        val req = PutDataMapRequest.create(SPO2_DATA_PATH).apply {
            dataMap.putFloat("spo2_value", spo2.toFloat())
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest().setUrgent()
        dataClient.putDataItem(req)
    }

}

// ======================= UI =======================
@Composable
private fun HealthMeasureScreen(
    onPermissionGranted: () -> Unit,
    onTogglePeriodicSpo2Click: () -> Unit,
    onToggleDummyModeClick: () -> Unit,
    heartRate: Int,
    spo2: Int,
    isPeriodicActive: Boolean,
    isDummyActive: Boolean,
    currentMessage: String
) {
    var hasPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // 여러 권한을 한 번에 요청
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            // 모든 권한이 승인되었는지 확인
            val allGranted = permissions.values.all { it }
            hasPermission = allGranted
            if (allGranted) onPermissionGranted()
        }
    )

    LaunchedEffect(Unit) {
        // 필수 권한 목록
        val requiredPermissions = listOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
        )

        // 모든 권한이 승인되었는지 확인
        val allGranted = requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }

        hasPermission = allGranted
        if (allGranted) {
            onPermissionGranted()
        } else {
            permissionsLauncher.launch(requiredPermissions.toTypedArray())
        }
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

            // ✅ 케이스 2: 센서가 사용 가능할 때 (메인 화면)
            else -> {
                // ❤️ 심박수(Heart Rate) 표시 영역
                Text(
                    text = if (heartRate > 0) "$heartRate BPM" else "BPM: --",
                    style = MaterialTheme.typography.title1
                )

                // 💧 산소포화도(SpO2) 표시 영역
                Text(
                    text = if (spo2 > 0) "SpO₂: ${spo2}%" else "SpO₂: --",
                    style = MaterialTheme.typography.title1
                )

                Spacer(Modifier.height(8.dp))

                // 🔘 SpO2 측정 버튼 (주석처리 - 테스트 모드만 사용)
//                Button(
//                    onClick = onTogglePeriodicSpo2Click,
//                    enabled = hasPermission && !isDummyActive,
//                    modifier = Modifier
//                        .height(32.dp)
//                        .width(80.dp),
//                    colors = androidx.wear.compose.material.ButtonDefaults.buttonColors(
//                        backgroundColor = MaterialTheme.colors.surface.copy(alpha = 0.3f)
//                    )
//                ) {
//                    Text(
//                        text = if (isPeriodicActive) "중지" else "시작",
//                        style = MaterialTheme.typography.body2
//                    )
//                }
//
//                Spacer(Modifier.height(4.dp))

                // 🧪 테스트 모드 버튼
                Button(
                    onClick = onToggleDummyModeClick,
                    modifier = Modifier
                        .height(32.dp)
                        .width(80.dp),
                    colors = androidx.wear.compose.material.ButtonDefaults.buttonColors(
                        backgroundColor = if (isDummyActive)
                            androidx.compose.ui.graphics.Color(0xFFFF9800)  // 주황색 (활성)
                        else
                            MaterialTheme.colors.surface.copy(alpha = 0.3f)  // 반투명 (비활성)
                    )
                ) {
                    Text(
                        text = if (isDummyActive) "중지" else "시작",
                        style = MaterialTheme.typography.body2
                    )
                }

                // 📢 메시지 표시 영역 (한 번에 하나씩)
                if (currentMessage.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = currentMessage,
                        color = if (currentMessage.contains("실패") || currentMessage.contains("오류") ||
                                   currentMessage.contains("착용") || currentMessage.contains("움직임"))
                            MaterialTheme.colors.error
                        else if (currentMessage.contains("연결") || currentMessage.contains("초기화"))
                            androidx.compose.ui.graphics.Color(0xFF00d9ff)
                        else
                            MaterialTheme.colors.onSurface,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}
