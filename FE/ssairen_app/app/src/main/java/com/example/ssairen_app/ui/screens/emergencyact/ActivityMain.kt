// ActivityMain.kt
package com.example.ssairen_app.ui.screens.emergencyact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import androidx.compose.ui.text.style.TextAlign
import com.example.ssairen_app.ui.components.DarkCard
import com.example.ssairen_app.ui.navigation.EmergencyNav
import com.example.ssairen_app.ui.components.MainButton
import com.example.ssairen_app.ui.wear.WearDataViewModel

@Composable
fun ActivityMain(
    onNavigateToActivityLog: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .statusBarsPadding()
    ) {
        // 메인 콘텐츠 영역
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> HomeContent(
                    onNavigateToActivityLog = onNavigateToActivityLog
                )
                1 -> Text("구급활동일지 화면", color = Color.White)
                2 -> Text("요약 화면", color = Color.White)
                3 -> Text("메모 화면", color = Color.White)
                4 -> Text("병원이송 화면", color = Color.White)
            }
        }

        // 하단 네비게이션
        EmergencyNav(
            selectedTab = selectedTab,
            onTabSelected = {
                selectedTab = it
                if (it == 1) {
                    onNavigateToActivityLog()
                }
            }
        )
    }
}

@Composable
private fun HomeContent(
    onNavigateToActivityLog: () -> Unit = {}
) {
    var isRecording by remember { mutableStateOf(false) }  // ✅ 녹음 상태

    // ✅ Wear 데이터 ViewModel (Singleton 사용)
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val wearViewModel: WearDataViewModel = remember {
        WearDataViewModel.getInstance(application)
    }

    Log.d("ActivityMain", "🎨 HomeContent Composable 렌더링")
    Log.d("ActivityMain", "📱 ViewModel 인스턴스: $wearViewModel")

    // ✅ Wear에서 전송된 실시간 데이터
    val heartRate by wearViewModel.heartRate.collectAsState()
    val spo2 by wearViewModel.spo2.collectAsState()
    val spo2ErrorMessage by wearViewModel.spo2ErrorMessage.collectAsState()
    val hrStatusMessage by wearViewModel.hrStatusMessage.collectAsState()

    Log.d("ActivityMain", "📊 현재 UI에 표시되는 값 - HR: $heartRate, SpO2: $spo2, SpO2 에러: '$spo2ErrorMessage', HR 상태: '$hrStatusMessage'")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 상단 타이틀
        Text(
            text = "메인화면",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 34.dp, bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 좌측 영역 (차트 + 통계)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 차트 카드
                DarkCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "차트 영역",
                            color = Color(0xFF666666),
                            fontSize = 14.sp
                        )
                    }
                }

                // 통계 카드들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 심박수(맥박) - ✅ Wear에서 실시간 업데이트
                    StatCard(
                        title = "심박수(맥박)",
                        value = if (hrStatusMessage.isNotEmpty()) hrStatusMessage
                               else if (heartRate > 0) "$heartRate bpm" else "--",
                        modifier = Modifier.weight(1f),
                        valueColor = if (hrStatusMessage.isNotEmpty()) Color(0xFFFF9800) else Color(0xFF00d9ff),
                        isStatusMessage = hrStatusMessage.isNotEmpty()
                    )

                    // 산소포화도(SpO2) - ✅ Wear에서 실시간 업데이트
                    StatCard(
                        title = "산소포화도(SpO2)",
                        value = if (spo2ErrorMessage.isNotEmpty()) spo2ErrorMessage
                               else if (spo2 > 0) "$spo2%" else "--",
                        modifier = Modifier.weight(1f),
                        valueColor = if (spo2ErrorMessage.isNotEmpty()) Color(0xFFFF5252) else Color(0xFF00d9ff),
                        isStatusMessage = spo2ErrorMessage.isNotEmpty()
                    )
                }

                // ✅ 카메라 + 녹음 버튼
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 카메라 버튼
                    IconButton(
                        onClick = { /* 카메라 실행 */ },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF2a2a2a), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "카메라",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // ✅ 녹음 버튼
                    IconButton(
                        onClick = {
                            isRecording = !isRecording
                            // TODO: 녹음 시작/중지 로직
                            if (isRecording) {
                                println("🎤 녹음 시작")
                            } else {
                                println("⏹️ 녹음 중지")
                            }
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                if (isRecording) Color(0xFFff3b30) else Color(0xFF2a2a2a),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                            contentDescription = if (isRecording) "녹음 중지" else "녹음 시작",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // 우측 메뉴 버튼들
            Column(
                modifier = Modifier.width(140.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 환자정보 버튼
                MainButton(
                    onClick = onNavigateToActivityLog,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "환자정보",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "환자정보",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                MainButton(
                    onClick = { /* 환자평가 화면으로 이동 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "환자평가",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "환자평가",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                MainButton(
                    onClick = { /* 환자처치 화면으로 이동 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "환자처치",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "환자처치",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                MainButton(
                    onClick = { /* 구금조치 화면으로 이동 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "구금조치",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "구금조치",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                MainButton(
                    onClick = { /* 환자 발생 유형 화면으로 이동 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "환자 발생 유형",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "환자 발생 유형",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                MainButton(
                    onClick = { /* 응급처치 화면으로 이동 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "응급처치",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "응급처치",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                MainButton(
                    onClick = { /* 의료지도 화면으로 이동 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "의료지도",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "의료지도",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                MainButton(
                    onClick = { /* 세부 상황정보 화면으로 이동 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    backgroundColor = Color(0xFF2a2a2a),
                    cornerRadius = 8.dp
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "세부 상황정보",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "세부 상황정보",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ==========================================
// 통계 카드 컴포넌트
// ==========================================
@Composable
private fun StatCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    isStatusMessage: Boolean = false
) {
    DarkCard(
        modifier = modifier.height(100.dp),  // 높이 고정
        cornerRadius = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color(0xFF999999),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = if (isStatusMessage) 12.sp else 32.sp,
                fontWeight = if (isStatusMessage) FontWeight.Medium else FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = if (isStatusMessage) 2 else 1
            )
        }
    }
}