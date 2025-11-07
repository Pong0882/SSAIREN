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
import androidx.navigation.navArgument
import com.example.ssairen_app.ui.context.DispatchProvider
import com.example.ssairen_app.ui.screens.report.ReportHome
import com.example.ssairen_app.ui.screens.emergencyact.ActivityMain
import com.example.ssairen_app.ui.screens.emergencyact.ActivityLogHome
import com.example.ssairen_app.ui.screens.Summation

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
                onNavigateToActivityLog = {
                    navController.navigate("activity_log/0") // ✅ 수정
                }
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