// Summation.kt
package com.example.ssairen_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.example.ssairen_app.ui.navigation.ActivityLogNavigationBar // ❌ 삭제
import com.example.ssairen_app.ui.navigation.EmergencyNav

// ==========================================
// ✅ API 응답 데이터 클래스
// ==========================================
data class SummaryData(
    // 환자 발생 정소
    val incidentLocation: IncidentLocation = IncidentLocation(),

    // 환자 증상
    val patientSymptoms: PatientSymptoms = PatientSymptoms(),

    // 병력
    val medicalHistory: String = "",

    // 범죄의심
    val crimeStatus: String = "",

    // 실명
    val realName: String = "",

    // 발생 위
    val incidentType: IncidentType = IncidentType(),

    // 기타
    val etc: String = "",

    // 환자 평가
    val patientEvaluation: PatientEvaluation = PatientEvaluation()
)

data class IncidentLocation(
    val activity: String = "",  // 통증
    val state: String = "",     // 의식
    val etc: String = ""        // 기타
)

data class PatientSymptoms(
    val trauma: String = "",    // 두통
    val fall: String = "",      // 골절
    val etc: String = ""        // 기도이물
)

data class IncidentType(
    val mainType: String = "",      // 교통사고, 그 외 외상, 관통상 등
    val subType: String = ""
)

data class PatientEvaluation(
    val consciousness: ConsciousnessData = ConsciousnessData(),
    val vitalSigns: VitalSignsData = VitalSignsData(),
    val pupilResponse: PupilResponseData = PupilResponseData(),
    val patientLevel: String = "LEVEL 1",
    val specialNotes: SpecialNotes = SpecialNotes()
)

data class ConsciousnessData(
    val first: String = "",     // 1차: 시각 A, V, P, U
    val second: String = ""     // 2차: 시각 A, V, P, U
)

data class VitalSignsData(
    val first: VitalSign = VitalSign(),   // 1차
    val second: VitalSign = VitalSign()   // 2차
)

data class VitalSign(
    val time: String = "",
    val pulse: String = "",
    val bloodPressure: String = "",
    val temperature: String = "",
    val oxygenSaturation: String = "",
    val respiratoryRate: String = "",
    val bloodSugar: String = ""
)

data class PupilResponseData(
    val left: String = "",      // 좌: 정상, 적극
    val right: String = ""      // 우: 무반응
)

data class SpecialNotes(
    val bodyPart: String = "",          // 1차: 우, 혈압: 2차, 무반응, 제곱
    val hospital: String = "",          // 전소구로도, 행당재곽
    val memo: String = ""               // 구급대원 + 발생시간: 병가소견: + 주요소:
)

@Composable
fun Summation(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToActivityLog: () -> Unit = {}
) {
    // ✅ API에서 받아올 데이터 (현재는 더미 데이터)
    var summaryData by remember { mutableStateOf(SummaryData()) }
    var isLoading by remember { mutableStateOf(false) }

    // var selectedLogTab by remember { mutableIntStateOf(0) } // ❌ 삭제
    var selectedBottomTab by remember { mutableIntStateOf(2) }  // ✅ 요약 탭이 선택된 상태로 시작

    // TODO: API 호출 함수
    LaunchedEffect(Unit) {
        // isLoading = true
        // summaryData = fetchSummaryFromAPI()
        // isLoading = false
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
                text = "요약본-ver.3",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ❌ 2. 8개 탭 네비게이션 (삭제)
        /*
        ActivityLogNavigationBar(
            selectedTab = selectedLogTab,
            onTabSelected = { selectedLogTab = it },
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        */

        // 3. 요약 테이블
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF3b7cff)
                )
            } else {
                SummaryTable(data = summaryData)
            }
        }

        // 4. 하단 네비게이션
        EmergencyNav(
            selectedTab = selectedBottomTab,
            onTabSelected = {
                selectedBottomTab = it
                when (it) {
                    0 -> onNavigateToHome()         // 홈
                    1 -> onNavigateToActivityLog()  // ✅ 구급활동일지
                    2 -> { /* 현재 화면 유지 */ }  // 요약
                    3 -> { /* TODO: 메모 */ }
                    4 -> { /* TODO: 병원이송 */ }
                }
            }
        )
    }
}

