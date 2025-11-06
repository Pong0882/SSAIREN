//MainActivity.kt
package com.example.ssairen_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ssairen_app.ui.context.DispatchProvider
import com.example.ssairen_app.ui.screens.report.ReportHome
import com.example.ssairen_app.ui.screens.emergencyact.ActivityMain
import com.example.ssairen_app.ui.screens.emergencyact.ActivityLogHome  // ✅ 추가

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                onNavigateToActivityLog = {  // ✅ 이름 변경
                    navController.navigate("activity_log")  // ✅ activity_log로 이동
                }
            )
        }

        composable("activity_main") {
            ActivityMain(
                onNavigateToActivityLog = {
                    navController.navigate("activity_log")  // ✅ 구급활동일지로 이동
                }
            )
        }

        // ✅ 구급활동일지 화면 추가
        composable("activity_log") {
            ActivityLogHome(
                initialTab = 0,  // 0 = 환자정보
                onNavigateBack = {
                    navController.popBackStack()  // 뒤로가기
                },
                onNavigateToHome = {
                    navController.navigate("activity_main") {  // ✅ 홈으로 이동
                        popUpTo("activity_log") { inclusive = true }
                    }
                }
            )
        }
    }
}