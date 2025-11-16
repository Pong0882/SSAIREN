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
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import com.example.ssairen_app.service.AudioRecordingServiceNew
import com.example.ssairen_app.ui.components.DarkCard
import com.example.ssairen_app.ui.components.MainButton
import com.example.ssairen_app.ui.components.HeartRateChart
import com.example.ssairen_app.ui.navigation.EmergencyNav
import com.example.ssairen_app.ui.wear.WearDataViewModel
import com.example.ssairen_app.utils.SpeechToTextHelper
import com.example.ssairen_app.utils.SttManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ActivityMain(
    onNavigateToActivityLog: () -> Unit = {},
    onNavigateToReportHome: () -> Unit = {},
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
    var selectedTab by remember { mutableIntStateOf(0) }

    // ✅ 전역 STT 상태 사용 (싱글톤)
    val isSttRecording = SttManager.isSttRecording
    val sttText = SttManager.sttText

    // ✅ 20초 자동 전송은 AppNavigation 레벨에서 처리 (모든 화면에서 동작)

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
                    onNavigateToReportHome = onNavigateToReportHome,
                    onNavigateToActivityLog = onNavigateToActivityLog,
                    onNavigateToPatientInfo = onNavigateToPatientInfo,
                    onNavigateToPatientType = onNavigateToPatientType,
                    onNavigateToPatientEva = onNavigateToPatientEva,
                    onNavigateToFirstAid = onNavigateToFirstAid,
                    onNavigateToDispatch = onNavigateToDispatch,
                    onNavigateToMedicalGuidance = onNavigateToMedicalGuidance,
                    onNavigateToPatientTransport = onNavigateToPatientTransport,
                    onNavigateToReportDetail = onNavigateToReportDetail,
                    activityViewModel = activityViewModel,
                    selectedTab = selectedTab,  // ✅ 추가
                    onTabChange = { selectedTab = it }  // ✅ 추가
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
    onNavigateToReportHome: () -> Unit = {},
    onNavigateToPatientInfo: () -> Unit = {},
    onNavigateToPatientType: () -> Unit = {},
    onNavigateToPatientEva: () -> Unit = {},
    onNavigateToFirstAid: () -> Unit = {},
    onNavigateToDispatch: () -> Unit = {},
    onNavigateToMedicalGuidance: () -> Unit = {},
    onNavigateToPatientTransport: () -> Unit = {},
    onNavigateToReportDetail: () -> Unit = {},
    activityViewModel: com.example.ssairen_app.viewmodel.ActivityViewModel = viewModel(),
    selectedTab: Int = 0,  // ✅ 추가
    onTabChange: (Int) -> Unit = {}  // ✅ 추가
) {
    // ✅ 전역 STT 상태 사용 (싱글톤)
    val isSttRecording = SttManager.isSttRecording
    val sttText = SttManager.sttText
    var isAudioRecording by remember { mutableStateOf(false) }
    var isVideoRecording by remember { mutableStateOf(false) }
    var videoService by remember { mutableStateOf<VideoRecordingService?>(null) }
    var isBound by remember { mutableStateOf(false) }

    // ✅ 오디오 서비스 변수 추가 (새로운 AudioRecord 방식)
    var audioService by remember { mutableStateOf<AudioRecordingServiceNew?>(null) }
    var isAudioBound by remember { mutableStateOf(false) }

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

    // ✅ 오디오 서비스 연결 (새로운 AudioRecord 방식)
    val audioServiceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as AudioRecordingServiceNew.LocalBinder
                audioService = binder.getService()
                isAudioBound = true
                Log.d("ActivityMain", "AudioRecordingServiceNew connected")

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
                Log.d("ActivityMain", "AudioRecordingServiceNew disconnected")
            }
        }
    }

    // 화면 정리 시 서비스 언바인드 (STT는 ActivityMain에서 관리)
    DisposableEffect(Unit) {
        onDispose {
            if (isBound) {
                context.unbindService(serviceConnection)
            }
            if (isAudioBound) {
                context.unbindService(audioServiceConnection)
            }
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

    // ✅ 오디오 녹음 시작 함수 (새로운 AudioRecord 방식)
    fun startAudioRecording() {
        if (!checkPermissions()) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
            return
        }

        if (!isAudioBound) {
            val intent = Intent(context, AudioRecordingServiceNew::class.java)
            context.bindService(intent, audioServiceConnection, Context.BIND_AUTO_CREATE)
        }

        val intent = Intent(context, AudioRecordingServiceNew::class.java).apply {
            action = AudioRecordingServiceNew.ACTION_START_RECORDING
        }
        ContextCompat.startForegroundService(context, intent)
    }

    // ✅ 오디오 녹음 중지 함수
    fun stopAudioRecording() {
        audioService?.stopRecording()
    }

    // ✅ 현재까지 녹음된 오디오를 전송 (녹음은 계속)
    fun sendCurrentAudio() {
        audioService?.sendCurrentRecording()
        Log.d("ActivityMain", "📤 Sending current audio recording")
    }

    // ✅ STT 녹음 시작 함수
    fun startSttRecording() {
        if (!checkPermissions()) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
            return
        }

        // ✅ SttManager 초기화 및 시작
        SttManager.initializeSttHelper(
            context = context,
            onResult = { text ->
                Log.d("ActivityMain", "📝 STT Result: $text")
            },
            onPartialResult = { text ->
                Log.d("ActivityMain", "📝 STT Partial: $text")
            },
            onError = { error ->
                Log.e("ActivityMain", "❌ STT Error: $error")
            }
        )
        SttManager.startRecording()
    }

    // ✅ STT 녹음 중지 함수 + 마지막 텍스트 전송
    fun stopSttRecording() {
        val finalText = SttManager.stopRecording()

        // ✅ 마지막 누적된 텍스트가 있으면 전송
        if (finalText.isNotEmpty()) {
            val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()

            if (currentReportId > 0) {
                Log.d("ActivityMain", "📤 녹음 종료 - 마지막 텍스트 전송")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.fileApiService.textToJson(
                            text = finalText,
                            emergencyReportId = currentReportId.toLong(),
                            maxNewTokens = 700,
                            temperature = 0.1
                        )

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                Log.d("ActivityMain", "✅ 마지막 텍스트 전송 성공")
                                Toast.makeText(context, "녹음 종료 - 전송 완료", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.e("ActivityMain", "❌ API Error: ${response.code()}")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ActivityMain", "❌ API Exception: ${e.message}")
                    }
                }
            }
        }
    }

    // ✅ 누적된 텍스트를 API로 전송하는 함수 (녹음은 계속 진행)
    fun sendAccumulatedTextToApi() {
        // ✅ SttManager에서 누적된 텍스트 가져오기
        val accumulatedText = SttManager.getAccumulatedText()
        val currentText = if (SttManager.sttText.isNotEmpty()) SttManager.sttText else accumulatedText

        val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()

        Log.d("ActivityMain", "📤 Sending accumulated text to API")
        Log.d("ActivityMain", "  - Accumulated Text: $accumulatedText")
        Log.d("ActivityMain", "  - Display Text (sttText): ${SttManager.sttText}")
        Log.d("ActivityMain", "  - Sending Text: $currentText")
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
                        Log.d("ActivityMain", "✅ textToJson API 성공!")
                        Log.d("ActivityMain", "📦 전체 응답: ${response.body()}")
                        Log.d("ActivityMain", "📄 응답 데이터: $data")
                        Log.d("ActivityMain", "🔍 응답 코드: ${response.code()}")

                        // 응답 데이터 구조 확인
                        response.body()?.let { apiResponse ->
                            Log.d("ActivityMain", "  - success: ${apiResponse.success}")
                            Log.d("ActivityMain", "  - message: ${apiResponse.message}")
                            apiResponse.data?.let { sttData ->
                                Log.d("ActivityMain", "  - reportSectionType: ${sttData.reportSectionType}")
                            }
                        }

                        // ✅ 전송 후에도 텍스트는 계속 누적됨 (초기화하지 않음)
                        Log.d("ActivityMain", "📝 Text sent successfully, continuing to accumulate")

                        // ✅ 전송 완료 Toast 알림
                        Toast.makeText(context, "전송 완료", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("ActivityMain", "❌ textToJson API 실패!")
                        Log.e("ActivityMain", "  - 응답 코드: ${response.code()}")
                        Log.e("ActivityMain", "  - 에러 바디: ${response.errorBody()?.string()}")
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

    // ✅ STT 자동 전송은 ActivityMain 레벨에서 처리됨

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 34.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "메인화면",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onNavigateToReportHome ) {  // ✅ 수정
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "보고서 홈",
                    tint = Color.White
                )
            }
        }

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

                // ✅ 바디캠 녹화 + STT 버튼
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                }
            }

            // 우측 메뉴 버튼들
            // 우측 메뉴 버튼들
            Column(
                modifier = Modifier
                    .width(140.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. 환자정보 버튼
                MainButton(
                    onClick = {
                        val reportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                        Log.d("ActivityMain", "🔘 환자정보 버튼 클릭 - reportId: $reportId")
                        if (reportId > 0) {
                            activityViewModel.getPatientInfo(reportId)
                            Log.d("ActivityMain", "📞 getPatientInfo 호출 완료")
                        } else {
                            Log.e("ActivityMain", "❌ reportId가 0입니다!")
                            Toast.makeText(context, "일지를 먼저 생성해주세요", Toast.LENGTH_SHORT).show()
                        }
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
                        val reportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                        Log.d("ActivityMain", "🔘 환자평가 버튼 클릭 - reportId: $reportId")
                        if (reportId > 0) {
                            activityViewModel.getPatientEva(reportId)
                        } else {
                            Toast.makeText(context, "일지를 먼저 생성해주세요", Toast.LENGTH_SHORT).show()
                        }
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

                // 3. 환자이송 버튼
                MainButton(
                    onClick = {
                        val reportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                        Log.d("ActivityMain", "🔘 환자이송 버튼 클릭 - reportId: $reportId")
                        if (reportId > 0) {
                            activityViewModel.getTransport(reportId)
                        } else {
                            Toast.makeText(context, "일지를 먼저 생성해주세요", Toast.LENGTH_SHORT).show()
                        }
                        onNavigateToPatientTransport()
                    },
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

                // 4. 구급출동 버튼
                MainButton(
                    onClick = {
                        val reportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                        Log.d("ActivityMain", "🔘 구급출동 버튼 클릭 - reportId: $reportId")
                        if (reportId > 0) {
                            activityViewModel.getDispatch(reportId)
                        } else {
                            Toast.makeText(context, "일지를 먼저 생성해주세요", Toast.LENGTH_SHORT).show()
                        }
                        onNavigateToDispatch()
                    },
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
                        val reportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                        Log.d("ActivityMain", "🔘 환자 발생 유형 버튼 클릭 - reportId: $reportId")
                        if (reportId > 0) {
                            activityViewModel.getPatientType(reportId)
                        } else {
                            Toast.makeText(context, "일지를 먼저 생성해주세요", Toast.LENGTH_SHORT).show()
                        }
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
                        val reportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                        Log.d("ActivityMain", "🔘 응급처치 버튼 클릭 - reportId: $reportId")
                        if (reportId > 0) {
                            activityViewModel.getFirstAid(reportId)
                        } else {
                            Toast.makeText(context, "일지를 먼저 생성해주세요", Toast.LENGTH_SHORT).show()
                        }
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

                // 7. 의료지도 버튼
                MainButton(
                    onClick = {
                        val reportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                        Log.d("ActivityMain", "🔘 의료지도 버튼 클릭 - reportId: $reportId")
                        if (reportId > 0) {
                            activityViewModel.getMedicalGuidance(reportId)
                        } else {
                            Toast.makeText(context, "일지를 먼저 생성해주세요", Toast.LENGTH_SHORT).show()
                        }
                        onNavigateToMedicalGuidance()
                    },
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

                // 8. 세부 상황정보 버튼
                MainButton(
                    onClick = {
                        val reportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                        Log.d("ActivityMain", "🔘 세부 상황정보 버튼 클릭 - reportId: $reportId")
                        if (reportId > 0) {
                            activityViewModel.getDetailReport(reportId)
                        } else {
                            Toast.makeText(context, "일지를 먼저 생성해주세요", Toast.LENGTH_SHORT).show()
                        }
                        onNavigateToReportDetail()
                    },
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