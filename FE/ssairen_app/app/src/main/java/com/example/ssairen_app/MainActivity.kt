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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

        // Android 13 이상에서 알림 권한 요청
        requestNotificationPermission()

        setContent {
            DispatchProvider(autoCreateDispatch = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF1a1a1a)
                ) {
                    AppNavigation()
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "report_home"
    ) {
        composable("report_home") {
            ReportHome(
                onNavigateToActivityLog = {
                    navController.navigate("activity_log/0") // ✅ 수정
                }
            )
        }

        composable("activity_main") {
            ActivityMain(
                onNavigateToActivityLog = {
                    navController.navigate("activity_log/0") // ✅ 수정
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
                }
            )
        }

        // ✅ 구급활동일지 화면 (정의는 올바름)
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

        // ✅ 요약본 화면
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
                    navController.navigate("activity_log/0") // ✅ 수정
                }
            )
        }
    }
}