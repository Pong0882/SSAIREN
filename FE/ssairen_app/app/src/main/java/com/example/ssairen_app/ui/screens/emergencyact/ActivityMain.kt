// ActivityMain.kt
package com.example.ssairen_app.ui.screens.emergencyact

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.data.api.RetrofitClient
import com.example.ssairen_app.service.VideoRecordingService
import com.example.ssairen_app.service.AudioRecordingService
import com.example.ssairen_app.ui.components.DarkCard
import com.example.ssairen_app.ui.components.MainButton
import com.example.ssairen_app.ui.components.HeartRateChart
import com.example.ssairen_app.ui.navigation.EmergencyNav
import com.example.ssairen_app.ui.wear.WearDataViewModel
import com.example.ssairen_app.utils.SpeechToTextHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ActivityMain(
    onNavigateToActivityLog: () -> Unit = {},
    onNavigateToPatientInfo: () -> Unit = {},
    onNavigateToPatientType: () -> Unit = {},
    onNavigateToPatientEva: () -> Unit = {},
    onNavigateToFirstAid: () -> Unit = {},
    onNavigateToDispatch: () -> Unit = {},
    onNavigateToMedicalGuidance: () -> Unit = {},
    onNavigateToPatientTransport: () -> Unit = {},
    onNavigateToReportDetail: () -> Unit = {},
    activityViewModel: com.example.ssairen_app.viewmodel.ActivityViewModel = viewModel()  // ✅ 추가
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .statusBarsPadding()
    ) {
        // 메인 콘텐츠 영역
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> HomeContent(
                    onNavigateToActivityLog = onNavigateToActivityLog,
                    onNavigateToPatientInfo = onNavigateToPatientInfo,
                    onNavigateToPatientType = onNavigateToPatientType,
                    onNavigateToPatientEva = onNavigateToPatientEva,
                    onNavigateToFirstAid = onNavigateToFirstAid,
                    onNavigateToDispatch = onNavigateToDispatch,
                    onNavigateToMedicalGuidance = onNavigateToMedicalGuidance,
                    onNavigateToPatientTransport = onNavigateToPatientTransport,
                    onNavigateToReportDetail = onNavigateToReportDetail,
                    activityViewModel = activityViewModel  // ✅ 전달
                )
                1 -> Text("구급활동일지 화면", color = Color.White)
                2 -> Text("요약 화면", color = Color.White)
                3 -> Text("메모 화면", color = Color.White)
                4 -> HospitalSearch()
            }
        }

        // 하단 네비게이션
        EmergencyNav(
            selectedTab = selectedTab,
            onTabSelected = {
                selectedTab = it
                if (it == 1) {
                    onNavigateToActivityLog()
                }
            }
        )
    }
}

