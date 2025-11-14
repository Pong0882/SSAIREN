// ActivityLogHome(네비게이션이 2개가 있는 진입점 홈).kt
package com.example.ssairen_app.ui.screens.emergencyact

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.ui.navigation.ActivityLogNavigationBar
import com.example.ssairen_app.ui.navigation.EmergencyNav
import com.example.ssairen_app.viewmodel.LogViewModel
import com.example.ssairen_app.viewmodel.ActivityViewModel
import com.example.ssairen_app.viewmodel.SaveState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ActivityLogHome(
    emergencyReportId: Int,
    initialTab: Int = 0,
    isReadOnly: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSummation: () -> Unit = {},
    viewModel: LogViewModel = viewModel(),
    activityViewModel: ActivityViewModel = viewModel()
) {
    var selectedLogTab by remember { mutableIntStateOf(initialTab) }
    var selectedBottomTab by remember { mutableIntStateOf(1) }

    val activityLogData by viewModel.activityLogData.collectAsState()
    val lastSavedTime by viewModel.lastSavedTime.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    // ✅ Snackbar 상태
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ emergencyReportId를 두 ViewModel에 모두 설정
    LaunchedEffect(emergencyReportId) {
        if (emergencyReportId > 0) {
            Log.d("ActivityLogHome", "📝 emergencyReportId 설정: $emergencyReportId")

            // ActivityViewModel에 설정
            activityViewModel.setEmergencyReportId(emergencyReportId)
            com.example.ssairen_app.viewmodel.ActivityViewModel.setGlobalReportId(emergencyReportId)

            // ✅ LogViewModel에도 설정
            viewModel.setEmergencyReportId(emergencyReportId)
        } else {
            Log.w("ActivityLogHome", "⚠️ 유효하지 않은 emergencyReportId: $emergencyReportId")
        }
    }

    // ✅ SaveState 관찰 및 UI 피드백
    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                val message = (saveState as SaveState.Success).message
                Log.d("ActivityLogHome", "✅ 저장 성공: $message")
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
            is SaveState.Error -> {
                val message = (saveState as SaveState.Error).message
                Log.e("ActivityLogHome", "❌ 저장 실패: $message")
                snackbarHostState.showSnackbar(
                    message = "저장 실패: $message",
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }

    // ✅ 탭 변경 전에 현재 데이터를 백엔드에 저장하는 함수
    fun saveCurrentTabToBackend() {
        if (isReadOnly) {
            Log.d("ActivityLogHome", "🔒 읽기 전용 모드 - 저장 차단")
            return
        }
        Log.d("ActivityLogHome", "💾 탭 변경 감지 - 백엔드 저장 시작")
        Log.d("ActivityLogHome", "   - 현재 탭: $selectedLogTab")

        viewModel.saveToBackend(selectedLogTab)

        Log.d("ActivityLogHome", "✅ 백엔드 저장 요청 완료")
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF2a2a2a),
                    contentColor = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1a1a1a))
                .statusBarsPadding()
                .padding(paddingValues)
        ) {
            // 1. 상단 타이틀 + 뒤로가기
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "구급활동일지",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (lastSavedTime.isNotEmpty()) {
                            Text(
                                text = "마지막 저장: $lastSavedTime",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        // ✅ 저장 중 표시
                        if (saveState is SaveState.Saving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF3b7cff)
                            )
                            Text(
                                text = "저장 중...",
                                color = Color(0xFF3b7cff),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 8개 탭 네비게이션
            ActivityLogNavigationBar(
                selectedTab = selectedLogTab,
                onTabSelected = { newTab ->
                    if (selectedLogTab != newTab) {
                        saveCurrentTabToBackend()
                        selectedLogTab = newTab
                        Log.d("ActivityLogHome", "📑 상단 탭 변경: $selectedLogTab → $newTab")
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 내용 영역
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedLogTab) {
                    0 -> PatientInfo(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    1 -> DispatchSection(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    2 -> PatientType(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    3 -> PatientEva(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    4 -> FirstAid(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    5 -> MedicalGuidance(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    6 -> PatientTransport(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    7 -> {
                        ReportDetail(
                            viewModel = viewModel,
                            data = activityLogData,
                            isReadOnly = isReadOnly
                        )

                        DisposableEffect(Unit) {
                            onDispose {
                                CoroutineScope(Dispatchers.IO).launch {
                                    Log.d("ActivityLogHome", "🔄 세부사항 탭 벗어남 - 백그라운드 저장")
                                    viewModel.saveDetailReportSection(activityViewModel)
                                }
                            }
                        }
                    }

                }
            }

            // 4. 하단 네비게이션
            EmergencyNav(
                selectedTab = selectedBottomTab,
                onTabSelected = { newTab ->
                    if (selectedBottomTab != newTab) {
                        saveCurrentTabToBackend()
                        selectedBottomTab = newTab
                        Log.d("ActivityLogHome", "📑 하단 탭 변경: $selectedBottomTab → $newTab")

                        when (newTab) {
                            0 -> onNavigateToHome()         // 홈
                            1 -> { /* 현재 화면 유지 */ }  // 구급활동일지
                            2 -> onNavigateToSummation()    // 요약
                            3 -> { /* TODO: 메모 */ }
                            4 -> { /* TODO: 병원이송 */ }
                        }
                    }
                }
            )
        }
    }
}