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
import com.example.ssairen_app.viewmodel.ActivityViewModel
import com.example.ssairen_app.viewmodel.SummationViewModel
import android.util.Log

@Composable
fun Summation(
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToActivityLog: () -> Unit = {},
    activityViewModel: ActivityViewModel = viewModel(),
    summationViewModel: SummationViewModel = viewModel()
) {
    val globalReportId by ActivityViewModel.globalCurrentReportId.observeAsState()
    val dispatchData by summationViewModel.dispatchData.collectAsState()
    val patientTypeData by summationViewModel.patientTypeData.collectAsState()
    val patientEvaData by summationViewModel.patientEvaData.collectAsState()
    val isLoading by summationViewModel.isLoading.collectAsState()

    var selectedBottomTab by remember { mutableIntStateOf(2) }

    // 데이터 로드
    LaunchedEffect(globalReportId) {
        val reportId = globalReportId
        Log.d("Summation", "========================================")
        Log.d("Summation", "LaunchedEffect 실행됨")
        Log.d("Summation", "globalReportId: $reportId")
        Log.d("Summation", "========================================")

        if (reportId != null && reportId > 0) {
            Log.d("Summation", "📋 요약 데이터 로드 시작: reportId=$reportId")
            summationViewModel.loadSummaryData(reportId)
        } else {
            Log.e("Summation", "❌ globalReportId가 null이거나 0입니다: $reportId")
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
                text = "요약",
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
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF3b7cff)
                )
            } else {
                SummaryTable(
                    dispatchData = dispatchData,
                    patientTypeData = patientTypeData,
                    patientEvaData = patientEvaData
                )
            }
        }

        // 3. 하단 네비게이션
        EmergencyNav(
            selectedTab = selectedBottomTab,
            onTabSelected = {
                selectedBottomTab = it
                when (it) {
                    0 -> onNavigateToHome()
                    1 -> onNavigateToActivityLog()
                    2 -> { /* 현재 화면 유지 */ }
                    3 -> { /* TODO: 메모 */ }
                    4 -> { /* TODO: 병원이송 */ }
                }
            }
        )
    }
}