@Composable
private fun HomeContent(
    onNavigateToActivityLog: () -> Unit = {},
    onNavigateToPatientInfo: () -> Unit = {},
    onNavigateToPatientType: () -> Unit = {},
    onNavigateToPatientEva: () -> Unit = {},
    onNavigateToFirstAid: () -> Unit = {},
    onNavigateToDispatch: () -> Unit = {},
    onNavigateToMedicalGuidance: () -> Unit = {},
    onNavigateToPatientTransport: () -> Unit = {},
    onNavigateToReportDetail: () -> Unit = {},
    activityViewModel: com.example.ssairen_app.viewmodel.ActivityViewModel = viewModel()
) {
    var isAudioRecording by remember { mutableStateOf(false) }
    var isVideoRecording by remember { mutableStateOf(false) }
    var videoService by remember { mutableStateOf<VideoRecordingService?>(null) }
    var isBound by remember { mutableStateOf(false) }

    // ✅ 오디오 서비스 변수 추가
    var audioService by remember { mutableStateOf<AudioRecordingService?>(null) }
    var isAudioBound by remember { mutableStateOf(false) }

    // ✅ STT 관련 상태 추가
    var isSttRecording by remember { mutableStateOf(false) }
    var sttText by remember { mutableStateOf("") }
    var sttHelper by remember { mutableStateOf<SpeechToTextHelper?>(null) }

    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val wearViewModel: WearDataViewModel = remember {
        WearDataViewModel.getInstance(application)
    }

    // 비디오 서비스 연결
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as VideoRecordingService.LocalBinder
                videoService = binder.getService()
                isBound = true
                Log.d("ActivityMain", "VideoRecordingService connected")

                videoService?.setRecordingCallbacks(
                    onStarted = {
                        isVideoRecording = true
                        Log.d("ActivityMain", "Recording started")
                    },
                    onStopped = { file ->
                        isVideoRecording = false
                        Log.d("ActivityMain", "Recording stopped")
                    },
                    onError = { error ->
                        Log.e("ActivityMain", "Recording error: $error")
                    },
                    onUploadComplete = { objectName ->
                        Log.d("ActivityMain", "Upload complete: $objectName")
                    },
                    onProgress = { durationSeconds ->
                        // 진행률 업데이트 (필요시)
                    }
                )

                if (videoService?.isCurrentlyRecording() == true) {
                    isVideoRecording = true
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                videoService = null
                isBound = false
                Log.d("ActivityMain", "VideoRecordingService disconnected")
            }
        }
    }

    // ✅ 오디오 서비스 연결
    val audioServiceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as AudioRecordingService.LocalBinder
                audioService = binder.getService()
                isAudioBound = true
                Log.d("ActivityMain", "AudioRecordingService connected")

                audioService?.setRecordingCallbacks(
                    onStarted = {
                        isAudioRecording = true
                        Log.d("ActivityMain", "Audio recording started")
                    },
                    onStopped = { file ->
                        isAudioRecording = false
                        Log.d("ActivityMain", "Audio recording stopped: ${file?.name}")
                    },
                    onError = { error ->
                        isAudioRecording = false
                        Log.e("ActivityMain", "Audio recording error: $error")
                    },
                    onUploadComplete = { sessionName ->
                        Log.d("ActivityMain", "Audio upload complete: $sessionName")
                    }
                )

                if (audioService?.isCurrentlyRecording() == true) {
                    isAudioRecording = true
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                audioService = null
                isAudioBound = false
                Log.d("ActivityMain", "AudioRecordingService disconnected")
            }
        }
    }

    // 화면 정리 시 서비스 언바인드 및 STT 정리
    DisposableEffect(Unit) {
        onDispose {
            if (isBound) {
                context.unbindService(serviceConnection)
            }
            if (isAudioBound) {
                context.unbindService(audioServiceConnection)
            }
            sttHelper?.destroy()
        }
    }

    // 필요한 권한 목록
    val requiredPermissions = buildList {
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Log.d("ActivityMain", "All permissions granted")
        } else {
            Log.e("ActivityMain", "Permissions denied")
        }
    }

    // 권한 확인 함수
    fun checkPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 비디오 녹화 시작 함수
    fun startVideoRecording() {
        if (!checkPermissions()) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
            return
        }

        if (!isBound) {
            val intent = Intent(context, VideoRecordingService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        val intent = Intent(context, VideoRecordingService::class.java).apply {
            action = VideoRecordingService.ACTION_START_RECORDING
        }
        ContextCompat.startForegroundService(context, intent)
    }

    // 비디오 녹화 중지 함수
    fun stopVideoRecording() {
        videoService?.stopRecording()
    }

    // ✅ 오디오 녹음 시작 함수
    fun startAudioRecording() {
        if (!checkPermissions()) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
            return
        }

        if (!isAudioBound) {
            val intent = Intent(context, AudioRecordingService::class.java)
            context.bindService(intent, audioServiceConnection, Context.BIND_AUTO_CREATE)
        }

        val intent = Intent(context, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_START_RECORDING
        }
        ContextCompat.startForegroundService(context, intent)
    }

    // ✅ 오디오 녹음 중지 함수
    fun stopAudioRecording() {
        audioService?.stopRecording()
    }

    // ✅ STT 녹음 시작 함수
    fun startSttRecording() {
        if (!checkPermissions()) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
            return
        }

        if (sttHelper == null) {
            sttHelper = SpeechToTextHelper(
                context = context,
                onResult = { text ->
                    sttText = text
                    Log.d("ActivityMain", "📝 STT Result: $text")
                },
                onPartialResult = { text ->
                    Log.d("ActivityMain", "📝 STT Partial: $text")
                },
                onError = { error ->
                    Log.e("ActivityMain", "❌ STT Error: $error")
                }
            )
        }

        isSttRecording = true
        sttHelper?.startListening()
        Log.d("ActivityMain", "🎤 STT Recording Started")
    }

    // ✅ STT 녹음 중지 함수 (API 전송 없이 녹음만 중지)
    fun stopSttRecording() {
        sttHelper?.stopListening()
        isSttRecording = false
        Log.d("ActivityMain", "🛑 STT Recording Stopped")

        // 녹음 중지 시 누적 텍스트 초기화
        sttHelper?.clearAccumulatedText()
        sttText = ""
    }

    // ✅ 누적된 텍스트를 API로 전송하는 함수 (녹음은 계속 진행)
    fun sendAccumulatedTextToApi() {
        val currentText = sttHelper?.getAccumulatedText() ?: ""
        val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()

        Log.d("ActivityMain", "📤 Sending accumulated text to API")
        Log.d("ActivityMain", "  - Text: $currentText")
        Log.d("ActivityMain", "  - ReportId: $currentReportId")

        if (currentText.isEmpty()) {
            Log.w("ActivityMain", "⚠️ No text to send")
            return
        }

        if (currentReportId <= 0) {
            Log.e("ActivityMain", "❌ Invalid report ID: $currentReportId")
            return
        }

        // API 호출
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("ActivityMain", "📤 Calling text-to-json API...")
                val response = RetrofitClient.fileApiService.textToJson(
                    text = currentText,
                    emergencyReportId = currentReportId.toLong(),
                    maxNewTokens = 700,
                    temperature = 0.1
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        Log.d("ActivityMain", "✅ API Success: $data")
                        // TODO: 받은 JSON 데이터 처리

                        // 전송 성공 후 누적 텍스트 초기화
                        sttHelper?.clearAccumulatedText()
                        sttText = ""
                        Log.d("ActivityMain", "🗑️ Accumulated text cleared")
                    } else {
                        Log.e("ActivityMain", "❌ API Error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ActivityMain", "❌ API Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    // 에러 처리
                }
            }
        }
    }

    Log.d("ActivityMain", "🎨 HomeContent Composable 렌더링")
    Log.d("ActivityMain", "📱 ViewModel 인스턴스: $wearViewModel")

    val heartRate by wearViewModel.heartRate.collectAsState()
    val spo2 by wearViewModel.spo2.collectAsState()
    val spo2ErrorMessage by wearViewModel.spo2ErrorMessage.collectAsState()
    val hrStatusMessage by wearViewModel.hrStatusMessage.collectAsState()
    val heartRateHistory by wearViewModel.heartRateHistory.collectAsState()

    Log.d("ActivityMain", "📊 현재 UI에 표시되는 값 - HR: $heartRate, SpO2: $spo2, SpO2 에러: '$spo2ErrorMessage', HR 상태: '$hrStatusMessage'")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 상단 타이틀
        Text(
            text = "메인화면",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 34.dp, bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 좌측 영역 (차트 + 통계)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 차트 카드
                DarkCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    HeartRateChart(
                        heartRateHistory = heartRateHistory,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 통계 카드들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 심박수(맥박)
                    StatCard(
                        title = "심박수(맥박)",
                        value = if (hrStatusMessage.isNotEmpty()) hrStatusMessage
                        else if (heartRate > 0) "$heartRate bpm" else "--",
                        modifier = Modifier.weight(1f),
                        valueColor = if (hrStatusMessage.isNotEmpty()) Color(0xFFFF9800) else Color(0xFF00d9ff),
                        isStatusMessage = hrStatusMessage.isNotEmpty()
                    )

                    // 산소포화도(SpO2)
                    StatCard(
                        title = "산소포화도(SpO2)",
                        value = if (spo2ErrorMessage.isNotEmpty()) spo2ErrorMessage
                        else if (spo2 > 0) "$spo2%" else "--",
                        modifier = Modifier.weight(1f),
                        valueColor = if (spo2ErrorMessage.isNotEmpty()) Color(0xFFFF5252) else Color(0xFF00d9ff),
                        isStatusMessage = spo2ErrorMessage.isNotEmpty()
                    )
                }

                // ✅ 바디캠 녹화 + 오디오 녹음 + STT 버튼
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 첫 번째 줄: 바디캠, 오디오, STT 버튼
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 바디캠 녹화 버튼
                        IconButton(
                            onClick = {
                                if (isVideoRecording) {
                                    stopVideoRecording()
                                } else {
                                    startVideoRecording()
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (isVideoRecording) Color(0xFFff3b30) else Color(0xFF2a2a2a),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isVideoRecording) Icons.Filled.Stop else Icons.Filled.PhotoCamera,
                                contentDescription = if (isVideoRecording) "녹화 중지" else "녹화 시작",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // ✅ 오디오 녹음 버튼
                        IconButton(
                            onClick = {
                                if (isAudioRecording) {
                                    stopAudioRecording()
                                } else {
                                    startAudioRecording()
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (isAudioRecording) Color(0xFFff3b30) else Color(0xFF2a2a2a),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isAudioRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                                contentDescription = if (isAudioRecording) "녹음 중지" else "녹음 시작",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // ✅ STT 버튼 (음성인식)
                        IconButton(
                            onClick = {
                                if (isSttRecording) {
                                    stopSttRecording()
                                } else {
                                    startSttRecording()
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (isSttRecording) Color(0xFF4CAF50) else Color(0xFF2a2a2a),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isSttRecording) Icons.Filled.Stop else Icons.Filled.KeyboardVoice,
                                contentDescription = if (isSttRecording) "STT 중지" else "STT 시작",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // 두 번째 줄: STT 전송 버튼 (STT 녹음 중일 때만 표시)
                    if (isSttRecording) {
                        Button(
                            onClick = { sendAccumulatedTextToApi() },
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .height(40.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "텍스트 전송",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "텍스트 전송",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 우측 메뉴 버튼들
            Column(
                modifier = Modifier.width(140.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. 환자정보 버튼
                MainButton(
                    onClick = {
                        activityViewModel.getPatientInfo()  // ✅ GET 추가
                        onNavigateToPatientInfo()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "환자정보",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "환자정보",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 2. 환자평가 버튼
                MainButton(
                    onClick = {
                        activityViewModel.getPatientEva()  // ✅ GET 추가
                        onNavigateToPatientEva()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "환자평가",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "환자평가",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 3. 환자이송 버튼 (API 미구현)
                MainButton(
                    onClick = onNavigateToPatientTransport,  // ✅ API 없음 (TODO)
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "환자이송",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "환자이송",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 4. 구급출동 버튼 (API 미구현)
                MainButton(
                    onClick = onNavigateToDispatch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "구급출동",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "구급출동",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 5. 환자 발생 유형 버튼
                MainButton(
                    onClick = {
                        activityViewModel.getPatientType()  // ✅ GET 추가
                        onNavigateToPatientType()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "환자 발생 유형",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "환자 발생 유형",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 6. 응급처치 버튼
                MainButton(
                    onClick = {
                        activityViewModel.getFirstAid()  // ✅ GET 추가
                        onNavigateToFirstAid()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "응급처치",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "응급처치",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 7. 의료지도 버튼 (API 미구현)
                MainButton(
                    onClick = onNavigateToMedicalGuidance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "의료지도",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "의료지도",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 8. 세부 상황정보 버튼 (API 미구현)
                MainButton(
                    onClick = onNavigateToReportDetail,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "세부 상황정보",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "세부 상황정보",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// 통계 카드 컴포넌트
@Composable
private fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    isStatusMessage: Boolean = false
) {
    DarkCard(
        modifier = modifier.height(100.dp),
        cornerRadius = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color(0xFF999999),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = if (isStatusMessage) 12.sp else 32.sp,
                fontWeight = if (isStatusMessage) FontWeight.Medium else FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = if (isStatusMessage) 2 else 1
            )
        }
    }
}