// Summation.kt
package com.example.ssairen_app.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.ui.navigation.EmergencyNav
import com.example.ssairen_app.viewmodel.LogViewModel
import com.example.ssairen_app.viewmodel.ActivityViewModel

@Composable
fun Summation(
    emergencyReportId: Int,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToActivityLog: (Int) -> Unit = {},
    logViewModel: LogViewModel = viewModel(),
    activityViewModel: ActivityViewModel = viewModel()
) {
    var selectedBottomTab by remember { mutableIntStateOf(2) }

    // ✅ ViewModel에서 실제 데이터 가져오기
    val activityLogData by logViewModel.activityLogData.collectAsState()

    // ✅ emergencyReportId 설정 및 API 호출
    LaunchedEffect(emergencyReportId) {
        if (emergencyReportId > 0) {
            Log.d("Summation", "📝 emergencyReportId 설정: $emergencyReportId")
            activityViewModel.setEmergencyReportId(emergencyReportId)

            // 모든 섹션의 데이터 불러오기
            activityViewModel.getDispatch(emergencyReportId)
            activityViewModel.getPatientType(emergencyReportId)
            activityViewModel.getPatientEva(emergencyReportId)
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
                text = "요약본",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. 요약 테이블
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            SummaryTable(data = activityLogData)
        }

        // 3. 하단 네비게이션
        EmergencyNav(
            selectedTab = selectedBottomTab,
            onTabSelected = {
                selectedBottomTab = it
                when (it) {
                    0 -> onNavigateToHome()
                    1 -> onNavigateToActivityLog(emergencyReportId)  // ✅ ID 전달
                    2 -> { /* 현재 화면 유지 */ }
                    3 -> { /* TODO: 메모 */ }
                    4 -> { /* TODO: 병원이송 */ }
                }
            }
        )
    }
}

