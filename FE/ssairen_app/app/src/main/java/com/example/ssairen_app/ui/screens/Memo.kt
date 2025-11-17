// Memo.kt
package com.example.ssairen_app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.ui.navigation.EmergencyNav
import com.example.ssairen_app.viewmodel.ActivityViewModel

@Composable
fun Memo(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToActivityLog: () -> Unit = {},
    onNavigateToSummation: () -> Unit = {},
    onNavigateToHospitalSearch: () -> Unit = {},
    activityViewModel: ActivityViewModel = viewModel()
) {
    val globalReportId by ActivityViewModel.globalCurrentReportId.observeAsState()
    var selectedBottomTab by remember { mutableIntStateOf(3) }
    var memoText by remember { mutableStateOf("") }

    // 데이터 로드
    LaunchedEffect(globalReportId) {
        val reportId = globalReportId
        Log.d("Memo", "========================================")
        Log.d("Memo", "LaunchedEffect 실행됨")
        Log.d("Memo", "globalReportId: $reportId")
        Log.d("Memo", "========================================")

        if (reportId != null && reportId > 0) {
            Log.d("Memo", "📝 메모 화면 로드: reportId=$reportId")
        } else {
            Log.e("Memo", "❌ globalReportId가 null이거나 0입니다: $reportId")
        }
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
            Text(
                text = "메모",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 메모 입력 영역
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = memoText,
                onValueChange = { memoText = it },
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                placeholder = {
                    Text(
                        text = "메모를 입력하세요...",
                        color = Color(0xFF888888)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF2a2a2a),
                    unfocusedContainerColor = Color(0xFF2a2a2a),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF3b7cff),
                    focusedBorderColor = Color(0xFF3b7cff),
                    unfocusedBorderColor = Color(0xFF3a3a3a)
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. 하단 네비게이션
        EmergencyNav(
            selectedTab = selectedBottomTab,
            onTabSelected = {
                selectedBottomTab = it
                when (it) {
                    0 -> onNavigateToHome()
                    1 -> onNavigateToActivityLog()
                    2 -> onNavigateToSummation()
                    3 -> { /* 현재 화면 유지 */ }
                    4 -> onNavigateToHospitalSearch()
                }
            }
        )
    }
}
