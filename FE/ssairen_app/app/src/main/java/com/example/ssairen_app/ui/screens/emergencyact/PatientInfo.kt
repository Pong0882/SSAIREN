// PatientInfo.kt
package com.example.ssairen_app.ui.screens.emergencyact

import android.util.Log
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
import com.example.ssairen_app.ui.components.MainButton
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
    activityViewModel: ActivityViewModel = viewModel()  // ✅ ActivityViewModel 추가
) {
    // ✅ API 상태 관찰, STT 데이터 상태 관찰 추가
    val patientInfoState by activityViewModel.patientInfoState.observeAsState(PatientInfoApiState.Idle)
    val currentReportId by activityViewModel.currentEmergencyReportId.observeAsState()

    // ✅ API 호출 (currentReportId가 설정되면 자동 실행)
    LaunchedEffect(currentReportId) {
        currentReportId?.let { reportId ->
            Log.d("PatientInfo", "📞 API 호출: getPatientInfo($reportId)")
            activityViewModel.getPatientInfo(reportId)
        }
    }

    // ✅ State 변수들 (data.patientInfo로 초기화)
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

    // ✅ 자동 저장 함수 (LogViewModel에 임시 저장)
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

    // ✅ API 응답 처리
    LaunchedEffect(patientInfoState) {
        Log.d("PatientInfo", "🟢 patientInfoState 변경: $patientInfoState")

        when (val state = patientInfoState) {
            is PatientInfoApiState.Success -> {
                Log.d("PatientInfo", "✅ API 성공 - 데이터 매핑 시작")
                val apiData = state.patientInfoResponse.data.data.patientInfo

                // 신고자 정보 매핑
                apiData.reporter?.let { reporter ->
                    reporterPhone = reporter.phone ?: ""
                    selectedReportMethod = reporter.reportMethod ?: ""
                    Log.d("PatientInfo", "신고자: phone=$reporterPhone, method=$selectedReportMethod")
                }

                // 환자 정보 매핑
                apiData.patient?.let { patient ->
                    patientName = patient.name ?: ""
                    selectedGender = patient.gender ?: ""
                    patientAge = patient.ageYears?.toString() ?: ""
                    patientAddress = patient.address ?: ""

                    Log.d("PatientInfo", "환자: name=$patientName, gender=$selectedGender, age=$patientAge")
                    Log.d("PatientInfo", "주소: $patientAddress")

                    // 생년월일 파싱 (YYYY-MM-DD)
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

                // 보호자 정보 매핑
                apiData.guardian?.let { guardian ->
                    guardianName = guardian.name ?: ""
                    guardianRelation = guardian.relation ?: ""
                    guardianPhone = guardian.phone ?: ""
                    Log.d("PatientInfo", "보호자: name=$guardianName, relation=$guardianRelation, phone=$guardianPhone")
                }

                Log.d("PatientInfo", "✅ 데이터 매핑 완료")

                // ✅ LogViewModel에 동기화 (덮어쓰기 버그 방지)
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


    // ✅ 로딩 중일 때 표시
    if (patientInfoState is PatientInfoApiState.Loading) {
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
            .padding(horizontal = 16.dp)
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 헤더
        Text(
            text = "세부항목-환자정보",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // 환자정보 입력 폼 카드
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2a2a2a)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 신고자 전화번호 + 신고방법
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UnderlineInputField(
                        label = "신고자 전화번호",
                        value = reporterPhone,
                        onValueChange = {
                            reporterPhone = it
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )

                    // 신고방법
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "신고방법",
                            color = Color(0xFF999999),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MainButton(
                                onClick = {
                                    if (!isReadOnly) {
                                        selectedReportMethod = "휴대전화"
                                        saveData()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (selectedReportMethod == "휴대전화")
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = "일반전화", fontSize = 12.sp)
                            }
                            MainButton(
                                onClick = {
                                    if (!isReadOnly) {
                                        selectedReportMethod = "유선전화"
                                        saveData()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (selectedReportMethod == "유선전화")
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = "유선전화", fontSize = 12.sp)
                            }
                            MainButton(
                                onClick = {
                                    if (!isReadOnly) {
                                        selectedReportMethod = "기타"
                                        saveData()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (selectedReportMethod == "기타")
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = "기타", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // 환자 성명 + 성별
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    UnderlineInputField(
                        label = "환자 성명",
                        value = patientName,
                        onValueChange = {
                            patientName = it
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )

                    // 성별
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "환자 성별",
                            color = Color(0xFF999999),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MainButton(
                                onClick = {
                                    if (!isReadOnly) {
                                        selectedGender = "남성"
                                        saveData()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (selectedGender == "남성")
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = "남성", fontSize = 13.sp)
                            }
                            MainButton(
                                onClick = {
                                    if (!isReadOnly) {
                                        selectedGender = "여성"
                                        saveData()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (selectedGender == "여성")
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = "여성", fontSize = 13.sp)
                            }
                        }
                    }
                }

                // 생년월일 + 나이
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 생년월일
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "생년월일",
                            color = Color(0xFF999999),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
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

                    // 나이
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "나이",
                            color = Color(0xFF999999),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BasicTextField(
                                value = patientAge,
                                onValueChange = {
                                    patientAge = it
                                    saveData()
                                },
                                modifier = Modifier.padding(bottom = 4.dp),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.End
                                ),
                                singleLine = true,
                                readOnly = isReadOnly
                            )
                            Text(
                                text = "세",
                                color = Color.White,
                                fontSize = 15.sp,
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

                HorizontalDivider(
                    color = Color(0xFF4a4a4a),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
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

                // 보호자 연락처
                UnderlineInputField(
                    label = "보호자 연락처",
                    value = guardianPhone,
                    onValueChange = {
                        guardianPhone = it
                        saveData()
                    },
                    enabled = !isReadOnly
                )
            }
        }
    }
}

// ==========================================
// 보조 컴포넌트들
// ==========================================

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
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.padding(bottom = 4.dp),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.End
                ),
                singleLine = true,
                readOnly = !enabled
            )
            Text(
                text = label,
                color = Color.White,
                fontSize = 15.sp,
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
    textAlign: TextAlign = TextAlign.End,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            color = Color(0xFF999999),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
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