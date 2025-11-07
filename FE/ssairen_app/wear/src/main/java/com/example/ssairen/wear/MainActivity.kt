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

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {

    // UI 상태
    private var heartRate by mutableStateOf(0)
    private var spo2 by mutableStateOf(0)
    private var statusMessage by mutableStateOf("")
    private var connectionMessage by mutableStateOf("")
    private var isPeriodicSpo2Active by mutableStateOf(false)

    // ========= 생명주기 =========
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 화면 켜짐 유지 (센서 작동 유지를 위해)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Log.d(TAG, "⭐ 화면 켜짐 유지 설정 완료 (FLAG_KEEP_SCREEN_ON)")

        // Foreground Service 시작
        startHealthTrackingService()

        // Service에서 UI 업데이트 콜백 설정
        HealthTrackingForegroundService.onHeartRateUpdate = { hr ->
            heartRate = hr
        }
        HealthTrackingForegroundService.onSpo2Update = { value ->
            spo2 = value
        }
        HealthTrackingForegroundService.onStatusUpdate = { msg ->
            statusMessage = msg
        }
        HealthTrackingForegroundService.onConnectionStateUpdate = { msg ->
            connectionMessage = msg
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
                statusMessage = statusMessage,
                connectionMessage = connectionMessage
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
    }

    private fun startHealthTrackingService() {
        if (HealthTrackingForegroundService.isServiceRunning) return

        val intent = Intent(this, HealthTrackingForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
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
    statusMessage: String,
    connectionMessage: String
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

                // 📢 연결 상태 메시지 (센서 초기화)
                if (connectionMessage.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = connectionMessage,
                        color = if (connectionMessage.contains("실패") || connectionMessage.contains("오류"))
                            MaterialTheme.colors.error
                        else
                            androidx.compose.ui.graphics.Color(0xFF00d9ff),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.caption1
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
        }
    }
}
