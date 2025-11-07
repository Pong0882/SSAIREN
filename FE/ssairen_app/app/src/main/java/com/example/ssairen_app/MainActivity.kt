//MainActivity.kt
package com.example.ssairen_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
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
}

// ⭐ 새로 추가: 로그인 분기 처리
@Composable
fun AppRoot(
    viewModel: AuthViewModel = viewModel()
) {
    val isLoggedIn by viewModel.isLoggedIn.observeAsState(false)

    if (isLoggedIn) {
        // ✅ 로그인됨 → 메인 네비게이션
        AppNavigation()
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
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "report_home"
    ) {
        composable("report_home") {
            ReportHome(
                onNavigateToActivityLog = {
                    navController.navigate("activity_log/0")
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