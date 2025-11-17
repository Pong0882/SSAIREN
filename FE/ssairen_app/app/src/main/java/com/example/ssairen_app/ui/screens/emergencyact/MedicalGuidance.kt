package com.example.ssairen_app.ui.screens.emergencyact

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ssairen_app.viewmodel.LogViewModel
import com.example.ssairen_app.viewmodel.ActivityLogData
import com.example.ssairen_app.viewmodel.ActivityViewModel
import com.example.ssairen_app.viewmodel.MedicalGuidanceApiState
import com.example.ssairen_app.viewmodel.MedicalGuidanceData

/**
 * 의료지도 섹션 메인 화면
 *
 * @param viewModel LogViewModel
 * @param data ActivityLogData
 * @param isReadOnly 읽기 전용 모드
 * @param activityViewModel ActivityViewModel (API 호출용)
 */
@Composable
fun MedicalGuidance(
    viewModel: LogViewModel,
    data: ActivityLogData,
    isReadOnly: Boolean = false,
    activityViewModel: ActivityViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val guidanceData = data.medicalGuidance

    // API 상태 관찰
    val medicalGuidanceState by activityViewModel.medicalGuidanceState.observeAsState(MedicalGuidanceApiState.Idle)
    val currentReportId by activityViewModel.currentEmergencyReportId.observeAsState()

    // API 호출 (currentReportId가 설정되면 자동 실행)
    LaunchedEffect(currentReportId) {
        currentReportId?.let { reportId ->
            Log.d("MedicalGuidance", "📞 API 호출: getMedicalGuidance($reportId)")
            activityViewModel.getMedicalGuidance(reportId)
        }
    }

    // 로컬 UI 상태 - ViewModel에서 가져온 값으로 초기화
    var selectedConnection by remember { mutableStateOf(guidanceData.contactStatus) }
    var requestTime by remember { mutableStateOf(guidanceData.requestTime) }
    var selectedRequestMethod by remember { mutableStateOf(guidanceData.requestMethod) }
    var requestMethodOtherValue by remember { mutableStateOf(guidanceData.requestMethodValue ?: "") }
    var selectedInstitution by remember { mutableStateOf(guidanceData.guidanceAgency) }
    var institutionOtherValue by remember { mutableStateOf(guidanceData.guidanceAgencyValue ?: "") }
    var doctorName by remember { mutableStateOf(guidanceData.guidanceDoctor) }
    var selectedEmergencyCare by remember { mutableStateOf(guidanceData.emergencyTreatment) }
    var emergencyCareOtherValue by remember { mutableStateOf(guidanceData.emergencyTreatmentOtherValue ?: "") }
    var selectedMedication by remember { mutableStateOf(guidanceData.medication) }
    var medicationOtherValue by remember { mutableStateOf(guidanceData.medicationOtherValue ?: "") }

    // boolean 필드들을 Set으로 변환 (UI 표시용 - 다중 선택)
    var selectedHospitalSelections by remember {
        mutableStateOf(
            buildSet {
                if (guidanceData.hospitalRequest) add("병원선정")
                if (guidanceData.patientEvaluation) add("환자평가")
                if (guidanceData.cprTransfer) add("CPR유보중단")
                if (guidanceData.transferRefusal) add("이송거절")
                if (guidanceData.transferRejection) add("이송거부")
            }
        )
    }

    // API 응답 처리
    LaunchedEffect(medicalGuidanceState) {
        Log.d("MedicalGuidance", "🟢 medicalGuidanceState 변경: $medicalGuidanceState")

        when (val state = medicalGuidanceState) {
            is MedicalGuidanceApiState.Success -> {
                Log.d("MedicalGuidance", "✅ API 성공 - 데이터 매핑 시작")
                val apiData = state.medicalGuidanceResponse.data.data.medicalGuidance

                // null 안전 처리: null이면 빈 문자열
                selectedConnection = apiData.contactStatus ?: ""
                requestTime = apiData.requestTime ?: ""
                selectedRequestMethod = apiData.requestMethod.type ?: ""
                requestMethodOtherValue = apiData.requestMethod.value ?: ""
                selectedInstitution = apiData.guidanceAgency.type ?: ""
                institutionOtherValue = apiData.guidanceAgency.value ?: ""
                doctorName = apiData.guidanceDoctor.name ?: ""

                // 응급처치/약물투여 리스트 변환 (null 안전 처리)
                selectedEmergencyCare = apiData.guidanceContent.emergencyTreatment?.map { it.name }?.toSet() ?: emptySet()
                emergencyCareOtherValue = apiData.guidanceContent.emergencyTreatment?.find { it.name == "기타" }?.value ?: ""
                selectedMedication = apiData.guidanceContent.medication?.map { it.name }?.toSet() ?: emptySet()
                medicationOtherValue = apiData.guidanceContent.medication?.find { it.name == "기타" }?.value ?: ""

                // boolean 필드들 변환 (null 안전 처리)
                selectedHospitalSelections = buildSet {
                    if (apiData.guidanceContent.hospitalRequest == true) add("병원선정")
                    if (apiData.guidanceContent.patientEvaluation == true) add("환자평가")
                    if (apiData.guidanceContent.cprTransfer == true) add("CPR유보중단")
                    if (apiData.guidanceContent.transferRefusal == true) add("이송거절")
                    if (apiData.guidanceContent.transferRejection == true) add("이송거부")
                }

                Log.d("MedicalGuidance", "✅ 데이터 매핑 완료")

                // ✅ LogViewModel에 동기화 (덮어쓰기 버그 방지)
                viewModel.updateMedicalGuidance(
                    MedicalGuidanceData(
                        contactStatus = selectedConnection,
                        requestTime = requestTime,
                        requestMethod = selectedRequestMethod,
                        requestMethodValue = if (selectedRequestMethod == "기타") requestMethodOtherValue else null,
                        guidanceAgency = selectedInstitution,
                        guidanceAgencyValue = if (selectedInstitution == "기타") institutionOtherValue else null,
                        guidanceDoctor = doctorName,
                        emergencyTreatment = selectedEmergencyCare,
                        emergencyTreatmentOtherValue = if (selectedEmergencyCare.contains("기타")) emergencyCareOtherValue else null,
                        medication = selectedMedication,
                        medicationOtherValue = if (selectedMedication.contains("기타")) medicationOtherValue else null,
                        hospitalRequest = selectedHospitalSelections.contains("병원선정"),
                        patientEvaluation = selectedHospitalSelections.contains("환자평가"),
                        cprTransfer = selectedHospitalSelections.contains("CPR유보중단"),
                        transferRefusal = selectedHospitalSelections.contains("이송거절"),
                        transferRejection = selectedHospitalSelections.contains("이송거부")
                    )
                )
                Log.d("MedicalGuidance", "💾 LogViewModel 동기화 완료")
            }
            is MedicalGuidanceApiState.Error -> {
                Log.e("MedicalGuidance", "❌ API 오류: ${state.message}")
            }
            is MedicalGuidanceApiState.Loading -> {
                Log.d("MedicalGuidance", "⏳ 로딩 중...")
            }
            else -> {
                Log.d("MedicalGuidance", "⚪ Idle 상태")
            }
        }
    }

    // ViewModel 데이터가 변경되면 UI 상태 업데이트
    LaunchedEffect(guidanceData) {
        selectedConnection = guidanceData.contactStatus
        requestTime = guidanceData.requestTime
        selectedRequestMethod = guidanceData.requestMethod
        requestMethodOtherValue = guidanceData.requestMethodValue ?: ""
        selectedInstitution = guidanceData.guidanceAgency
        institutionOtherValue = guidanceData.guidanceAgencyValue ?: ""
        doctorName = guidanceData.guidanceDoctor
        selectedEmergencyCare = guidanceData.emergencyTreatment
        emergencyCareOtherValue = guidanceData.emergencyTreatmentOtherValue ?: ""
        selectedMedication = guidanceData.medication
        medicationOtherValue = guidanceData.medicationOtherValue ?: ""

        selectedHospitalSelections = buildSet {
            if (guidanceData.hospitalRequest) add("병원선정")
            if (guidanceData.patientEvaluation) add("환자평가")
            if (guidanceData.cprTransfer) add("CPR유보중단")
            if (guidanceData.transferRefusal) add("이송거절")
            if (guidanceData.transferRejection) add("이송거부")
        }
    }

    // 값이 변경될 때마다 ViewModel 업데이트 (읽기 전용이 아닐 때만)
    LaunchedEffect(
        selectedConnection, requestTime, selectedRequestMethod, requestMethodOtherValue,
        selectedInstitution, institutionOtherValue, doctorName,
        selectedEmergencyCare, emergencyCareOtherValue,
        selectedMedication, medicationOtherValue, selectedHospitalSelections
    ) {
        if (!isReadOnly) {
            viewModel.updateMedicalGuidance(
                MedicalGuidanceData(
                    contactStatus = selectedConnection,
                    requestTime = requestTime,
                    requestMethod = selectedRequestMethod,
                    requestMethodValue = if (selectedRequestMethod == "기타") requestMethodOtherValue else null,
                    guidanceAgency = selectedInstitution,
                    guidanceAgencyValue = if (selectedInstitution == "기타") institutionOtherValue else null,
                    guidanceDoctor = doctorName,
                    emergencyTreatment = selectedEmergencyCare,
                    emergencyTreatmentOtherValue = if (selectedEmergencyCare.contains("기타")) emergencyCareOtherValue else null,
                    medication = selectedMedication,
                    medicationOtherValue = if (selectedMedication.contains("기타")) medicationOtherValue else null,
                    hospitalRequest = selectedHospitalSelections.contains("병원선정"),
                    patientEvaluation = selectedHospitalSelections.contains("환자평가"),
                    cprTransfer = selectedHospitalSelections.contains("CPR유보중단"),
                    transferRefusal = selectedHospitalSelections.contains("이송거절"),
                    transferRejection = selectedHospitalSelections.contains("이송거부")
                )
            )
        }
    }

    // 로딩 중일 때 표시
    if (medicalGuidanceState is MedicalGuidanceApiState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF3b7cff))
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ==== 1. 의료지도 + 요청시각 (가로 배치) ====
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 의료지도 (연결/미연결)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "의료지도",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    SingleSelectButtonGroup(
                        options = listOf("연결", "미연결"),
                        selectedOption = selectedConnection,
                        onOptionSelected = { selectedConnection = it },
                        columns = 2,
                        enabled = !isReadOnly
                    )
                }

                // 요청시각
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "요청시각",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    TextField(
                        value = requestTime,
                        onValueChange = { requestTime = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        enabled = !isReadOnly,
                        placeholder = {
                            Text(
                                text = "00:00",
                                color = Color(0xFF999999),
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color(0xFF666666),
                            focusedIndicatorColor = Color(0xFF3a3a3a),
                            unfocusedIndicatorColor = Color(0xFF3a3a3a),
                            disabledIndicatorColor = Color(0xFF3a3a3a),
                            cursorColor = Color(0xFF3b7cff)
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = Color.White
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // ==== 2. 요청 방법 ====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "요청 방법",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                SingleSelectButtonGroup(
                    options = listOf("일반전화", "휴대전화음성", "휴대전화화상", "무전기", "기타"),
                    selectedOption = selectedRequestMethod,
                    onOptionSelected = { selectedRequestMethod = it },
                    columns = 5,
                    enabled = !isReadOnly
                )

                // "기타" 선택 시 입력 필드 표시
                if (selectedRequestMethod == "기타") {
                    TextField(
                        value = requestMethodOtherValue,
                        onValueChange = { requestMethodOtherValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        enabled = !isReadOnly,
                        placeholder = {
                            Text(
                                text = "기타 요청 방법을 입력하세요",
                                color = Color(0xFF999999),
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color(0xFF666666),
                            focusedIndicatorColor = Color(0xFF3a3a3a),
                            unfocusedIndicatorColor = Color(0xFF3a3a3a),
                            disabledIndicatorColor = Color(0xFF3a3a3a),
                            cursorColor = Color(0xFF3b7cff)
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = Color.White
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // ==== 3. 의료지도 기관 + 의료지도 의사 성명 (가로 배치) ====
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // 의료지도 기관
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "의료지도 기관",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    SingleSelectButtonGroup(
                        options = listOf("소방", "병원", "기타"),
                        selectedOption = selectedInstitution,
                        onOptionSelected = { selectedInstitution = it },
                        columns = 3,
                        enabled = !isReadOnly
                    )
                }

                // 의료지도 의사 성명
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "의료지도 의사 성명",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    TextField(
                        value = doctorName,
                        onValueChange = { doctorName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        enabled = !isReadOnly,
                        placeholder = {
                            Text(
                                text = "성명 서술",
                                color = Color(0xFF999999),
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color(0xFF666666),
                            focusedIndicatorColor = Color(0xFF3a3a3a),
                            unfocusedIndicatorColor = Color(0xFF3a3a3a),
                            disabledIndicatorColor = Color(0xFF3a3a3a),
                            cursorColor = Color(0xFF3b7cff)
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = Color.White
                        ),
                        singleLine = true
                    )
                }
            }
        }

        // ==== 의료지도 기관 "기타" 입력 필드 (선택 시에만 표시) ====
        if (selectedInstitution == "기타") {
            item {
                TextField(
                    value = institutionOtherValue,
                    onValueChange = { institutionOtherValue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    enabled = !isReadOnly,
                    placeholder = {
                        Text(
                            text = "기타 기관을 입력하세요",
                            color = Color(0xFF999999),
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color(0xFF666666),
                        focusedIndicatorColor = Color(0xFF3a3a3a),
                        unfocusedIndicatorColor = Color(0xFF3a3a3a),
                        disabledIndicatorColor = Color(0xFF3a3a3a),
                        cursorColor = Color(0xFF3b7cff)
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = Color.White
                    ),
                    singleLine = true
                )
            }
        }

        // ==== 4. 의료지도 내용 - 응급처치 ====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "의료지도 내용",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "응급처치",
                    color = Color.White,
                    fontSize = 14.sp
                )

                MultiSelectButtonGroup(
                    options = listOf(
                        "airway", "intubation", "supraglottic airway", "ECG", "AED",
                        "CPR", "IV", "BVM", "산소투여", "고정",
                        "상처처치", "혈당체크", "보온", "기타"
                    ),
                    selectedOptions = selectedEmergencyCare,
                    onOptionsChanged = { selectedEmergencyCare = it },
                    columns = 5,
                    enabled = !isReadOnly
                )
            }
        }

        // ==== 응급처치 "기타" 입력 필드 (선택 시에만 표시) ====
        if (selectedEmergencyCare.contains("기타")) {
            item {
                TextField(
                    value = emergencyCareOtherValue,
                    onValueChange = { emergencyCareOtherValue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    enabled = !isReadOnly,
                    placeholder = {
                        Text(
                            text = "기타 응급처치를 입력하세요",
                            color = Color(0xFF999999),
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color(0xFF666666),
                        focusedIndicatorColor = Color(0xFF3a3a3a),
                        unfocusedIndicatorColor = Color(0xFF3a3a3a),
                        disabledIndicatorColor = Color(0xFF3a3a3a),
                        cursorColor = Color(0xFF3b7cff)
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = Color.White
                    ),
                    singleLine = true
                )
            }
        }

        // ==== 5. 약물투여 ====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "약물투여",
                    color = Color.White,
                    fontSize = 14.sp
                )

                MultiSelectButtonGroup(
                    options = listOf(
                        "N/S", "D/W", "NTG", "기관지확장제", "에피네프린", "아미오다론", "기타"
                    ),
                    selectedOptions = selectedMedication,
                    onOptionsChanged = { selectedMedication = it },
                    columns = 7,
                    enabled = !isReadOnly
                )
            }
        }

        // ==== 약물투여 "기타" 입력 필드 (선택 시에만 표시) ====
        if (selectedMedication.contains("기타")) {
            item {
                TextField(
                    value = medicationOtherValue,
                    onValueChange = { medicationOtherValue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    enabled = !isReadOnly,
                    placeholder = {
                        Text(
                            text = "기타 약물을 입력하세요",
                            color = Color(0xFF999999),
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color(0xFF666666),
                        focusedIndicatorColor = Color(0xFF3a3a3a),
                        unfocusedIndicatorColor = Color(0xFF3a3a3a),
                        disabledIndicatorColor = Color(0xFF3a3a3a),
                        cursorColor = Color(0xFF3b7cff)
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = Color.White
                    ),
                    singleLine = true
                )
            }
        }

        // ==== 6. 병원선정 (타이틀 없음, 다중 선택) ====
        item {
            MultiSelectButtonGroup(
                options = listOf("병원선정", "환자평가", "CPR유보중단", "이송거절", "이송거부"),
                selectedOptions = selectedHospitalSelections,
                onOptionsChanged = { selectedHospitalSelections = it },
                columns = 5,
                enabled = !isReadOnly
            )
        }
    }
}

// ==========================================
// 단일 선택 버튼 그룹
// ==========================================
@Composable
private fun SingleSelectButtonGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 5,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.chunked(columns).forEach { rowOptions ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowOptions.forEach { option ->
                    SelectButton(
                        text = option,
                        isSelected = selectedOption == option,
                        onClick = { onOptionSelected(option) },
                        modifier = Modifier.weight(1f),
                        enabled = enabled
                    )
                }
                // 빈 공간 채우기
                repeat(columns - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ==========================================
// 다중 선택 버튼 그룹
// ==========================================
@Composable
private fun MultiSelectButtonGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionsChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 5,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.chunked(columns).forEach { rowOptions ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowOptions.forEach { option ->
                    val isSelected = option in selectedOptions
                    SelectButton(
                        text = option,
                        isSelected = isSelected,
                        onClick = {
                            val newSelection = if (isSelected) {
                                selectedOptions - option
                            } else {
                                selectedOptions + option
                            }
                            onOptionsChanged(newSelection)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = enabled
                    )
                }
                // 빈 공간 채우기
                repeat(columns - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ==========================================
// 선택 버튼 (단일/다중 선택 모두 사용)
// ==========================================
@Composable
private fun SelectButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF3b7cff) else Color(0xFF3a3a3a),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF2a2a2a),
            disabledContentColor = Color(0xFF666666)
        ),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF4a4a4a))
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1
        )
    }
}