@Composable
private fun SummaryTable(data: SummaryData) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        color = Color.White
    ) {
        Column {
            // 환자 발생 정소
            TableRow(
                label = "환자 발생 정소",
                content = {
                    Column {
                        TableSubRow(label = "통증", value = data.incidentLocation.activity)
                        TableSubRow(label = "의식", value = data.incidentLocation.state)
                        TableSubRow(label = "기타", value = data.incidentLocation.etc)
                    }
                }
            )

            // 환자 증상
            TableRow(
                label = "환자 증상",
                content = {
                    Column {
                        TableSubRow(label = "두통", value = data.patientSymptoms.trauma)
                        TableSubRow(label = "골절", value = data.patientSymptoms.fall)
                        TableSubRow(label = "기도이물", value = data.patientSymptoms.etc)
                    }
                }
            )

            // 병력
            TableRow(
                label = "병력",
                content = {
                    TableCell(text = data.medicalHistory, modifier = Modifier.fillMaxWidth())
                }
            )

            // 범죄의심
            TableRow(
                label = "범죄의심",
                content = {
                    TableCell(text = data.crimeStatus, modifier = Modifier.fillMaxWidth())
                }
            )

            // 실명
            TableRow(
                label = "실명",
                content = {
                    TableCell(text = data.realName, modifier = Modifier.fillMaxWidth())
                }
            )

            // 발생 위
            TableRow(
                label = "발생 위",
                content = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TableCell(text = data.incidentType.mainType, modifier = Modifier.weight(1f))
                        TableCell(text = data.incidentType.subType, modifier = Modifier.weight(1f))
                    }
                }
            )

            // 기타
            TableRow(
                label = "기타",
                content = {
                    TableCell(text = data.etc, modifier = Modifier.fillMaxWidth())
                }
            )

            // 환자 평가 - 의식 상태
            TableRow(
                label = "환자 평가",
                content = {
                    Column {
                        // 의식 상태
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(
                                text = "의식 상태",
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color(0xFFf0f0f0)
                            )
                            TableCell(text = "1차", modifier = Modifier.weight(1f))
                            TableCell(text = "시각", modifier = Modifier.weight(1f))
                            TableCell(text = "A", modifier = Modifier.weight(0.5f))
                            TableCell(text = "V", modifier = Modifier.weight(0.5f))
                            TableCell(text = "P", modifier = Modifier.weight(0.5f))
                            TableCell(text = "U", modifier = Modifier.weight(0.5f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            TableCell(text = "2차", modifier = Modifier.weight(1f))
                            TableCell(text = "시각", modifier = Modifier.weight(1f))
                            TableCell(text = "A", modifier = Modifier.weight(0.5f))
                            TableCell(text = "V", modifier = Modifier.weight(0.5f))
                            TableCell(text = "P", modifier = Modifier.weight(0.5f))
                            TableCell(text = "U", modifier = Modifier.weight(0.5f))
                        }

                        // 활력반응
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(
                                text = "활력반응",
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color(0xFFf0f0f0)
                            )
                            TableCell(text = "좌", modifier = Modifier.weight(1f))
                            TableCell(text = "정상", modifier = Modifier.weight(1f))
                            TableCell(text = "적극", modifier = Modifier.weight(1f))
                            TableCell(text = "2차", modifier = Modifier.weight(1f))
                            TableCell(text = "무반응", modifier = Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            TableCell(text = "우", modifier = Modifier.weight(1f))
                            TableCell(text = "1차", modifier = Modifier.weight(1f))
                            TableCell(text = "적극", modifier = Modifier.weight(1f))
                            TableCell(text = "혈압", modifier = Modifier.weight(1f))
                            TableCell(text = "요률", modifier = Modifier.weight(1f))
                        }

                        // 활력징후
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(
                                text = "활력징후",
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color(0xFFf0f0f0)
                            )
                            TableCell(text = "시각", modifier = Modifier.weight(1f))
                            TableCell(text = "혈압", modifier = Modifier.weight(1f))
                            TableCell(text = "맥박", modifier = Modifier.weight(1f))
                            TableCell(text = "요율", modifier = Modifier.weight(1f))
                            TableCell(text = "제곱", modifier = Modifier.weight(1f))
                            TableCell(text = "신소구로도", modifier = Modifier.weight(1f))
                            TableCell(text = "행당재곽", modifier = Modifier.weight(1f))
                        }

                        // 환자분류
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(
                                text = "환자분류",
                                modifier = Modifier.weight(1f),
                                backgroundColor = Color(0xFFf0f0f0)
                            )
                            TableCell(text = data.patientEvaluation.patientLevel, modifier = Modifier.weight(3f))
                        }

                        // 구급대원 메모
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TableCell(
                                text = "구급대원 + 발생시간:\n병가소견: + 주요소:",
                                modifier = Modifier.fillMaxWidth(),
                                minHeight = 60.dp
                            )
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
            .border(0.5.dp, Color(0xFFcccccc))
    ) {
        // 라벨 셀
        Box(
            modifier = Modifier
                .width(100.dp)
                .background(Color(0xFFf5f5f5))
                .border(0.5.dp, Color(0xFFcccccc))
                .padding(8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
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
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.width(60.dp)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TableCell(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White,
    minHeight: Dp = 32.dp
) {
    Box(
        modifier = modifier
            .background(backgroundColor)
            .border(0.5.dp, Color(0xFFcccccc))
            .defaultMinSize(minHeight = minHeight)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}