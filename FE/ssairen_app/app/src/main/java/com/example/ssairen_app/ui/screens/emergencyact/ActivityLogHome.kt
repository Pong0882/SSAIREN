// ActivityLogHome.kt
package com.example.ssairen_app.ui.screens.emergencyact

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

@Composable
fun ActivityLogHome(
    initialTab: Int = 0,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    viewModel: LogViewModel = viewModel()
) {
    var selectedLogTab by remember { mutableIntStateOf(initialTab) }
    var selectedBottomTab by remember { mutableIntStateOf(1) }

    val activityLogData by viewModel.activityLogData.collectAsState()
    val lastSavedTime by viewModel.lastSavedTime.collectAsState()

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
            onTabSelected = {
                selectedLogTab = it
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 내용 영역 - ✅ 파일명에 맞게 수정
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
//                1 -> PatientEva(
//                    viewModel = viewModel,
//                    data = activityLogData
//                )
//                2 -> PatientTransport(
//                    viewModel = viewModel,
//                    data = activityLogData
//                )
//                3 -> DispatchSection(
//                    viewModel = viewModel,
//                    data = activityLogData
//                )
//                4 -> PatienType(
//                    viewModel = viewModel,
//                    data = activityLogData
//                )
//                5 -> FirstAid(
//                    viewModel = viewModel,
//                    data = activityLogData
//                )
//                6 -> MedicalGuidance(
//                    viewModel = viewModel,
//                    data = activityLogData
//                )
//                7 -> ReportDetail(
//                    viewModel = viewModel,
//                    data = activityLogData
//                )
            }
        }

        // 4. 하단 네비게이션
        EmergencyNav(
            selectedTab = selectedBottomTab,
            onTabSelected = {
                selectedBottomTab = it
                if (it == 0) {
                    // viewModel.submitToDatabase()
                    onNavigateToHome()
                }
            }
        )
    }
}