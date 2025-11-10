//MainActivity.kt
package com.example.ssairen_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ssairen_app.data.websocket.DispatchMessage
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ssairen_app.ui.context.DispatchProvider
import com.example.ssairen_app.ui.context.rememberDispatchState
import com.example.ssairen_app.ui.screens.report.ReportHome
import com.example.ssairen_app.ui.screens.emergencyact.ActivityMain
import com.example.ssairen_app.ui.screens.emergencyact.ActivityLogHome
import com.example.ssairen_app.ui.screens.Summation
import com.example.ssairen_app.ui.screens.Login  // ⭐ 추가
import com.example.ssairen_app.viewmodel.AuthViewModel  // ⭐ 추가
import com.example.ssairen_app.data.api.RetrofitClient  // ⭐ 바디캠 업로드용
import com.example.ssairen_app.ui.components.DispatchModal  // ⭐ 모달 추가
import com.example.ssairen_app.service.MyFirebaseMessagingService  // ⭐ FCM 서비스

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // 알림 권한 요청 런처
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "✅ 알림 권한 허용됨")
        } else {
            Log.w(TAG, "⚠️ 알림 권한 거부됨 - 생체신호 이상 알림을 받을 수 없습니다")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // RetrofitClient 초기화 (바디캠 비디오 업로드용)
        RetrofitClient.init(this)

        // Android 13 이상에서 알림 권한 요청
        requestNotificationPermission()

        setContent {
            DispatchProvider(autoCreateDispatch = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1a1a1a)
                ) {
                    AppRoot(intent = intent)  // ⭐ Intent 전달
                }
            }
        }
    }

    // ✅ 새로운 Intent 수신 (앱이 이미 실행 중일 때)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d(TAG, "📩 New Intent received")
    }

    // ✅ 앱이 포그라운드로 들어올 때
    override fun onResume() {
        super.onResume()
        MyFirebaseMessagingService.isAppInForeground = true
        Log.d(TAG, "✅ App is now in FOREGROUND - WebSocket will handle messages")
    }

    // ✅ 앱이 백그라운드로 갈 때
    override fun onPause() {
        super.onPause()
        MyFirebaseMessagingService.isAppInForeground = false
        Log.d(TAG, "❌ App is now in BACKGROUND - FCM will handle messages")
    }

    /**
     * 알림 권한 요청 (Android 13 이상)
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "✅ 알림 권한이 이미 허용되어 있습니다")
                }
                else -> {
                    Log.d(TAG, "📱 알림 권한 요청 중...")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d(TAG, "✅ Android 13 미만 - 알림 권한 요청 불필요")
        }
    }
}

// ⭐ 새로 추가: 로그인 분기 처리
@Composable
fun AppRoot(
    viewModel: AuthViewModel = viewModel(),
    intent: Intent? = null
) {
    val isLoggedIn by viewModel.isLoggedIn.observeAsState(false)

    // ✅ DispatchContext 가져오기
    val dispatchState = rememberDispatchState()

    // ✅ WebSocket 메시지 관찰
    val dispatchMessage by viewModel.dispatchMessage.observeAsState()

    // ✅ WebSocket 메시지 수신 시 DispatchContext에 전달
    LaunchedEffect(dispatchMessage) {
        dispatchMessage?.let { message ->
            Log.d("AppRoot", "📩 Dispatch message received: $message")
            // 이미 모달이 떠있으면 무시 (새 출동 지령만 처리)
            if (!dispatchState.showDispatchModal) {
                dispatchState.createDispatchFromWebSocket(message)
            } else {
                Log.d("AppRoot", "⚠️ Modal already showing, skipping dispatch")
            }
            // 즉시 클리어해서 다음 메시지 받을 수 있게
            viewModel.clearDispatchMessage()
        }
    }

    // ✅ FCM 알림 클릭으로 들어온 경우 모달 띄우기
    LaunchedEffect(intent) {
        intent?.let {
            if (it.getBooleanExtra("from_notification", false)) {
                Log.d("AppRoot", "📲 Opened from FCM notification")

                // Intent에서 출동 데이터 추출
                val dispatchFromIntent = DispatchMessage(
                    fireStateId = it.getStringExtra("fireStateId")?.toIntOrNull() ?: 0,
                    paramedicId = it.getStringExtra("paramedicId")?.toIntOrNull() ?: 0,
                    disasterNumber = it.getStringExtra("disasterNumber") ?: "UNKNOWN",
                    disasterType = it.getStringExtra("disasterType") ?: "긴급출동",
                    disasterSubtype = it.getStringExtra("disasterSubtype"),
                    reporterName = it.getStringExtra("reporterName"),
                    reporterPhone = it.getStringExtra("reporterPhone"),
                    locationAddress = it.getStringExtra("locationAddress") ?: "위치 정보 없음",
                    incidentDescription = it.getStringExtra("incidentDescription"),
                    dispatchLevel = it.getStringExtra("dispatchLevel"),
                    dispatchOrder = it.getStringExtra("dispatchOrder")?.toIntOrNull(),
                    dispatchStation = it.getStringExtra("dispatchStation"),
                    date = it.getStringExtra("date")
                )

                Log.d("AppRoot", "📩 Creating dispatch modal from notification: $dispatchFromIntent")
                dispatchState.createDispatchFromWebSocket(dispatchFromIntent)

                // Intent 플래그 제거 (다시 안 뜨도록)
                it.removeExtra("from_notification")
            }
        }
    }

    if (isLoggedIn) {
        // ✅ 로그인됨 → 메인 네비게이션
        AppNavigation(
            onLogout = {
                viewModel.logout()  // ✅ ViewModel의 logout 호출
            }
        )
    } else {
        // ❌ 로그인 안됨 → 로그인 화면
        Login(
            onLoginSuccess = {
                // 로그인 성공 시 자동으로 isLoggedIn이 true가 되어
                // AppNavigation으로 전환됨
            }
        )
    }
}

@Composable
fun AppNavigation(
    onLogout: () -> Unit  // ✅ 로그아웃 콜백 추가
) {
    val navController = rememberNavController()

    // ✅ DispatchContext 가져오기
    val dispatchState = rememberDispatchState()

    // ✅ 출동 모달 표시
    if (dispatchState.showDispatchModal && dispatchState.activeDispatch != null) {
        DispatchModal(
            dispatch = dispatchState.activeDispatch!!,
            onAccept = {
                // 출동 수락 처리
                Log.d("MainActivity", "✅ 출동 수락: ${dispatchState.activeDispatch?.id}")
                dispatchState.closeDispatchModal()

                // TODO: 출동 수락 후 액티비티 화면으로 이동
                navController.navigate("activity_main")
            },
            onDismiss = {
                // 모달 닫기
                Log.d("MainActivity", "❌ 출동 모달 닫기")
                dispatchState.closeDispatchModal()
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = "report_home"
    ) {
        composable("report_home") {
            ReportHome(
                onNavigateToActivityLog = {
                    navController.navigate("activity_log/0")
                },
                onLogout = onLogout  // ✅ 로그아웃 연결
            )
        }

        composable("activity_main") {
            ActivityMain(
                onNavigateToActivityLog = {
                    navController.navigate("activity_log/0")
                },
                onNavigateToPatientInfo = {
                    navController.navigate("activity_log/0")
                },
                onNavigateToPatientType = {
                    navController.navigate("activity_log/2")
                },
                onNavigateToPatientEva = {
                    navController.navigate("activity_log/3")
                },
                onNavigateToFirstAid = {
                    navController.navigate("activity_log/4")
                },
                onNavigateToDispatch = {
                    navController.navigate("activity_log/1")
                },
                onNavigateToMedicalGuidance = {
                    navController.navigate("activity_log/5")
                },
                onNavigateToPatientTransport = {
                    navController.navigate("activity_log/6")
                },
                onNavigateToReportDetail = {
                    navController.navigate("activity_log/7")
                }
            )
        }

        composable(
            route = "activity_log/{tab}",
            arguments = listOf(navArgument("tab") { defaultValue = 0 })
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tab") ?: 0
            ActivityLogHome(
                initialTab = tabIndex,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate("activity_main") {
                        popUpTo("activity_log/{tab}") { inclusive = true }
                    }
                },
                onNavigateToSummation = {
                    navController.navigate("summation")
                }
            )
        }

        composable("summation") {
            Summation(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate("activity_main") {
                        popUpTo("summation") { inclusive = true }
                    }
                },
                onNavigateToActivityLog = {
                    navController.navigate("activity_log/0")
                }
            )
        }
    }
}