@Composable
private fun SummaryTable(
    dispatchData: com.example.ssairen_app.data.model.response.DispatchResponseInfo?,
    patientTypeData: com.example.ssairen_app.data.model.response.IncidentTypeData?,
    patientEvaData: com.example.ssairen_app.data.model.response.PatientAssessmentData?
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        color = Color.White
    ) {
        Column {
            // 구급 출동 정보
            if (dispatchData != null) {
                DispatchSection(dispatchData)
            }

            // 환자 발생 유형 정보
            if (patientTypeData != null) {
                PatientTypeSection(patientTypeData)
            }

            // 환자 평가 정보
            if (patientEvaData != null) {
                PatientEvaSection(patientEvaData)
            }

            // 데이터가 없을 경우
            if (dispatchData == null && patientTypeData == null && patientEvaData == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "표시할 데이터가 없습니다.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DispatchSection(data: com.example.ssairen_app.data.model.response.DispatchResponseInfo) {
    // 환자 발생 장소
    TableRow(
        label = "환자 발생 장소",
        content = {
            val locationText = buildString {
                append(data.sceneLocation.name ?: "")
                if (data.sceneLocation.name == "기타" && !data.sceneLocation.value.isNullOrBlank()) {
                    append(" (${data.sceneLocation.value})")
                }
            }
            if (locationText.isNotBlank()) {
                TableCell(text = locationText, modifier = Modifier.fillMaxWidth())
            }
        }
    )

    // 환자 증상
    TableRow(
        label = "환자 증상",
        content = {
            Column {
                // 통증 증상
                data.symptoms.pain?.let { painList ->
                    if (painList.isNotEmpty()) {
                        TableSubRow(
                            label = "통증",
                            value = painList.joinToString(", ") { symptom ->
                                if (symptom.name == "그 밖의 통증" && !symptom.value.isNullOrBlank()) {
                                    "${symptom.name} (${symptom.value})"
                                } else {
                                    symptom.name
                                }
                            }
                        )
                    }
                }

                // 외상 증상
                data.symptoms.trauma?.let { traumaList ->
                    if (traumaList.isNotEmpty()) {
                        TableSubRow(
                            label = "외상",
                            value = traumaList.joinToString(", ") { it.name }
                        )
                    }
                }

                // 그 외 증상
                data.symptoms.otherSymptoms?.let { otherList ->
                    if (otherList.isNotEmpty()) {
                        TableSubRow(
                            label = "그 외 증상",
                            value = otherList.joinToString(", ") { symptom ->
                                if (symptom.name == "기타" && !symptom.value.isNullOrBlank()) {
                                    "${symptom.name} (${symptom.value})"
                                } else {
                                    symptom.name
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun PatientTypeSection(data: com.example.ssairen_app.data.model.response.IncidentTypeData) {
    // 병력
    data.medicalHistory?.let { history ->
        if (history.status == "있음" && !history.items.isNullOrEmpty()) {
            TableRow(
                label = "병력",
                content = {
                    val historyText = history.items.joinToString(", ") { item ->
                        if (item.name == "기타" && !item.value.isNullOrBlank()) {
                            "${item.name} (${item.value})"
                        } else {
                            item.name
                        }
                    }
                    TableCell(text = historyText, modifier = Modifier.fillMaxWidth())
                }
            )
        } else if (history.status == "없음") {
            TableRow(
                label = "병력",
                content = {
                    TableCell(text = "없음", modifier = Modifier.fillMaxWidth())
                }
            )
        }
    }

    // 범죄의심
    data.legalSuspicion?.let { legal ->
        if (!legal.name.isNullOrBlank()) {
            TableRow(
                label = "범죄의심",
                content = {
                    TableCell(text = legal.name, modifier = Modifier.fillMaxWidth())
                }
            )
        }
    }

    // 환자 발생 유형 - 카테고리별 처리
    when (data.category) {
        "질병" -> {
            // 질병은 subCategory_traffic, injury, nonTrauma에 없을 것으로 예상
            // categoryOther 사용
            if (!data.categoryOther.isNullOrBlank()) {
                TableRow(
                    label = "질병",
                    content = {
                        TableCell(text = data.categoryOther, modifier = Modifier.fillMaxWidth())
                    }
                )
            }
        }
        "질병외" -> {
            // 교통사고
            data.subCategoryTraffic?.let { traffic ->
                TableRow(
                    label = "질병 외",
                    content = {
                        Column {
                            TableSubRow(label = "구분", value = traffic.type ?: "")
                            val victimText = buildString {
                                append(traffic.name ?: "")
                                if (!traffic.value.isNullOrBlank()) {
                                    append(" (${traffic.value})")
                                }
                            }
                            if (victimText.isNotBlank()) {
                                TableSubRow(label = "피해자 유형", value = victimText)
                            }
                        }
                    }
                )
            }

            // 그 외 손상
            data.subCategoryInjury?.let { injury ->
                TableRow(
                    label = "질병 외",
                    content = {
                        Column {
                            TableSubRow(label = "구분", value = injury.type ?: "")
                            if (!injury.name.isNullOrBlank()) {
                                TableSubRow(label = "유형", value = injury.name)
                            }
                        }
                    }
                )
            }

            // 비외상성 손상
            data.subCategoryNonTrauma?.let { nonTrauma ->
                TableRow(
                    label = "질병 외",
                    content = {
                        Column {
                            TableSubRow(label = "구분", value = nonTrauma.type ?: "")
                            val typeText = buildString {
                                append(nonTrauma.name ?: "")
                                if (nonTrauma.name == "기타" && !nonTrauma.value.isNullOrBlank()) {
                                    append(" (${nonTrauma.value})")
                                }
                            }
                            if (typeText.isNotBlank()) {
                                TableSubRow(label = "유형", value = typeText)
                            }
                        }
                    }
                )
            }
        }
        "기타" -> {
            data.subCategoryOther?.let { other ->
                TableRow(
                    label = "기타",
                    content = {
                        val text = buildString {
                            append(other.name ?: "")
                            if (other.name == "기타" && !other.value.isNullOrBlank()) {
                                append(" (${other.value})")
                            }
                        }
                        TableCell(text = text, modifier = Modifier.fillMaxWidth())
                    }
                )
            }
        }
    }
}

@Composable
private fun PatientEvaSection(data: com.example.ssairen_app.data.model.response.PatientAssessmentData) {
    TableRow(
        label = "환자 평가",
        content = {
            Column {
                // 의식 상태
                data.consciousness?.let { consciousness ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TableCell(
                            text = "의식 상태",
                            modifier = Modifier.weight(1f),
                            backgroundColor = Color(0xFFf0f0f0)
                        )
                        consciousness.first?.let { first ->
                            TableCell(text = "1차", modifier = Modifier.weight(1f))
                            TableCell(text = first.time ?: "", modifier = Modifier.weight(1f))
                            TableCell(
                                text = if (first.state == "A") "●" else "",
                                modifier = Modifier.weight(0.5f)
                            )
                            TableCell(
                                text = if (first.state == "V") "●" else "",
                                modifier = Modifier.weight(0.5f)
                            )
                            TableCell(
                                text = if (first.state == "P") "●" else "",
                                modifier = Modifier.weight(0.5f)
                            )
                            TableCell(
                                text = if (first.state == "U") "●" else "",
                                modifier = Modifier.weight(0.5f)
                            )
                        }
                    }

                    consciousness.second?.let { second ->
                        if (!second.state.isNullOrBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Spacer(modifier = Modifier.weight(1f))
                                TableCell(text = "2차", modifier = Modifier.weight(1f))
                                TableCell(text = second.time ?: "", modifier = Modifier.weight(1f))
                                TableCell(
                                    text = if (second.state == "A") "●" else "",
                                    modifier = Modifier.weight(0.5f)
                                )
                                TableCell(
                                    text = if (second.state == "V") "●" else "",
                                    modifier = Modifier.weight(0.5f)
                                )
                                TableCell(
                                    text = if (second.state == "P") "●" else "",
                                    modifier = Modifier.weight(0.5f)
                                )
                                TableCell(
                                    text = if (second.state == "U") "●" else "",
                                    modifier = Modifier.weight(0.5f)
                                )
                            }
                        }
                    }
                }

                // 동공 반응
                data.pupilReaction?.let { pupil ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TableCell(
                            text = "동공 반응",
                            modifier = Modifier.weight(1f),
                            backgroundColor = Color(0xFFf0f0f0)
                        )
                        pupil.left?.let { left ->
                            TableCell(text = "좌", modifier = Modifier.weight(1f))
                            TableCell(text = left.status ?: "", modifier = Modifier.weight(1f))
                            TableCell(text = left.reaction ?: "", modifier = Modifier.weight(1f))
                        }
                    }

                    pupil.right?.let { right ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))
                            TableCell(text = "우", modifier = Modifier.weight(1f))
                            TableCell(text = right.status ?: "", modifier = Modifier.weight(1f))
                            TableCell(text = right.reaction ?: "", modifier = Modifier.weight(1f))
                        }
                    }
                }

                // 활력 징후
                data.vitalSigns?.let { vitalSigns ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TableCell(
                            text = "활력 징후",
                            modifier = Modifier.weight(1f),
                            backgroundColor = Color(0xFFf0f0f0)
                        )
                        TableCell(text = "시각", modifier = Modifier.weight(1f))
                        TableCell(text = "혈압", modifier = Modifier.weight(1f))
                        TableCell(text = "맥박", modifier = Modifier.weight(1f))
                        TableCell(text = "호흡", modifier = Modifier.weight(1f))
                        TableCell(text = "체온", modifier = Modifier.weight(1f))
                        TableCell(text = "산소포화도", modifier = Modifier.weight(1f))
                        TableCell(text = "혈당", modifier = Modifier.weight(1f))
                    }

                    vitalSigns.first?.let { first ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(text = "1차", modifier = Modifier.weight(1f))
                            TableCell(text = first.time ?: "", modifier = Modifier.weight(1f))
                            TableCell(text = first.bloodPressure ?: "", modifier = Modifier.weight(1f))
                            TableCell(text = first.pulse?.toString() ?: "", modifier = Modifier.weight(1f))
                            TableCell(text = first.respiration?.toString() ?: "", modifier = Modifier.weight(1f))
                            TableCell(text = first.temperature?.toString() ?: "", modifier = Modifier.weight(1f))
                            TableCell(text = first.spo2?.toString() ?: "", modifier = Modifier.weight(1f))
                            TableCell(text = first.bloodSugar?.toString() ?: "", modifier = Modifier.weight(1f))
                        }
                    }

                    vitalSigns.second?.let { second ->
                        if (second.pulse != null || second.bloodPressure?.isNotBlank() == true) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                TableCell(text = "2차", modifier = Modifier.weight(1f))
                                TableCell(text = second.time ?: "", modifier = Modifier.weight(1f))
                                TableCell(text = second.bloodPressure ?: "", modifier = Modifier.weight(1f))
                                TableCell(text = second.pulse?.toString() ?: "", modifier = Modifier.weight(1f))
                                TableCell(text = second.respiration?.toString() ?: "", modifier = Modifier.weight(1f))
                                TableCell(text = second.temperature?.toString() ?: "", modifier = Modifier.weight(1f))
                                TableCell(text = second.spo2?.toString() ?: "", modifier = Modifier.weight(1f))
                                TableCell(text = second.bloodSugar?.toString() ?: "", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // 환자 분류
                data.patientLevel?.let { level ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TableCell(
                            text = "환자 분류",
                            modifier = Modifier.weight(1f),
                            backgroundColor = Color(0xFFf0f0f0)
                        )
                        TableCell(text = level, modifier = Modifier.weight(3f))
                    }
                }

                // 특이사항
                data.notes?.let { notes ->
                    if (!notes.note.isNullOrBlank() || !notes.onset.isNullOrBlank() || !notes.cheifComplaint.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TableCell(
                                text = buildString {
                                    if (!notes.onset.isNullOrBlank()) {
                                        append("발생 시각: ${notes.onset}\n")
                                    }
                                    if (!notes.cheifComplaint.isNullOrBlank()) {
                                        append("주 호소: ${notes.cheifComplaint}\n")
                                    }
                                    if (!notes.note.isNullOrBlank()) {
                                        append("메모: ${notes.note}")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                minHeight = 60.dp
                            )
                        }
                    }
                }
            }
        }
    )
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
