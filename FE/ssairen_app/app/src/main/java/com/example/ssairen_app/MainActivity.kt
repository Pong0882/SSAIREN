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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.example.ssairen_app.viewmodel.ReportViewModel  // ⭐ 새 일지 등록용
import com.example.ssairen_app.viewmodel.CreateReportState  // ⭐ 일지 생성 상태
import com.example.ssairen_app.data.api.RetrofitClient  // ⭐ 바디캠 업로드용
import com.example.ssairen_app.ui.components.DispatchModal  // ⭐ 모달 추가
import com.example.ssairen_app.ui.screens.report.DispatchDetail  // ⭐ 출동 상세 모달
import com.example.ssairen_app.ui.screens.report.DispatchDetailData  // ⭐ 출동 상세 데이터
import com.example.ssairen_app.service.MyFirebaseMessagingService  // ⭐ FCM 서비스

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    // ✅ Intent를 State로 관리
    private var currentIntent by mutableStateOf<Intent?>(null)

    // ✅ 알림에서 받은 출동 데이터를 State로 관리
    private var pendingDispatchFromNotification by mutableStateOf<DispatchMessage?>(null)

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

        Log.d(TAG, "========================================")
        Log.d(TAG, "🚀 MainActivity.onCreate() 시작")
        Log.d(TAG, "========================================")

        // RetrofitClient 초기화 (바디캠 비디오 업로드용)
        RetrofitClient.init(this)

        // Android 13 이상에서 알림 권한 요청
        requestNotificationPermission()

        // ✅ 초기 Intent 설정 및 출동 데이터 추출
        Log.d(TAG, "📱 Intent 처리 시작")
        currentIntent = intent
        extractDispatchFromIntent(intent)
        Log.d(TAG, "📱 pendingDispatchFromNotification: ${pendingDispatchFromNotification != null}")

        setContent {
            // 실제 출동지령(FCM/WebSocket)만 모달 표시
            DispatchProvider(autoCreateDispatch = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1a1a1a)
                ) {
                    AppRoot(
                        intent = currentIntent,
                        pendingDispatch = pendingDispatchFromNotification
                    )
                }
            }
        }
    }

    // ✅ 새로운 Intent 수신 (앱이 이미 실행 중일 때)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntent = intent  // ✅ State 업데이트
        extractDispatchFromIntent(intent)  // ✅ 출동 데이터 추출
        Log.d(TAG, "📩 New Intent received, State updated")
    }

    // ✅ Intent에서 출동 데이터 추출
    private fun extractDispatchFromIntent(intent: Intent?) {
        Log.d(TAG, "----------------------------------------")
        Log.d(TAG, "🔍 extractDispatchFromIntent 호출됨")

        if (intent == null) {
            Log.d(TAG, "❌ Intent가 null입니다")
            Log.d(TAG, "----------------------------------------")
            return
        }

        Log.d(TAG, "✅ Intent 존재함")

        // Intent extras 전부 출력
        val extras = intent.extras
        if (extras != null) {
            Log.d(TAG, "📦 Intent extras 내용:")
            for (key in extras.keySet()) {
                Log.d(TAG, "   $key = ${extras.get(key)}")
            }
        } else {
            Log.d(TAG, "⚠️ Intent extras가 null입니다")
        }

        val fromNotification = intent.getBooleanExtra("from_notification", false)
        // ✅ FCM data에 type=DISPATCH가 있으면 알림에서 온 것으로 판단
        val typeFromFcm = intent.getStringExtra("type")
        val isFromDispatchNotification = fromNotification || (typeFromFcm == "DISPATCH")

        Log.d(TAG, "🔔 from_notification 플래그: $fromNotification")
        Log.d(TAG, "🔔 FCM type: $typeFromFcm")
        Log.d(TAG, "🔔 최종 판단 (알림에서 옴): $isFromDispatchNotification")

        if (isFromDispatchNotification) {
            Log.d(TAG, "========================================")
            Log.d(TAG, "🚨🚨🚨 FCM 알림으로 앱 시작됨! 🚨🚨🚨")
            Log.d(TAG, "========================================")

            // Intent에서 출동 데이터 추출
            val dispatch = DispatchMessage(
                id = intent.getStringExtra("id")?.toIntOrNull() ?: 0,
                fireStateId = intent.getStringExtra("fireStateId")?.toIntOrNull() ?: 0,
                paramedicId = intent.getStringExtra("paramedicId")?.toIntOrNull() ?: 0,
                disasterNumber = intent.getStringExtra("disasterNumber") ?: "UNKNOWN",
                disasterType = intent.getStringExtra("disasterType") ?: "긴급출동",
                disasterSubtype = intent.getStringExtra("disasterSubtype"),
                reporterName = intent.getStringExtra("reporterName"),
                reporterPhone = intent.getStringExtra("reporterPhone"),
                locationAddress = intent.getStringExtra("locationAddress") ?: "위치 정보 없음",
                incidentDescription = intent.getStringExtra("incidentDescription"),
                dispatchLevel = intent.getStringExtra("dispatchLevel"),
                dispatchOrder = intent.getStringExtra("dispatchOrder")?.toIntOrNull(),
                dispatchStation = intent.getStringExtra("dispatchStation"),
                date = intent.getStringExtra("date")
            )

            Log.d(TAG, "📦 출동 데이터 추출 완료:")
            Log.d(TAG, "  ✓ 재난번호: ${dispatch.disasterNumber}")
            Log.d(TAG, "  ✓ 위치: ${dispatch.locationAddress}")
            Log.d(TAG, "  ✓ 유형: ${dispatch.disasterType}")

            pendingDispatchFromNotification = dispatch
            Log.d(TAG, "✅ pendingDispatchFromNotification에 저장 완료!")

            // Intent 플래그 제거 (중복 처리 방지)
            intent.removeExtra("from_notification")
            intent.removeExtra("type")
        } else {
            Log.d(TAG, "ℹ️ 일반 앱 시작 (알림 아님)")
        }

        Log.d(TAG, "----------------------------------------")
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
    intent: Intent? = null,
    pendingDispatch: DispatchMessage? = null
) {
    Log.d("AppRoot", "========================================")
    Log.d("AppRoot", "🎨 AppRoot Composable 렌더링")
    Log.d("AppRoot", "   - pendingDispatch: ${pendingDispatch != null}")
    if (pendingDispatch != null) {
        Log.d("AppRoot", "   - 재난번호: ${pendingDispatch.disasterNumber}")
    }
    Log.d("AppRoot", "========================================")

    val isLoggedIn by viewModel.isLoggedIn.observeAsState(false)
    Log.d("AppRoot", "🔐 isLoggedIn: $isLoggedIn")

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

    // ✅ 처리된 출동 ID 기억 (중복 처리 방지)
    val processedDispatchId = remember { mutableStateOf<String?>(null) }

    // ✅ 알림에서 받은 출동 데이터 처리 (로그인 완료 후)
    LaunchedEffect(pendingDispatch, isLoggedIn) {
        Log.d("AppRoot", "╔════════════════════════════════════════╗")
        Log.d("AppRoot", "║   LaunchedEffect 실행됨!              ║")
        Log.d("AppRoot", "╚════════════════════════════════════════╝")
        Log.d("AppRoot", "📊 상태 체크:")
        Log.d("AppRoot", "   - pendingDispatch: ${pendingDispatch != null}")
        Log.d("AppRoot", "   - isLoggedIn: $isLoggedIn")
        Log.d("AppRoot", "   - processedDispatchId: ${processedDispatchId.value}")

        // ⚠️ 로그인 상태가 아니면 처리하지 않음
        if (!isLoggedIn) {
            if (pendingDispatch != null) {
                Log.d("AppRoot", "⏳⏳⏳ Pending dispatch exists but not logged in yet")
                Log.d("AppRoot", "⏳⏳⏳ 로그인 완료되면 자동으로 처리됩니다")
            } else {
                Log.d("AppRoot", "ℹ️ 로그인 안됨 & 대기 중인 출동 없음")
            }
            return@LaunchedEffect
        }

        // ✅ 로그인 완료 + 대기 중인 출동이 있으면 모달 띄우기
        if (pendingDispatch == null) {
            Log.d("AppRoot", "ℹ️ 대기 중인 출동 없음")
            return@LaunchedEffect
        }

        Log.d("AppRoot", "✅✅✅ 조건 충족! (로그인 완료 + 출동 데이터 있음)")

        // 이미 처리한 출동인지 확인 (중복 방지)
        if (processedDispatchId.value == pendingDispatch.disasterNumber) {
            Log.d("AppRoot", "⚠️ 이미 처리한 출동입니다: ${pendingDispatch.disasterNumber}")
            return@LaunchedEffect
        }

        Log.d("AppRoot", "╔════════════════════════════════════════╗")
        Log.d("AppRoot", "║   🚨 알림 출동 처리 시작! 🚨           ║")
        Log.d("AppRoot", "╚════════════════════════════════════════╝")
        Log.d("AppRoot", "📦 출동 정보:")
        Log.d("AppRoot", "  ✓ 재난번호: ${pendingDispatch.disasterNumber}")
        Log.d("AppRoot", "  ✓ 위치: ${pendingDispatch.locationAddress}")
        Log.d("AppRoot", "  ✓ 유형: ${pendingDispatch.disasterType}")

        Log.d("AppRoot", "🎯 dispatchState.createDispatchFromWebSocket 호출 중...")
        dispatchState.createDispatchFromWebSocket(pendingDispatch)

        processedDispatchId.value = pendingDispatch.disasterNumber  // 처리 완료 표시

        Log.d("AppRoot", "╔════════════════════════════════════════╗")
        Log.d("AppRoot", "║   ✅ 모달 생성 완료! ✅                ║")
        Log.d("AppRoot", "╚════════════════════════════════════════╝")
        Log.d("AppRoot", "📌 dispatchState.showDispatchModal: ${dispatchState.showDispatchModal}")
        Log.d("AppRoot", "📌 dispatchState.activeDispatch: ${dispatchState.activeDispatch}")
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

    // ✅ ReportViewModel 가져오기
    val reportViewModel: ReportViewModel = viewModel()
    val createReportState by reportViewModel.createReportState.observeAsState(CreateReportState.Idle)

    // ✅ 일지 생성 성공 시 화면 이동
    LaunchedEffect(createReportState) {
        if (createReportState is CreateReportState.Success) {
            val emergencyReportId = (createReportState as CreateReportState.Success).reportData.emergencyReportId
            Log.d("MainActivity", "✅ 일지 생성 완료, 화면 이동: emergencyReportId=$emergencyReportId")
            dispatchState.closeDispatchModal() // 모달 닫기
            navController.navigate("activity_log/$emergencyReportId/0?isReadOnly=false")
            reportViewModel.resetCreateState()
        }
    }

    // ✅ 출동 모달 표시 (전역으로 모든 화면에서 표시)
    if (dispatchState.showDispatchModal && dispatchState.activeDispatch != null) {
        val dispatch = dispatchState.activeDispatch!!
        DispatchDetail(
            dispatchData = DispatchDetailData(
                dispatchNumber = dispatch.id,
                status = "실전/1차",
                type = dispatch.type,
                area = "관할구역",
                location = dispatch.location,
                reporter = "신고자명",
                reporterPhone = "010-0000-0000",
                dispatchTime = dispatch.date,
                address = dispatch.location,
                cause = "사고 원인 정보"
            ),
            onDismiss = {
                Log.d("MainActivity", "❌ 출동 모달 닫기")
                dispatchState.closeDispatchModal()
            },
            onCreateNewReport = {
                // 새 일지 등록 API 호출
                Log.d("MainActivity", "✅ 새 일지 등록 요청: dispatchId=${dispatch.dispatchId}")
                reportViewModel.createReport(dispatch.dispatchId)
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = "report_home"
    ) {
        composable("report_home") {
            ReportHome(
                onNavigateToActivityLog = { emergencyReportId, isReadOnly ->
                    navController.navigate("activity_log/$emergencyReportId/0?isReadOnly=$isReadOnly")
                },
                onLogout = onLogout  // ✅ 로그아웃 연결
            )
        }

        composable("activity_main") {
            ActivityMain(
                onNavigateToActivityLog = {
                    // ✅ 전역 현재 활성 보고서 ID 사용
                    val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                    navController.navigate("activity_log/$currentReportId/0")
                },
                onNavigateToPatientInfo = {
                    // ✅ 전역 현재 활성 보고서 ID 사용
                    val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                    navController.navigate("activity_log/$currentReportId/0")
                },
                onNavigateToPatientType = {
                    // ✅ 전역 현재 활성 보고서 ID 사용
                    val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                    navController.navigate("activity_log/$currentReportId/2")
                },
                onNavigateToPatientEva = {
                    // ✅ 전역 현재 활성 보고서 ID 사용
                    val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                    navController.navigate("activity_log/$currentReportId/3")
                },
                onNavigateToFirstAid = {
                    // ✅ 전역 현재 활성 보고서 ID 사용
                    val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                    navController.navigate("activity_log/$currentReportId/4")
                },
                onNavigateToDispatch = {
                    // ✅ 전역 현재 활성 보고서 ID 사용
                    val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                    navController.navigate("activity_log/$currentReportId/1")
                },
                onNavigateToMedicalGuidance = {
                    // ✅ 전역 현재 활성 보고서 ID 사용
                    val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                    navController.navigate("activity_log/$currentReportId/5")
                },
                onNavigateToPatientTransport = {
                    // ✅ 전역 현재 활성 보고서 ID 사용
                    val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                    navController.navigate("activity_log/$currentReportId/6")
                },
                onNavigateToReportDetail = {
                    // ✅ 전역 현재 활성 보고서 ID 사용
                    val currentReportId = com.example.ssairen_app.viewmodel.ActivityViewModel.getGlobalReportId()
                    navController.navigate("activity_log/$currentReportId/7")
                }
            )
        }

        composable(
            route = "activity_log/{emergencyReportId}/{tab}?isReadOnly={isReadOnly}",
            arguments = listOf(
                navArgument("emergencyReportId") { defaultValue = 0 },
                navArgument("tab") { defaultValue = 0 },
                navArgument("isReadOnly") { defaultValue = false }
            )
        ) { backStackEntry ->
            val emergencyReportId = backStackEntry.arguments?.getInt("emergencyReportId") ?: 0
            val tabIndex = backStackEntry.arguments?.getInt("tab") ?: 0
            val isReadOnly = backStackEntry.arguments?.getBoolean("isReadOnly") ?: false
            ActivityLogHome(
                emergencyReportId = emergencyReportId,
                initialTab = tabIndex,
                isReadOnly = isReadOnly,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate("activity_main") {
                        popUpTo("activity_log/{emergencyReportId}/{tab}?isReadOnly={isReadOnly}") { inclusive = true }
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
                    navController.navigate("activity_log/0/0")
                }
            )
        }
    }
}