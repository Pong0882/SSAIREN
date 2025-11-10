//// PatientEva.kt
//package com.example.ssairen_app.ui.screens.emergencyact
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.runtime.livedata.observeAsState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.ssairen_app.ui.components.MainButton
//import com.example.ssairen_app.viewmodel.ActivityLogData
//import com.example.ssairen_app.viewmodel.ActivityViewModel
//import com.example.ssairen_app.viewmodel.LogViewModel
//import com.example.ssairen_app.viewmodel.PatientEvaData
//import com.example.ssairen_app.viewmodel.PatientEvaState
//
//@Composable
//fun PatientEva(
//    viewModel: LogViewModel,
//    data: ActivityLogData,
//    activityViewModel: ActivityViewModel = viewModel()  // ✅ ActivityViewModel 추가
//) {
//    // ✅ API 상태 관찰
//    val patientEvaState by activityViewModel.patientEvaState.observeAsState(PatientEvaState.Idle)
//    val currentReportId by activityViewModel.currentEmergencyReportId.observeAsState(21)
//
//    // ✅ API 호출 (화면 진입 시 1회)
//    LaunchedEffect(currentReportId) {
//        activityViewModel.getPatientEva(currentReportId)
//    }
//
//    // ✅ data.patientEva로 초기화
//    var selectedLevel by remember { mutableStateOf(data.patientEva.patientLevel) }
//
//    // 의식 상태 (1차, 2차)
//    var consciousness1stAlert by remember { mutableStateOf(data.patientEva.consciousness1stAlert) }
//    var consciousness1stVerbal by remember { mutableStateOf(data.patientEva.consciousness1stVerbal) }
//    var consciousness1stPainful by remember { mutableStateOf(data.patientEva.consciousness1stPainful) }
//    var consciousness1stUnresponsive by remember { mutableStateOf(data.patientEva.consciousness1stUnresponsive) }
//
//    var consciousness2ndAlert by remember { mutableStateOf(data.patientEva.consciousness2ndAlert) }
//    var consciousness2ndVerbal by remember { mutableStateOf(data.patientEva.consciousness2ndVerbal) }
//    var consciousness2ndPainful by remember { mutableStateOf(data.patientEva.consciousness2ndPainful) }
//    var consciousness2ndUnresponsive by remember { mutableStateOf(data.patientEva.consciousness2ndUnresponsive) }
//
//    // 동공반응 (좌/우)
//    var leftPupilNormal by remember { mutableStateOf(data.patientEva.leftPupilNormal) }
//    var leftPupilSlow by remember { mutableStateOf(data.patientEva.leftPupilSlow) }
//    var leftPupilReactive by remember { mutableStateOf(data.patientEva.leftPupilReactive) }
//    var leftPupilNonReactive by remember { mutableStateOf(data.patientEva.leftPupilNonReactive) }
//
//    var rightPupilNormal by remember { mutableStateOf(data.patientEva.rightPupilNormal) }
//    var rightPupilSlow by remember { mutableStateOf(data.patientEva.rightPupilSlow) }
//    var rightPupilReactive by remember { mutableStateOf(data.patientEva.rightPupilReactive) }
//    var rightPupilNonReactive by remember { mutableStateOf(data.patientEva.rightPupilNonReactive) }
//
//    // 활력 징후 (좌/우)
//    var leftTime by remember { mutableStateOf(data.patientEva.leftTime) }
//    var leftPulse by remember { mutableStateOf(data.patientEva.leftPulse) }
//    var leftBloodPressure by remember { mutableStateOf(data.patientEva.leftBloodPressure) }
//    var leftTemperature by remember { mutableStateOf(data.patientEva.leftTemperature) }
//    var leftOxygenSaturation by remember { mutableStateOf(data.patientEva.leftOxygenSaturation) }
//    var leftRespiratoryRate by remember { mutableStateOf(data.patientEva.leftRespiratoryRate) }
//    var leftBloodSugar by remember { mutableStateOf(data.patientEva.leftBloodSugar) }
//
//    var rightTime by remember { mutableStateOf(data.patientEva.rightTime) }
//    var rightPulse by remember { mutableStateOf(data.patientEva.rightPulse) }
//    var rightBloodPressure by remember { mutableStateOf(data.patientEva.rightBloodPressure) }
//    var rightTemperature by remember { mutableStateOf(data.patientEva.rightTemperature) }
//    var rightOxygenSaturation by remember { mutableStateOf(data.patientEva.rightOxygenSaturation) }
//    var rightRespiratoryRate by remember { mutableStateOf(data.patientEva.rightRespiratoryRate) }
//    var rightBloodSugar by remember { mutableStateOf(data.patientEva.rightBloodSugar) }
//
//    // ✅ API 응답 처리
//    LaunchedEffect(patientEvaState) {
//        when (val state = patientEvaState) {
//            is PatientEvaState.Success -> {
//                val apiData = state.patientEvaResponse.data.data.patientAssessment
//
//                // 환자 레벨 매핑
//                selectedLevel = when (apiData.patientLevel) {
//                    "LEVEL1" -> "LEVEL 1"
//                    "LEVEL2" -> "LEVEL 2"
//                    "LEVEL3" -> "LEVEL 3"
//                    "LEVEL4" -> "LEVEL 4"
//                    "LEVEL5" -> "LEVEL 5"
//                    else -> apiData.patientLevel ?: ""
//                }
//
//                // 의식 상태 매핑 (1차)
//                apiData.consciousness?.first?.let { first ->
//                    when (first.state) {
//                        "A" -> consciousness1stAlert = true
//                        "V" -> consciousness1stVerbal = true
//                        "P" -> consciousness1stPainful = true
//                        "U" -> consciousness1stUnresponsive = true
//                    }
//                }
//
//                // 의식 상태 매핑 (2차)
//                apiData.consciousness?.second?.let { second ->
//                    when (second.state) {
//                        "A" -> consciousness2ndAlert = true
//                        "V" -> consciousness2ndVerbal = true
//                        "P" -> consciousness2ndPainful = true
//                        "U" -> consciousness2ndUnresponsive = true
//                    }
//                }
//
//                // 동공반응 매핑 (좌)
//                apiData.pupilReaction?.left?.let { left ->
//                    when (left.status) {
//                        "정상" -> leftPupilNormal = true
//                        "축소" -> leftPupilSlow = true
//                        "확대" -> leftPupilReactive = true
//                    }
//                    when (left.reaction) {
//                        "무반응" -> leftPupilNonReactive = true
//                    }
//                }
//
//                // 동공반응 매핑 (우)
//                apiData.pupilReaction?.right?.let { right ->
//                    when (right.status) {
//                        "정상" -> rightPupilNormal = true
//                        "축소" -> rightPupilSlow = true
//                        "확대" -> rightPupilReactive = true
//                    }
//                    when (right.reaction) {
//                        "무반응" -> rightPupilNonReactive = true
//                    }
//                }
//
//                // 활력징후 매핑 (1차 - 좌)
//                apiData.vitalSigns?.first?.let { first ->
//                    leftTime = first.time ?: ""
//                    leftBloodPressure = first.bloodPressure ?: ""
//                    leftPulse = first.pulse?.toString() ?: ""
//                    leftRespiratoryRate = first.respiration?.toString() ?: ""
//                    leftTemperature = first.temperature?.toString() ?: ""
//                    leftOxygenSaturation = first.spo2?.toString() ?: ""
//                    leftBloodSugar = first.bloodSugar?.toString() ?: ""
//                }
//
//                // 활력징후 매핑 (2차 - 우)
//                apiData.vitalSigns?.second?.let { second ->
//                    rightTime = second.time ?: ""
//                    rightBloodPressure = second.bloodPressure ?: ""
//                    rightPulse = second.pulse?.toString() ?: ""
//                    rightRespiratoryRate = second.respiration?.toString() ?: ""
//                    rightTemperature = second.temperature?.toString() ?: ""
//                    rightOxygenSaturation = second.spo2?.toString() ?: ""
//                    rightBloodSugar = second.bloodSugar?.toString() ?: ""
//                }
//            }
//            is PatientEvaState.Error -> {
//                // 에러 처리
//                android.util.Log.e("PatientEva", "API 오류: ${state.message}")
//            }
//            else -> { /* Loading or Idle */ }
//        }
//    }
//
//    // ✅ 자동 저장 함수
//    fun saveData() {
//        val evaData = PatientEvaData(
//            patientLevel = selectedLevel,
//            consciousness1stAlert = consciousness1stAlert,
//            consciousness1stVerbal = consciousness1stVerbal,
//            consciousness1stPainful = consciousness1stPainful,
//            consciousness1stUnresponsive = consciousness1stUnresponsive,
//            consciousness2ndAlert = consciousness2ndAlert,
//            consciousness2ndVerbal = consciousness2ndVerbal,
//            consciousness2ndPainful = consciousness2ndPainful,
//            consciousness2ndUnresponsive = consciousness2ndUnresponsive,
//            leftPupilNormal = leftPupilNormal,
//            leftPupilSlow = leftPupilSlow,
//            leftPupilReactive = leftPupilReactive,
//            leftPupilNonReactive = leftPupilNonReactive,
//            rightPupilNormal = rightPupilNormal,
//            rightPupilSlow = rightPupilSlow,
//            rightPupilReactive = rightPupilReactive,
//            rightPupilNonReactive = rightPupilNonReactive,
//            leftTime = leftTime,
//            leftPulse = leftPulse,
//            leftBloodPressure = leftBloodPressure,
//            leftTemperature = leftTemperature,
//            leftOxygenSaturation = leftOxygenSaturation,
//            leftRespiratoryRate = leftRespiratoryRate,
//            leftBloodSugar = leftBloodSugar,
//            rightTime = rightTime,
//            rightPulse = rightPulse,
//            rightBloodPressure = rightBloodPressure,
//            rightTemperature = rightTemperature,
//            rightOxygenSaturation = rightOxygenSaturation,
//            rightRespiratoryRate = rightRespiratoryRate,
//            rightBloodSugar = rightBloodSugar
//        )
//        viewModel.updatePatientEva(evaData)
//    }
//
//    // ✅ 로딩 중일 때 표시
//    if (patientEvaState is PatientEvaState.Loading) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color(0xFF1a1a1a)),
//            contentAlignment = Alignment.Center
//        ) {
//            CircularProgressIndicator(color = Color(0xFF3b7cff))
//        }
//        return
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFF1a1a1a))
//            .verticalScroll(rememberScrollState())
//            .padding(horizontal = 16.dp)
//            .padding(bottom = 80.dp),
//        verticalArrangement = Arrangement.spacedBy(20.dp)
//    ) {
//        // 헤더
//        Text(
//            text = "세부항목-환자평가",
//            color = Color.White,
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//            modifier = Modifier.padding(vertical = 8.dp)
//        )
//
//        // ==========================================
//        // 환자 분류 (Level 1-5)
//        // ==========================================
//        Surface(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(8.dp),
//            color = Color(0xFF2a2a2a)
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Text(
//                    text = "환자 분류",
//                    color = Color.White,
//                    fontSize = 15.sp,
//                    fontWeight = FontWeight.SemiBold
//                )
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    listOf("LEVEL 1", "LEVEL 2", "LEVEL 3", "LEVEL 4", "LEVEL 5").forEach { level ->
//                        MainButton(
//                            onClick = {
//                                selectedLevel = level
//                                saveData()
//                            },
//                            modifier = Modifier
//                                .weight(1f)
//                                .height(40.dp),
//                            backgroundColor = if (selectedLevel == level)
//                                Color(0xFF3b7cff) else Color(0xFF3a3a3a),
//                            cornerRadius = 6.dp
//                        ) {
//                            Text(
//                                text = level.replace("LEVEL ", ""),
//                                fontSize = 12.sp,
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//            }
//        }
//
//        // ==========================================
//        // 의식 상태
//        // ==========================================
//        Surface(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(8.dp),
//            color = Color(0xFF2a2a2a)
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Text(
//                    text = "의식 상태",
//                    color = Color.White,
//                    fontSize = 15.sp,
//                    fontWeight = FontWeight.SemiBold
//                )
//
//                // 1차
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Text(
//                        text = "1차",
//                        color = Color(0xFF999999),
//                        fontSize = 14.sp,
//                        modifier = Modifier.width(40.dp)
//                    )
//
//                    TimeInputField(
//                        value = "00:00:00",
//                        onValueChange = { },
//                        modifier = Modifier.width(100.dp)
//                    )
//
//                    Row(
//                        modifier = Modifier.weight(1f),
//                        horizontalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        ToggleButton("Alert", consciousness1stAlert) {
//                            consciousness1stAlert = it
//                            saveData()
//                        }
//                        ToggleButton("Verbal", consciousness1stVerbal) {
//                            consciousness1stVerbal = it
//                            saveData()
//                        }
//                        ToggleButton("Painful", consciousness1stPainful) {
//                            consciousness1stPainful = it
//                            saveData()
//                        }
//                        ToggleButton("Unresponsive", consciousness1stUnresponsive) {
//                            consciousness1stUnresponsive = it
//                            saveData()
//                        }
//                    }
//                }
//
//                HorizontalDivider(color = Color(0xFF4a4a4a), thickness = 1.dp)
//
//                // 2차
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Text(
//                        text = "2차",
//                        color = Color(0xFF999999),
//                        fontSize = 14.sp,
//                        modifier = Modifier.width(40.dp)
//                    )
//
//                    TimeInputField(
//                        value = "00:00:00",
//                        onValueChange = { },
//                        modifier = Modifier.width(100.dp)
//                    )
//
//                    Row(
//                        modifier = Modifier.weight(1f),
//                        horizontalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        ToggleButton("Alert", consciousness2ndAlert) {
//                            consciousness2ndAlert = it
//                            saveData()
//                        }
//                        ToggleButton("Verbal", consciousness2ndVerbal) {
//                            consciousness2ndVerbal = it
//                            saveData()
//                        }
//                        ToggleButton("Painful", consciousness2ndPainful) {
//                            consciousness2ndPainful = it
//                            saveData()
//                        }
//                        ToggleButton("Unresponsive", consciousness2ndUnresponsive) {
//                            consciousness2ndUnresponsive = it
//                            saveData()
//                        }
//                    }
//                }
//            }
//        }
//
//        // ==========================================
//        // 동공반응
//        // ==========================================
//        Surface(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(8.dp),
//            color = Color(0xFF2a2a2a)
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Text(
//                    text = "동공반응",
//                    color = Color.White,
//                    fontSize = 15.sp,
//                    fontWeight = FontWeight.SemiBold
//                )
//
//                // 좌
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Text(
//                        text = "좌",
//                        color = Color(0xFF999999),
//                        fontSize = 14.sp,
//                        modifier = Modifier.width(40.dp)
//                    )
//
//                    Row(
//                        modifier = Modifier.weight(1f),
//                        horizontalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        ToggleButton("정상", leftPupilNormal) {
//                            leftPupilNormal = it
//                            saveData()
//                        }
//                        ToggleButton("축동", leftPupilSlow) {
//                            leftPupilSlow = it
//                            saveData()
//                        }
//                        ToggleButton("산동", leftPupilReactive) {
//                            leftPupilReactive = it
//                            saveData()
//                        }
//                        ToggleButton("축정불가", leftPupilNonReactive) {
//                            leftPupilNonReactive = it
//                            saveData()
//                        }
//                        ToggleButton("반응", false) { }
//                        ToggleButton("무반응", false) { }
//                        ToggleButton("축정불가", false) { }
//                    }
//                }
//
//                HorizontalDivider(color = Color(0xFF4a4a4a), thickness = 1.dp)
//
//                // 우
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Text(
//                        text = "우",
//                        color = Color(0xFF999999),
//                        fontSize = 14.sp,
//                        modifier = Modifier.width(40.dp)
//                    )
//
//                    Row(
//                        modifier = Modifier.weight(1f),
//                        horizontalArrangement = Arrangement.spacedBy(4.dp)
//                    ) {
//                        ToggleButton("정상", rightPupilNormal) {
//                            rightPupilNormal = it
//                            saveData()
//                        }
//                        ToggleButton("축동", rightPupilSlow) {
//                            rightPupilSlow = it
//                            saveData()
//                        }
//                        ToggleButton("산동", rightPupilReactive) {
//                            rightPupilReactive = it
//                            saveData()
//                        }
//                        ToggleButton("축정불가", rightPupilNonReactive) {
//                            rightPupilNonReactive = it
//                            saveData()
//                        }
//                        ToggleButton("반응", false) { }
//                        ToggleButton("무반응", false) { }
//                        ToggleButton("축정불가", false) { }
//                    }
//                }
//            }
//        }
//
//        // ==========================================
//        // 활력 징후
//        // ==========================================
//        Surface(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(8.dp),
//            color = Color(0xFF2a2a2a)
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Text(
//                    text = "활력 징후",
//                    color = Color.White,
//                    fontSize = 15.sp,
//                    fontWeight = FontWeight.SemiBold
//                )
//
//                // 헤더 행
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                ) {
//                    Text("", modifier = Modifier.width(40.dp))
//                    VitalSignHeader("시각(p)", Modifier.weight(1f))
//                    VitalSignHeader("혈압(p)", Modifier.weight(1f))
//                    VitalSignHeader("맥박(p)", Modifier.weight(1f))
//                    VitalSignHeader("호흡(p)", Modifier.weight(1f))
//                    VitalSignHeader("체온(p)", Modifier.weight(1f))
//                    VitalSignHeader("SpO2(p)", Modifier.weight(1f))
//                    VitalSignHeader("혈당체크", Modifier.weight(1f))
//                }
//
//                HorizontalDivider(color = Color(0xFF4a4a4a), thickness = 1.dp)
//
//                // 좌
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                ) {
//                    Text(
//                        text = "좌",
//                        color = Color(0xFF999999),
//                        fontSize = 14.sp,
//                        modifier = Modifier.width(40.dp)
//                    )
//                    VitalSignInput(leftTime, {
//                        leftTime = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(leftBloodPressure, {
//                        leftBloodPressure = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(leftPulse, {
//                        leftPulse = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(leftRespiratoryRate, {
//                        leftRespiratoryRate = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(leftTemperature, {
//                        leftTemperature = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(leftOxygenSaturation, {
//                        leftOxygenSaturation = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(leftBloodSugar, {
//                        leftBloodSugar = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                }
//
//                HorizontalDivider(color = Color(0xFF4a4a4a), thickness = 1.dp)
//
//                // 우
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(4.dp)
//                ) {
//                    Text(
//                        text = "우",
//                        color = Color(0xFF999999),
//                        fontSize = 14.sp,
//                        modifier = Modifier.width(40.dp)
//                    )
//                    VitalSignInput(rightTime, {
//                        rightTime = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(rightBloodPressure, {
//                        rightBloodPressure = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(rightPulse, {
//                        rightPulse = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(rightRespiratoryRate, {
//                        rightRespiratoryRate = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(rightTemperature, {
//                        rightTemperature = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(rightOxygenSaturation, {
//                        rightOxygenSaturation = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                    VitalSignInput(rightBloodSugar, {
//                        rightBloodSugar = it
//                        saveData()
//                    }, Modifier.weight(1f))
//                }
//            }
//        }
//    }
//}
//
//// ==========================================
//// 하위 컴포넌트들
//// ==========================================
//@Composable
//private fun TimeInputField(
//    value: String,
//    onValueChange: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    OutlinedTextField(
//        value = value,
//        onValueChange = onValueChange,
//        modifier = modifier.height(36.dp),
//        textStyle = androidx.compose.ui.text.TextStyle(
//            color = Color.White,
//            fontSize = 12.sp,
//            textAlign = TextAlign.Center
//        ),
//        colors = OutlinedTextFieldDefaults.colors(
//            focusedContainerColor = Color(0xFF3a3a3a),
//            unfocusedContainerColor = Color(0xFF3a3a3a),
//            focusedBorderColor = Color(0xFF4a4a4a),
//            unfocusedBorderColor = Color(0xFF4a4a4a),
//            focusedTextColor = Color.White,
//            unfocusedTextColor = Color.White
//        ),
//        shape = RoundedCornerShape(4.dp),
//        singleLine = true
//    )
//}
//
//@Composable
//private fun RowScope.ToggleButton(
//    text: String,
//    isSelected: Boolean,
//    onToggle: (Boolean) -> Unit
//) {
//    MainButton(
//        onClick = { onToggle(!isSelected) },
//        modifier = Modifier
//            .weight(1f)
//            .height(32.dp),
//        backgroundColor = if (isSelected) Color(0xFF3b7cff) else Color(0xFF3a3a3a),
//        cornerRadius = 4.dp
//    ) {
//        Text(
//            text = text,
//            fontSize = 11.sp,
//            fontWeight = FontWeight.Normal
//        )
//    }
//}
//
//@Composable
//private fun VitalSignHeader(text: String, modifier: Modifier = Modifier) {
//    Text(
//        text = text,
//        color = Color(0xFF999999),
//        fontSize = 11.sp,
//        modifier = modifier,
//        textAlign = TextAlign.Center
//    )
//}
//
//@Composable
//private fun VitalSignInput(
//    value: String,
//    onValueChange: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    OutlinedTextField(
//        value = value,
//        onValueChange = onValueChange,
//        modifier = modifier.height(36.dp),
//        textStyle = androidx.compose.ui.text.TextStyle(
//            color = Color.White,
//            fontSize = 12.sp,
//            textAlign = TextAlign.Center
//        ),
//        colors = OutlinedTextFieldDefaults.colors(
//            focusedContainerColor = Color(0xFF3a3a3a),
//            unfocusedContainerColor = Color(0xFF3a3a3a),
//            focusedBorderColor = Color(0xFF4a4a4a),
//            unfocusedBorderColor = Color(0xFF4a4a4a),
//            focusedTextColor = Color.White,
//            unfocusedTextColor = Color.White
//        ),
//        shape = RoundedCornerShape(4.dp),
//        singleLine = true
//    )
//}