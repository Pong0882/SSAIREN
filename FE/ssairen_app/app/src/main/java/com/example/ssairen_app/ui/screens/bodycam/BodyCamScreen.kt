package com.example.ssairen_app.ui.screens.bodycam

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ssairen_app.service.VideoRecordingService
import com.example.ssairen_app.data.api.RetrofitClient
import java.io.File

/**
 * 바디캠 녹화 화면
 *
 * 소방관들이 현장에서 비디오를 녹화할 수 있는 화면입니다.
 * 녹화 시작/중지 버튼을 제공하며, 백그라운드에서도 녹화가 계속됩니다.
 */
@Composable
fun BodyCamScreen() {
    val context = LocalContext.current

    // RetrofitClient 초기화 및 로그인 확인
    LaunchedEffect(Unit) {
        // RetrofitClient 초기화 (TokenManager 설정)
        RetrofitClient.init(context)

        val tokenManager = RetrofitClient.getTokenManager()

        // 이미 로그인되어 있으면 스킵
        if (tokenManager.isLoggedIn()) {
            Log.d("BodyCamScreen", "Already logged in, user: ${tokenManager.getUserName()}")
            return@LaunchedEffect
        }

        // ==============================================
        // TODO: 실제 로그인 화면에서 아래 로직 사용
        // ==============================================
        // 실제 프로젝트에서는 로그인 화면(LoginScreen)에서
        // 사용자 입력을 받아 로그인 API를 호출하고,
        // 성공 시 tokenManager.saveLoginInfo()로 저장해야 합니다.
        //
        // 현재는 테스트를 위해 하드코딩된 정보로 자동 로그인합니다.
        // ==============================================

        try {
            Log.d("BodyCamScreen", "Attempting auto-login (테스트용)...")

            // 테스트용 하드코딩 로그인 정보 (실제로는 사용자 입력)
            val testUsername = "20240007"
            val testPassword = "Password123!"
            val testUserType = "PARAMEDIC"

            val loginRequest = com.example.ssairen_app.data.api.LoginRequest(
                userType = testUserType,
                username = testUsername,
                password = testPassword
            )

            val response = RetrofitClient.fileApiService.login(loginRequest)

            if (response.isSuccessful && response.body()?.success == true) {
                val tokenData = response.body()?.data
                if (tokenData != null) {
                    // TokenManager에 로그인 정보 저장
                    tokenManager.saveLoginInfo(
                        tokenResponse = tokenData,
                        loginUsername = testUsername,
                        loginPassword = testPassword,
                        loginUserType = testUserType
                    )
                    Log.d("BodyCamScreen", "Auto-login successful, user: ${tokenManager.getUserName()}")
                } else {
                    Log.e("BodyCamScreen", "Auto-login failed: no token data")
                }
            } else {
                Log.e("BodyCamScreen", "Auto-login failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("BodyCamScreen", "Auto-login error", e)
        }
    }

    // 녹화 상태
    var isRecording by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    var statusMessage by remember { mutableStateOf("대기 중") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 서비스 바인딩
    var videoService by remember { mutableStateOf<VideoRecordingService?>(null) }
    var isBound by remember { mutableStateOf(false) }

    // 서비스 연결 관리
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as VideoRecordingService.LocalBinder
                videoService = binder.getService()
                isBound = true
                Log.d("BodyCamScreen", "Service connected")

                // 콜백 설정
                videoService?.setRecordingCallbacks(
                    onStarted = {
                        isRecording = true
                        statusMessage = "녹화 중"
                        errorMessage = null
                    },
                    onStopped = { file ->
                        isRecording = false
                        statusMessage = if (file != null) "녹화 완료, 업로드 중..." else "녹화 중지됨"
                        recordingDuration = 0
                    },
                    onError = { error ->
                        errorMessage = error
                        statusMessage = "오류 발생"
                        isRecording = false
                    },
                    onUploadComplete = { objectName ->
                        statusMessage = "업로드 완료"
                        errorMessage = null
                    },
                    onProgress = { durationSeconds ->
                        recordingDuration = durationSeconds.toInt()
                    }
                )

                // 서비스 연결 시 현재 녹화 상태 확인 (앱 재진입 대응)
                if (videoService?.isCurrentlyRecording() == true) {
                    isRecording = true
                    statusMessage = "녹화 중"
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                videoService = null
                isBound = false
                Log.d("BodyCamScreen", "Service disconnected")
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
            statusMessage = "권한 승인됨, 준비 완료"
            errorMessage = null
        } else {
            errorMessage = "필요한 권한이 거부되었습니다."
            statusMessage = "권한 필요"
        }
    }

    // 권한 확인
    fun checkPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 녹화 시작
    fun startRecording() {
        if (!checkPermissions()) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
            return
        }

        if (!isBound) {
            // 서비스 바인딩
            val intent = Intent(context, VideoRecordingService::class.java)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        // 서비스 시작 및 녹화 시작
        val intent = Intent(context, VideoRecordingService::class.java).apply {
            action = VideoRecordingService.ACTION_START_RECORDING
        }
        ContextCompat.startForegroundService(context, intent)
    }

    // 녹화 중지
    fun stopRecording() {
        videoService?.stopRecording()
    }

    // 화면 정리 시 서비스 언바인드
    DisposableEffect(Unit) {
        onDispose {
            if (isBound) {
                context.unbindService(serviceConnection)
            }
        }
    }

    // UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 타이틀
            Text(
                text = "바디캠",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 상태 표시
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2a2a2a)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "상태",
                        fontSize = 16.sp,
                        color = Color(0xFF999999)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = statusMessage,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) Color(0xFFff3b30) else Color.White
                    )

                    if (isRecording) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // 녹화 시간 표시
                        val minutes = recordingDuration / 60
                        val seconds = recordingDuration % 60
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFff3b30)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color(0xFFff3b30),
                            strokeWidth = 3.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 오류 메시지
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3a1a1a)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = error,
                        fontSize = 14.sp,
                        color = Color(0xFFff3b30),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // 녹화 버튼
            Button(
                onClick = {
                    if (isRecording) {
                        stopRecording()
                    } else {
                        startRecording()
                    }
                },
                modifier = Modifier
                    .size(120.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color(0xFFff3b30) else Color(0xFF3b7cff)
                )
            ) {
                Text(
                    text = if (isRecording) "중지" else "녹화",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // 안내 텍스트
            Text(
                text = if (isRecording) {
                    "녹화 중입니다.\n앱을 닫아도 백그라운드에서 계속 녹화됩니다.\n7분마다 자동으로 파일이 분할됩니다."
                } else {
                    "녹화 버튼을 눌러 바디캠 녹화를 시작하세요.\nFull HD (1920x1080) 화질로 녹화됩니다.\n7분마다 자동으로 파일이 분할되어 저장됩니다."
                },
                fontSize = 14.sp,
                color = Color(0xFF999999),
                modifier = Modifier.padding(horizontal = 32.dp),
                lineHeight = 20.sp
            )
        }

        // 설정 정보 (하단)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MinIO 업로드: 자동",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = "화질: Full HD (1920x1080)",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
            Text(
                text = "자동 분할: 7분마다",
                fontSize = 12.sp,
                color = Color(0xFF666666)
            )
        }
    }
}
