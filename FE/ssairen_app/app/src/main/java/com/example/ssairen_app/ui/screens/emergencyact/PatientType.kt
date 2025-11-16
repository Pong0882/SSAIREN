// PatientType.kt
package com.example.ssairen_app.ui.screens.emergencyact

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.viewmodel.ActivityLogData
import com.example.ssairen_app.viewmodel.ActivityViewModel
import com.example.ssairen_app.viewmodel.LogViewModel
import com.example.ssairen_app.viewmodel.PatienTypeData
import com.example.ssairen_app.viewmodel.PatientTypeApiState

private const val TAG = "PatientType"

@Composable
fun PatientType(
    viewModel: LogViewModel,
    data: ActivityLogData,
    isReadOnly: Boolean = false,
    activityViewModel: ActivityViewModel = viewModel()
) {
    Log.d(TAG, "🎬 PatientType Composable 시작")

    val patientTypeState by activityViewModel.patientTypeState.observeAsState(PatientTypeApiState.Idle)
    val currentReportId by activityViewModel.currentEmergencyReportId.observeAsState()

    LaunchedEffect(currentReportId) {
        currentReportId?.let { reportId ->
            Log.d(TAG, "📞 API 호출: getPatientType($reportId)")
            activityViewModel.getPatientType(reportId)
        }
    }

    var hasMedicalHistory by remember { mutableStateOf(data.patienType.hasMedicalHistory) }
    var medicalHistoryList by remember { mutableStateOf(data.patienType.medicalHistoryList) }
    var mainType by remember { mutableStateOf(data.patienType.mainType) }
    var crimeOption by remember { mutableStateOf(data.patienType.crimeOption) }
    var subType by remember { mutableStateOf(data.patienType.subType) }
    var accidentVictimType by remember { mutableStateOf(data.patienType.accidentVictimType) }
    var etcType by remember { mutableStateOf(data.patienType.etcType) }

    var nonTraumaSelections by remember { mutableStateOf(setOf<String>()) }

    fun saveData() {
        val patienTypeData = PatienTypeData(
            hasMedicalHistory = hasMedicalHistory,
            medicalHistoryList = medicalHistoryList,
            mainType = mainType,
            crimeOption = crimeOption,
            subType = subType,
            accidentVictimType = if (subType == "비외상성 손상") {
                nonTraumaSelections.joinToString(", ")
            } else {
                accidentVictimType
            },
            etcType = etcType
        )
        viewModel.updatePatienType(patienTypeData)
    }

    LaunchedEffect(patientTypeState) {
        when (val state = patientTypeState) {
            is PatientTypeApiState.Success -> {
                Log.d(TAG, "✅ API 응답 성공")
                val apiData = state.patientTypeResponse.data.data.incidentType

                Log.d(TAG, "📦 원본 API 데이터:")
                Log.d(TAG, "   - category: ${apiData.category}")
                Log.d(TAG, "   - categoryOther: ${apiData.categoryOther}")
                Log.d(TAG, "   - medicalHistory: ${apiData.medicalHistory}")
                Log.d(TAG, "   - legalSuspicion: ${apiData.legalSuspicion}")
                Log.d(TAG, "   - subCategoryTraffic: ${apiData.subCategoryTraffic}")
                Log.d(TAG, "   - subCategoryInjury: ${apiData.subCategoryInjury}")
                Log.d(TAG, "   - subCategoryNonTrauma: ${apiData.subCategoryNonTrauma}")
                Log.d(TAG, "   - subCategoryOther: ${apiData.subCategoryOther}")

                mainType = when (apiData.category) {
                    "질병" -> "질병"
                    "질병외" -> "질병 외"
                    "기타" -> "기타"
                    else -> ""
                }
                Log.d(TAG, "   ➡️ mainType 설정: ${apiData.category} → $mainType")

                apiData.medicalHistory?.let { medical ->
                    hasMedicalHistory = medical.status ?: ""
                    medicalHistoryList = medical.items?.map { it.name }?.toSet() ?: setOf()
                    Log.d(TAG, "   ➡️ 병력 설정: ${medical.status}")
                    Log.d(TAG, "   ➡️ 병력 목록: $medicalHistoryList")
                }

                apiData.legalSuspicion?.let { legal ->
                    crimeOption = legal.name ?: ""
                    Log.d(TAG, "   ➡️ 범죄 옵션: ${legal.name}")
                }

                apiData.subCategoryTraffic?.let { traffic ->
                    if (traffic.type == "교통사고") {
                        subType = "교통사고"
                        accidentVictimType = traffic.name ?: ""
                        Log.d(TAG, "   ➡️ 교통사고: ${traffic.name}")
                    }
                }

                apiData.subCategoryInjury?.let { injury ->
                    if (injury.type == "그 외 손상") {
                        subType = "그 외 외상"
                        accidentVictimType = injury.name ?: ""
                        Log.d(TAG, "   ➡️ 그 외 외상: ${injury.name}")
                    }
                }

                apiData.subCategoryNonTrauma?.let { nonTrauma ->
                    if (nonTrauma.type == "비외상성 손상") {
                        subType = "비외상성 손상"
                        val selectedItem = if (nonTrauma.value != null) {
                            "${nonTrauma.name}: ${nonTrauma.value}"
                        } else {
                            nonTrauma.name ?: ""
                        }
                        if (selectedItem.isNotEmpty()) {
                            nonTraumaSelections = nonTraumaSelections + selectedItem
                        }
                        accidentVictimType = nonTraumaSelections.joinToString(", ")
                        Log.d(TAG, "   ➡️ 비외상성 손상: $accidentVictimType")
                    }
                }

                apiData.subCategoryOther?.let { other ->
                    etcType = other.name ?: ""
                    Log.d(TAG, "   ➡️ 기타: ${other.name}")
                }

                Log.d(TAG, "✅ 최종 매핑 결과:")
                Log.d(TAG, "   - mainType: $mainType")
                Log.d(TAG, "   - hasMedicalHistory: $hasMedicalHistory")
                Log.d(TAG, "   - medicalHistoryList: $medicalHistoryList")
                Log.d(TAG, "   - crimeOption: $crimeOption")
                Log.d(TAG, "   - subType: $subType")
                Log.d(TAG, "   - accidentVictimType: $accidentVictimType")
                Log.d(TAG, "   - etcType: $etcType")

                saveData()
                Log.d(TAG, "💾 LogViewModel 동기화 완료")
            }
            is PatientTypeApiState.Error -> {
                Log.e(TAG, "❌ API 오류: ${state.message}")
            }
            is PatientTypeApiState.Loading -> {
                Log.d(TAG, "⏳ 로딩 중...")
            }
            else -> {}
        }
    }

    if (patientTypeState is PatientTypeApiState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1a1a1a)),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF3b7cff))
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1a1a1a))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp)  // ✅ 16.dp → 40.dp
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ✅ 헤더 제거

            // ✅ Surface 제거, Column만 사용
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top=16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "병력 유무",
                    color = Color.White,
                    fontSize = 14.sp  // ✅ fontWeight 제거
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("있음", "없음", "미상").forEach { option ->
                        SelectButton(
                            text = option,
                            isSelected = hasMedicalHistory == option,
                            onClick = {
                                hasMedicalHistory = option
                                if (option != "있음") medicalHistoryList = setOf()
                                saveData()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (hasMedicalHistory == "있음") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "병력 종류",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    listOf(
                        listOf("고혈압", "당뇨", "뇌혈관질환", "심장질환", "폐질환"),
                        listOf("결핵", "간염", "간경화", "알레르기", "암"),
                        listOf("신부전", "감염병", "기타")
                    ).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { item ->
                                SelectButton(
                                    text = item,
                                    isSelected = medicalHistoryList.contains(item),
                                    onClick = {
                                        medicalHistoryList = if (medicalHistoryList.contains(item)) {
                                            medicalHistoryList - item
                                        } else {
                                            medicalHistoryList + item
                                        }
                                        saveData()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size < 5) {
                                Spacer(modifier = Modifier.weight((5 - row.size).toFloat()))
                            }
                        }
                    }
                }
            }

            // 환자 발생 유형
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "환자 발생 유형",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("질병", "질병 외", "기타").forEach { type ->
                        SelectButton(
                            text = type,
                            isSelected = mainType == type,
                            onClick = {
                                mainType = type
                                if (type != "질병 외") {
                                    crimeOption = ""
                                    subType = ""
                                    accidentVictimType = ""
                                    nonTraumaSelections = setOf()
                                }
                                if (type != "기타") etcType = ""
                                saveData()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 질병 외 선택 시
            if (mainType == "질병 외") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "범죄가 의심입니까?",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("경찰통보", "경찰입회", "긴급이송", "관련기관 통보").forEach { item ->
                            SelectButton(
                                text = item,
                                isSelected = crimeOption == item,
                                onClick = {
                                    crimeOption = item
                                    saveData()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("교통사고", "그 외 외상", "비외상성 손상").forEach { type ->
                            SelectButton(
                                text = type,
                                isSelected = subType == type,
                                onClick = {
                                    subType = type
                                    accidentVictimType = ""
                                    nonTraumaSelections = setOf()
                                    saveData()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 교통사고 세부
                if (subType == "교통사고") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "교통사고의 사상자",
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        listOf(
                            listOf("운전자", "동승자", "보행자", "자전거", "오토바이"),
                            listOf("개인형 이동장치", "그 밖의 탈 것", "미상")
                        ).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { item ->
                                    SelectButton(
                                        text = item,
                                        isSelected = accidentVictimType == item,
                                        onClick = {
                                            accidentVictimType = item
                                            saveData()
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (row.size < 5) {
                                    Spacer(modifier = Modifier.weight((5 - row.size).toFloat()))
                                }
                            }
                        }
                    }
                }

                // 그 외 외상 세부
                if (subType == "그 외 외상") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "그 외 외상 유형",
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("낙상", "추락", "관통상", "기계", "농기계", "그 밖의 둔상").forEach { item ->
                                SelectButton(
                                    text = item,
                                    isSelected = accidentVictimType == item,
                                    onClick = {
                                        accidentVictimType = item
                                        saveData()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // 비외상성 손상 세부
                if (subType == "비외상성 손상") {
                    // 호흡위험
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "호흡위험이 있었나요? (다중 선택 가능)",
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("익수", "외력에 의한 압박", "이물질에 의한 기도막힘").forEach { item ->
                                SelectButton(
                                    text = item,
                                    isSelected = nonTraumaSelections.contains(item),
                                    onClick = {
                                        nonTraumaSelections = if (nonTraumaSelections.contains(item)) {
                                            nonTraumaSelections - item
                                        } else {
                                            nonTraumaSelections + item
                                        }
                                        saveData()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 화상
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "화상이 있었나요? (다중 선택 가능)",
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("화염", "고온체", "전기", "물").forEach { item ->
                                SelectButton(
                                    text = item,
                                    isSelected = nonTraumaSelections.contains(item),
                                    onClick = {
                                        nonTraumaSelections = if (nonTraumaSelections.contains(item)) {
                                            nonTraumaSelections - item
                                        } else {
                                            nonTraumaSelections + item
                                        }
                                        saveData()
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // 그 외 유형
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "그 외 발생 유형을 선택해주세요. (다중 선택 가능)",
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        listOf(
                            listOf("연기흡입", "중독", "화학물질", "동물/곤충"),
                            listOf("온열손상", "한랭손상", "성폭행", "상해")
                        ).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                row.forEach { item ->
                                    SelectButton(
                                        text = item,
                                        isSelected = nonTraumaSelections.contains(item),
                                        onClick = {
                                            nonTraumaSelections = if (nonTraumaSelections.contains(item)) {
                                                nonTraumaSelections - item
                                            } else {
                                                nonTraumaSelections + item
                                            }
                                            saveData()
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        var etcInput by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            SelectButton(
                                text = "기타",
                                isSelected = nonTraumaSelections.any { it.startsWith("기타") },
                                onClick = {
                                    val etcValue = if (etcInput.isNotEmpty()) "기타: $etcInput" else "기타"
                                    nonTraumaSelections = if (nonTraumaSelections.any { it.startsWith("기타") }) {
                                        nonTraumaSelections.filterNot { it.startsWith("기타") }.toSet() + etcValue
                                    } else {
                                        nonTraumaSelections + etcValue
                                    }
                                    saveData()
                                },
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = etcInput,
                                onValueChange = { newValue ->
                                    etcInput = newValue
                                    if (nonTraumaSelections.any { it.startsWith("기타") }) {
                                        nonTraumaSelections = nonTraumaSelections.filterNot { it.startsWith("기타") }.toSet() + "기타: $newValue"
                                        saveData()
                                    }
                                },
                                modifier = Modifier.weight(3f).height(36.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF3a3a3a),
                                    unfocusedContainerColor = Color(0xFF3a3a3a),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.White,
                                    focusedBorderColor = Color(0xFF3b7cff),
                                    unfocusedBorderColor = Color(0xFF4a4a4a)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp),
                                singleLine = true,
                                placeholder = {
                                    Text(text = "직접 입력", color = Color(0xFF666666), fontSize = 12.sp)
                                }
                            )
                        }
                    }
                }
            }

            // 기타 선택 시
            if (mainType == "기타") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("자연재해", "임신분만", "신생아", "단순구조", "기타").forEach { item ->
                            SelectButton(
                                text = item,
                                isSelected = etcType == item,
                                onClick = {
                                    etcType = item
                                    saveData()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        if (isReadOnly) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
            )
        }
    }
}

// ✅ SelectButton 컴포넌트 추가 (PatientInfo와 동일)
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
        shape = RoundedCornerShape(4.dp),  // ✅ 6.dp → 4.dp
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF4a4a4a))  // ✅ 테두리 추가
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