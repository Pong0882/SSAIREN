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

private const val TAG = "MainActivity"
private const val ERROR_MESSAGE_TIMEOUT_MS = 5_000L // 오류 메시지 5초 자동 제거

class MainActivity : ComponentActivity() {

    // UI 상태
    private var heartRate by mutableStateOf(0)
    private var spo2 by mutableStateOf(0)
    private var currentMessage by mutableStateOf<PriorityMessage?>(null)  // 우선순위 기반 메시지
    private var isPeriodicSpo2Active by mutableStateOf(false)

    // 메시지 타임아웃 관리
    private var messageTimeoutJob: kotlinx.coroutines.Job? = null

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
                    startHealthTrackingService()
                },
                onTogglePeriodicSpo2Click = { togglePeriodicSpo2Measurement() },
                heartRate = heartRate,
                spo2 = spo2,
                isPeriodicActive = isPeriodicSpo2Active,
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

}

// ======================= UI =======================
@Composable
private fun HealthMeasureScreen(
    onPermissionGranted: () -> Unit,
    onTogglePeriodicSpo2Click: () -> Unit,
    heartRate: Int,
    spo2: Int,
    isPeriodicActive: Boolean,
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
