// PatientInfo.kt
package com.example.ssairen_app.ui.screens.emergencyact

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.viewmodel.ActivityLogData
import com.example.ssairen_app.viewmodel.ActivityViewModel
import com.example.ssairen_app.viewmodel.LogViewModel
import com.example.ssairen_app.viewmodel.PatientInfoApiState
import com.example.ssairen_app.viewmodel.PatientInfoData

/**
 * 환자정보 화면
 *
 * 📌 용도:
 * 1. 새 일지 작성 - data.patientInfo가 빈 값
 * 2. 기존 보고서 조회/수정 - ActivityViewModel로 GET API 호출하여 데이터 로드
 *
 * 🔄 동작 방식:
 * - 화면 진입 → ActivityViewModel.getPatientInfo() → API 호출 → 화면에 표시
 * - 입력/수정 → saveData() → LogViewModel에 임시 저장 (메모리)
 * - 탭 변경 → ActivityLogHome의 saveToBackend() → PATCH API 호출 (DB 저장)
 */
@Composable
fun PatientInfo(
    viewModel: LogViewModel,
    data: ActivityLogData,
    isReadOnly: Boolean = false,
    activityViewModel: ActivityViewModel = viewModel()
) {
    val patientInfoState by activityViewModel.patientInfoState.observeAsState(PatientInfoApiState.Idle)
    val currentReportId by activityViewModel.currentEmergencyReportId.observeAsState()

    var isInitialLoad by remember { mutableStateOf(true) }

    LaunchedEffect(currentReportId) {
        currentReportId?.let { reportId ->
            while (true) {
                Log.d("PatientInfo", "📞 자동 API 호출: getPatientInfo($reportId)")
                activityViewModel.getPatientInfo(reportId)
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    var reporterPhone by remember { mutableStateOf(data.patientInfo.reporterPhone) }
    var selectedReportMethod by remember { mutableStateOf(data.patientInfo.reportMethod) }
    var patientName by remember { mutableStateOf(data.patientInfo.patientName) }
    var selectedGender by remember { mutableStateOf(data.patientInfo.patientGender) }
    var birthYear by remember { mutableStateOf(data.patientInfo.birthYear) }
    var birthMonth by remember { mutableStateOf(data.patientInfo.birthMonth) }
    var birthDay by remember { mutableStateOf(data.patientInfo.birthDay) }
    var patientAge by remember { mutableStateOf(data.patientInfo.patientAge) }
    var patientAddress by remember { mutableStateOf(data.patientInfo.patientAddress) }
    var guardianName by remember { mutableStateOf(data.patientInfo.guardianName) }
    var guardianRelation by remember { mutableStateOf(data.patientInfo.guardianRelation) }
    var guardianPhone by remember { mutableStateOf(data.patientInfo.guardianPhone) }

    fun saveData() {
        val patientInfoData = PatientInfoData(
            reporterPhone = reporterPhone,
            reportMethod = selectedReportMethod,
            patientName = patientName,
            patientGender = selectedGender,
            birthYear = birthYear,
            birthMonth = birthMonth,
            birthDay = birthDay,
            patientAge = patientAge,
            patientAddress = patientAddress,
            guardianName = guardianName,
            guardianRelation = guardianRelation,
            guardianPhone = guardianPhone
        )
        viewModel.updatePatientInfo(patientInfoData)
    }

    LaunchedEffect(patientInfoState) {
        Log.d("PatientInfo", "🟢 patientInfoState 변경: $patientInfoState")

        when (val state = patientInfoState) {
            is PatientInfoApiState.Success -> {
                Log.d("PatientInfo", "✅ API 성공 - 데이터 매핑 시작")
                isInitialLoad = false
                val apiData = state.patientInfoResponse.data.data.patientInfo

                apiData.reporter?.let { reporter ->
                    reporterPhone = reporter.phone ?: ""
                    selectedReportMethod = reporter.reportMethod ?: ""
                    Log.d("PatientInfo", "신고자: phone=$reporterPhone, method=$selectedReportMethod")
                }

                apiData.patient?.let { patient ->
                    patientName = patient.name ?: ""
                    selectedGender = patient.gender ?: ""
                    patientAge = patient.ageYears?.toString() ?: ""
                    patientAddress = patient.address ?: ""

                    Log.d("PatientInfo", "환자: name=$patientName, gender=$selectedGender, age=$patientAge")
                    Log.d("PatientInfo", "주소: $patientAddress")

                    patient.birthDate?.let { birthDate ->
                        val parts = birthDate.split("-")
                        if (parts.size == 3) {
                            birthYear = parts[0]
                            birthMonth = parts[1]
                            birthDay = parts[2]
                            Log.d("PatientInfo", "생년월일: $birthYear-$birthMonth-$birthDay")
                        }
                    }
                }

                apiData.guardian?.let { guardian ->
                    guardianName = guardian.name ?: ""
                    guardianRelation = guardian.relation ?: ""
                    guardianPhone = guardian.phone ?: ""
                    Log.d("PatientInfo", "보호자: name=$guardianName, relation=$guardianRelation, phone=$guardianPhone")
                }

                Log.d("PatientInfo", "✅ 데이터 매핑 완료")
                saveData()
                Log.d("PatientInfo", "💾 LogViewModel 동기화 완료")
            }
            is PatientInfoApiState.Error -> {
                Log.e("PatientInfo", "❌ API 오류: ${state.message}")
            }
            is PatientInfoApiState.Loading -> {
                Log.d("PatientInfo", "⏳ 로딩 중...")
            }
            else -> {
                Log.d("PatientInfo", "⚪ Idle 상태")
            }
        }
    }

    if (isInitialLoad && patientInfoState is PatientInfoApiState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1a1a1a)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF3b7cff))
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 40.dp)  // ✅ 16.dp → 40.dp
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ✅ 신고자 전화번호 + 신고방법 (라벨 정렬)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 라벨 Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "신고자 전화번호",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "신고방법",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

// ✅ 신고자 전화번호 부분만 수정
// 입력 필드 Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 신고자 전화번호 입력
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),  // ✅ 추가
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        BasicTextField(
                            value = reporterPhone,
                            onValueChange = {
                                reporterPhone = it
                                saveData()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start
                            ),
                            singleLine = true,
                            readOnly = isReadOnly,
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (reporterPhone.isEmpty()) {
                                        Text(
                                            text = "",
                                            color = Color(0xFF666666),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        HorizontalDivider(
                            color = Color(0xFF4a4a4a),
                            thickness = 1.dp
                        )
                    }

                    // 신고방법 버튼
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SelectButton(
                            text = "일반전화",
                            isSelected = selectedReportMethod == "휴대전화",
                            onClick = {
                                if (!isReadOnly) {
                                    selectedReportMethod = "휴대전화"
                                    saveData()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SelectButton(
                            text = "유선전화",
                            isSelected = selectedReportMethod == "유선전화",
                            onClick = {
                                if (!isReadOnly) {
                                    selectedReportMethod = "유선전화"
                                    saveData()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SelectButton(
                            text = "기타",
                            isSelected = selectedReportMethod == "기타",
                            onClick = {
                                if (!isReadOnly) {
                                    selectedReportMethod = "기타"
                                    saveData()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ✅ 환자 성명 + 성별 (라벨 정렬)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 라벨 Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "환자 성명",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "환자 성별",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                // ✅ 환자 성명 부분만 수정
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 환자 성명 입력
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        BasicTextField(
                            value = patientName,
                            onValueChange = {
                                patientName = it
                                saveData()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start
                            ),
                            singleLine = true,
                            readOnly = isReadOnly,
                            decorationBox = { innerTextField ->
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (patientName.isEmpty()) {
                                        Text(
                                            text = "",
                                            color = Color(0xFF666666),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        HorizontalDivider(
                            color = Color(0xFF4a4a4a),
                            thickness = 1.dp
                        )
                    }

                    // 성별 버튼
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SelectButton(
                            text = "남성",
                            isSelected = selectedGender == "남성",
                            onClick = {
                                if (!isReadOnly) {
                                    selectedGender = "남성"
                                    saveData()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        SelectButton(
                            text = "여성",
                            isSelected = selectedGender == "여성",
                            onClick = {
                                if (!isReadOnly) {
                                    selectedGender = "여성"
                                    saveData()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 생년월일 + 나이
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "생년월일",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BirthDateField(
                            value = birthYear,
                            onValueChange = { birthYear = it; saveData() },
                            label = "년",
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                        BirthDateField(
                            value = birthMonth,
                            onValueChange = { birthMonth = it; saveData() },
                            label = "월",
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                        BirthDateField(
                            value = birthDay,
                            onValueChange = { birthDay = it; saveData() },
                            label = "일",
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "나이",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = patientAge,
                            onValueChange = {
                                patientAge = it
                                saveData()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 4.dp),
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Start
                            ),
                            singleLine = true,
                            readOnly = isReadOnly
                        )
                        Text(
                            text = "세",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }
                    HorizontalDivider(
                        color = Color(0xFF4a4a4a),
                        thickness = 1.dp
                    )
                }
            }

            // 환자주소
            UnderlineInputField(
                label = "환자주소",
                value = patientAddress,
                onValueChange = {
                    patientAddress = it
                    saveData()
                },
                enabled = !isReadOnly
            )

            // 보호자 성명 + 관계
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                UnderlineInputField(
                    label = "보호자 성명",
                    value = guardianName,
                    onValueChange = {
                        guardianName = it
                        saveData()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isReadOnly
                )
                UnderlineInputField(
                    label = "보호자 관계",
                    value = guardianRelation,
                    onValueChange = {
                        guardianRelation = it
                        saveData()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isReadOnly
                )
            }

            // 보호자 연락처 (반만 차지)
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                UnderlineInputField(
                    label = "보호자 연락처",
                    value = guardianPhone,
                    onValueChange = {
                        guardianPhone = it
                        saveData()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isReadOnly
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// ==========================================
// 보조 컴포넌트들
// ==========================================

@Composable
private fun SelectButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF3b7cff) else Color(0xFF3a3a3a),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF4a4a4a))
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun BirthDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 4.dp),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Start
                ),
                singleLine = true,
                readOnly = !enabled
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }
        HorizontalDivider(
            color = Color(0xFF4a4a4a),
            thickness = 1.dp
        )
    }
}

@Composable
private fun UnderlineInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                textAlign = textAlign
            ),
            singleLine = true,
            readOnly = !enabled,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isEmpty()) {
                        Text(
                            text = "",
                            color = Color(0xFF666666),
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            }
        )

        HorizontalDivider(
            color = Color(0xFF4a4a4a),
            thickness = 1.dp
        )
    }
}