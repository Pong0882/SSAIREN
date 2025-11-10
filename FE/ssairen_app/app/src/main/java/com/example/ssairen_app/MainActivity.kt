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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ssairen_app.ui.context.DispatchProvider
import com.example.ssairen_app.ui.screens.report.ReportHome
import com.example.ssairen_app.ui.screens.emergencyact.ActivityMain
import com.example.ssairen_app.ui.screens.emergencyact.ActivityLogHome
import com.example.ssairen_app.ui.screens.Summation
import com.example.ssairen_app.ui.screens.Login  // ⭐ 추가
import com.example.ssairen_app.viewmodel.AuthViewModel  // ⭐ 추가
import com.example.ssairen_app.data.api.RetrofitClient  // ⭐ 바디캠 업로드용

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
            // ✅ autoCreateDispatch = true로 변경 - 임의로 모달창 자동 생성
            DispatchProvider(autoCreateDispatch = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1a1a1a)
                ) {
                    AppRoot()  // ⭐ 변경
                }
            }
        }
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
    viewModel: AuthViewModel = viewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.observeAsState(false)

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
                    navController.navigate("activity_log/0/0")
                },
                onNavigateToPatientInfo = {
                    navController.navigate("activity_log/0/0")
                },
                onNavigateToPatientType = {
                    navController.navigate("activity_log/0/2")
                },
                onNavigateToPatientEva = {
                    navController.navigate("activity_log/0/3")
                },
                onNavigateToFirstAid = {
                    navController.navigate("activity_log/0/4")
                },
                onNavigateToDispatch = {
                    navController.navigate("activity_log/0/1")
                },
                onNavigateToMedicalGuidance = {
                    navController.navigate("activity_log/0/5")
                },
                onNavigateToPatientTransport = {
                    navController.navigate("activity_log/0/6")
                },
                onNavigateToReportDetail = {
                    navController.navigate("activity_log/0/7")
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