@Composable
private fun SummaryTable(data: com.example.ssairen_app.viewmodel.ActivityLogData) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        color = Color(0xFF1a1a1a)  // ✅ 검은 배경
    ) {
        Column {
            // 환자 발생 장소
            TableRow(
                label = "환자 발생 장소",
                content = {
                    TableCell(
                        text = data.dispatch.sceneLocationName,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )

            // 환자 증상
            TableRow(
                label = "환자 증상",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        // 통증
                        if (data.dispatch.painSymptoms.isNotEmpty()) {
                            TableSubRow(
                                label = "통증",
                                value = data.dispatch.painSymptoms.joinToString(", ")
                            )
                        }

                        // 외상
                        if (data.dispatch.traumaSymptoms.isNotEmpty()) {
                            TableSubRow(
                                label = "외상",
                                value = data.dispatch.traumaSymptoms.joinToString(", ")
                            )
                        }

                        // 기타 증상
                        if (data.dispatch.otherSymptoms.isNotEmpty()) {
                            TableSubRow(
                                label = "기타 증상",
                                value = data.dispatch.otherSymptoms.joinToString(", ")
                            )
                        }
                    }
                }
            )

            // 병력
            TableRow(
                label = "병력",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TableCell(
                            text = data.patienType.hasMedicalHistory,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (data.patienType.medicalHistoryList.isNotEmpty()) {
                            TableCell(
                                text = data.patienType.medicalHistoryList.joinToString(", "),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            )

            // 범죄의심
            TableRow(
                label = "범죄의심",
                content = {
                    TableCell(
                        text = data.patienType.crimeOption.ifEmpty { "없음" },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )

            // 질병
            if (data.patienType.mainType == "질병") {
                TableRow(
                    label = "질병",
                    content = {
                        TableCell(
                            text = "질병",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }

            // 질병 외
            if (data.patienType.mainType == "질병 외") {
                TableRow(
                    label = "질병 외",
                    content = {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (data.patienType.subType.isNotEmpty()) {
                                TableSubRow(
                                    label = "유형",
                                    value = data.patienType.subType
                                )
                            }

                            if (data.patienType.accidentVictimType.isNotEmpty()) {
                                TableSubRow(
                                    label = "세부사항",
                                    value = data.patienType.accidentVictimType
                                )
                            }
                        }
                    }
                )
            }

            // 기타
            if (data.patienType.mainType == "기타") {
                TableRow(
                    label = "기타",
                    content = {
                        TableCell(
                            text = data.patienType.etcType.ifEmpty { "기타" },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }

            // 환자 평가
            TableRow(
                label = "환자 평가",
                content = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        // 환자 분류
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(
                                text = "환자 분류",
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color(0xFF2a2a2a)
                            )
                            TableCell(
                                text = data.patientEva.patientLevel.ifEmpty { "미입력" },
                                modifier = Modifier.weight(2f)
                            )
                        }

                        // 의식 상태
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(
                                text = "의식 상태",
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color(0xFF2a2a2a)
                            )
                            TableCell(text = "1차", modifier = Modifier.weight(0.5f))
                            TableCell(
                                text = when {
                                    data.patientEva.consciousness1stAlert -> "A"
                                    data.patientEva.consciousness1stVerbal -> "V"
                                    data.patientEva.consciousness1stPainful -> "P"
                                    data.patientEva.consciousness1stUnresponsive -> "U"
                                    else -> "-"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            TableCell(text = "2차", modifier = Modifier.weight(0.5f))
                            TableCell(
                                text = when {
                                    data.patientEva.consciousness2ndAlert -> "A"
                                    data.patientEva.consciousness2ndVerbal -> "V"
                                    data.patientEva.consciousness2ndPainful -> "P"
                                    data.patientEva.consciousness2ndUnresponsive -> "U"
                                    else -> "-"
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // 활력징후 헤더
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(
                                text = "활력징후",
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color(0xFF2a2a2a)
                            )
                            TableCell(text = "시각", modifier = Modifier.weight(0.8f))
                            TableCell(text = "혈압", modifier = Modifier.weight(0.8f))
                            TableCell(text = "맥박", modifier = Modifier.weight(0.6f))
                            TableCell(text = "호흡", modifier = Modifier.weight(0.6f))
                            TableCell(text = "체온", modifier = Modifier.weight(0.6f))
                            TableCell(text = "산소", modifier = Modifier.weight(0.6f))
                            TableCell(text = "혈당", modifier = Modifier.weight(0.6f))
                        }

                        // 활력징후 1차
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            TableCell(text = "1차", modifier = Modifier.weight(0.8f), backgroundColor = Color(0xFF2a2a2a))
                            TableCell(text = data.patientEva.leftTime, modifier = Modifier.weight(0.8f))
                            TableCell(text = data.patientEva.leftBloodPressure, modifier = Modifier.weight(0.8f))
                            TableCell(text = data.patientEva.leftPulse, modifier = Modifier.weight(0.6f))
                            TableCell(text = data.patientEva.leftRespiratoryRate, modifier = Modifier.weight(0.6f))
                            TableCell(text = data.patientEva.leftTemperature, modifier = Modifier.weight(0.6f))
                            TableCell(text = data.patientEva.leftOxygenSaturation, modifier = Modifier.weight(0.6f))
                            TableCell(text = data.patientEva.leftBloodSugar, modifier = Modifier.weight(0.6f))
                        }

                        // 활력징후 2차
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            TableCell(text = "2차", modifier = Modifier.weight(0.8f), backgroundColor = Color(0xFF2a2a2a))
                            TableCell(text = data.patientEva.rightTime, modifier = Modifier.weight(0.8f))
                            TableCell(text = data.patientEva.rightBloodPressure, modifier = Modifier.weight(0.8f))
                            TableCell(text = data.patientEva.rightPulse, modifier = Modifier.weight(0.6f))
                            TableCell(text = data.patientEva.rightRespiratoryRate, modifier = Modifier.weight(0.6f))
                            TableCell(text = data.patientEva.rightTemperature, modifier = Modifier.weight(0.6f))
                            TableCell(text = data.patientEva.rightOxygenSaturation, modifier = Modifier.weight(0.6f))
                            TableCell(text = data.patientEva.rightBloodSugar, modifier = Modifier.weight(0.6f))
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun TableRow(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFF3a3a3a))  // ✅ 어두운 테두리
    ) {
        // 라벨 셀
        Box(
            modifier = Modifier
                .width(120.dp)
                .background(Color(0xFF2a2a2a))  // ✅ 어두운 회색
                .border(0.5.dp, Color(0xFF3a3a3a))
                .padding(8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White  // ✅ 흰색 글자
            )
        }

        // 내용 셀
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun TableSubRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$label:",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFaaaaaa),  // ✅ 밝은 회색
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color.White,  // ✅ 흰색 글자
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TableCell(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF1a1a1a),  // ✅ 검은 배경
    minHeight: Dp = 32.dp
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .border(0.5.dp, Color(0xFF3a3a3a))  // ✅ 어두운 테두리
            .defaultMinSize(minHeight = minHeight)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.ifEmpty { "-" },
            fontSize = 11.sp,
            color = Color.White,  // ✅ 흰색 글자
            textAlign = TextAlign.Center
        )
    }
}