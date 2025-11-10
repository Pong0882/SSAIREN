// ActivityLogHome.kt
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

@Composable
fun ActivityLogHome(
    initialTab: Int = 0,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToSummation: () -> Unit = {},
    viewModel: LogViewModel = viewModel()
) {
    var selectedLogTab by remember { mutableIntStateOf(initialTab) }
    var selectedBottomTab by remember { mutableIntStateOf(1) }

    val activityLogData by viewModel.activityLogData.collectAsState()
    val lastSavedTime by viewModel.lastSavedTime.collectAsState()

    // ✅ 탭 변경 전에 현재 데이터를 백엔드에 저장하는 함수
    fun saveCurrentTabToBackend() {
        Log.d("ActivityLogHome", "💾 탭 변경 감지 - 백엔드 저장 시작")
        Log.d("ActivityLogHome", "   - 현재 탭: $selectedLogTab")

        viewModel.saveToBackend(selectedLogTab)

        Log.d("ActivityLogHome", "✅ 백엔드 저장 완료")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .statusBarsPadding()
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
                if (lastSavedTime.isNotEmpty()) {
                    Text(
                        text = "마지막 저장: $lastSavedTime",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
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
                    data = activityLogData
                )
                1 -> Text("구급출동", color = Color.White)  // TODO: DispatchSection()
                2 -> {
                    // ✅ 임시: PatientType 화면 (빌드 오류로 주석 처리)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1a1a1a)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "환자발생유형 (준비 중)",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    // PatientType(viewModel = viewModel, data = activityLogData)
                }
                3 -> {
                    // ✅ 임시: PatientEva 화면 (빌드 오류로 주석 처리)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1a1a1a)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "환자평가 (준비 중)",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    // PatientEva(viewModel = viewModel, data = activityLogData)
                }
                4 -> {
                    // ✅ 임시: FirstAid 화면 (빌드 오류로 주석 처리)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1a1a1a)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "응급처치 (준비 중)",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                    // FirstAid(viewModel = viewModel, data = activityLogData)
                }
                5 -> Text("의료지도", color = Color.White)  // TODO: MedicalGuidance()
                6 -> Text("환자이송", color = Color.White)  // TODO: PatientTransport()
                7 -> Text("세부상황표", color = Color.White)  // TODO: ReportDetail()
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