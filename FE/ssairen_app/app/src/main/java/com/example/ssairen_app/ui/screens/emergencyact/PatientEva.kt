// PatientEva.kt
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
import com.example.ssairen_app.viewmodel.PatientEvaData
import com.example.ssairen_app.viewmodel.PatientEvaApiState

private const val TAG = "PatientEva"

@Composable
fun PatientEva(
    viewModel: LogViewModel,
    data: ActivityLogData,
    isReadOnly: Boolean = false,
    activityViewModel: ActivityViewModel = viewModel()
) {
    val patientEvaState by activityViewModel.patientEvaState.observeAsState(PatientEvaApiState.Idle)
    val currentReportId by activityViewModel.currentEmergencyReportId.observeAsState()

    var isInitialLoad by remember { mutableStateOf(true) }

    LaunchedEffect(currentReportId) {
        currentReportId?.let { reportId ->
            while (true) {
                Log.d(TAG, "📞 자동 API 호출: getPatientEva($reportId)")
                activityViewModel.getPatientEva(reportId)
                kotlinx.coroutines.delay(5000)
            }
        }
    }

    var selectedLevel by remember { mutableStateOf(data.patientEva.patientLevel) }

    var consciousness1stTime by remember { mutableStateOf("") }
    var consciousness1stAlert by remember { mutableStateOf(data.patientEva.consciousness1stAlert) }
    var consciousness1stVerbal by remember { mutableStateOf(data.patientEva.consciousness1stVerbal) }
    var consciousness1stPainful by remember { mutableStateOf(data.patientEva.consciousness1stPainful) }
    var consciousness1stUnresponsive by remember { mutableStateOf(data.patientEva.consciousness1stUnresponsive) }

    var consciousness2ndTime by remember { mutableStateOf("") }
    var consciousness2ndAlert by remember { mutableStateOf(data.patientEva.consciousness2ndAlert) }
    var consciousness2ndVerbal by remember { mutableStateOf(data.patientEva.consciousness2ndVerbal) }
    var consciousness2ndPainful by remember { mutableStateOf(data.patientEva.consciousness2ndPainful) }
    var consciousness2ndUnresponsive by remember { mutableStateOf(data.patientEva.consciousness2ndUnresponsive) }

    var leftPupilStatus by remember { mutableStateOf("") }
    var leftPupilReaction by remember { mutableStateOf("") }
    var rightPupilStatus by remember { mutableStateOf("") }
    var rightPupilReaction by remember { mutableStateOf("") }

    var vitalSignsStatus by remember { mutableStateOf("") }

    var leftTime by remember { mutableStateOf(data.patientEva.leftTime) }
    var leftPulse by remember { mutableStateOf(data.patientEva.leftPulse) }
    var leftBloodPressure by remember { mutableStateOf(data.patientEva.leftBloodPressure) }
    var leftTemperature by remember { mutableStateOf(data.patientEva.leftTemperature) }
    var leftOxygenSaturation by remember { mutableStateOf(data.patientEva.leftOxygenSaturation) }
    var leftRespiratoryRate by remember { mutableStateOf(data.patientEva.leftRespiratoryRate) }
    var leftBloodSugar by remember { mutableStateOf(data.patientEva.leftBloodSugar) }

    var rightTime by remember { mutableStateOf(data.patientEva.rightTime) }
    var rightPulse by remember { mutableStateOf(data.patientEva.rightPulse) }
    var rightBloodPressure by remember { mutableStateOf(data.patientEva.rightBloodPressure) }
    var rightTemperature by remember { mutableStateOf(data.patientEva.rightTemperature) }
    var rightOxygenSaturation by remember { mutableStateOf(data.patientEva.rightOxygenSaturation) }
    var rightRespiratoryRate by remember { mutableStateOf(data.patientEva.rightRespiratoryRate) }
    var rightBloodSugar by remember { mutableStateOf(data.patientEva.rightBloodSugar) }

    fun saveData() {
        val evaData = PatientEvaData(
            patientLevel = selectedLevel,
            consciousness1stAlert = consciousness1stAlert,
            consciousness1stVerbal = consciousness1stVerbal,
            consciousness1stPainful = consciousness1stPainful,
            consciousness1stUnresponsive = consciousness1stUnresponsive,
            consciousness2ndAlert = consciousness2ndAlert,
            consciousness2ndVerbal = consciousness2ndVerbal,
            consciousness2ndPainful = consciousness2ndPainful,
            consciousness2ndUnresponsive = consciousness2ndUnresponsive,
            leftPupilNormal = false,
            leftPupilSlow = false,
            leftPupilReactive = false,
            leftPupilNonReactive = false,
            rightPupilNormal = false,
            rightPupilSlow = false,
            rightPupilReactive = false,
            rightPupilNonReactive = false,
            leftTime = leftTime,
            leftPulse = leftPulse,
            leftBloodPressure = leftBloodPressure,
            leftTemperature = leftTemperature,
            leftOxygenSaturation = leftOxygenSaturation,
            leftRespiratoryRate = leftRespiratoryRate,
            leftBloodSugar = leftBloodSugar,
            rightTime = rightTime,
            rightPulse = rightPulse,
            rightBloodPressure = rightBloodPressure,
            rightTemperature = rightTemperature,
            rightOxygenSaturation = rightOxygenSaturation,
            rightRespiratoryRate = rightRespiratoryRate,
            rightBloodSugar = rightBloodSugar
        )
        viewModel.updatePatientEva(evaData)
    }

    LaunchedEffect(patientEvaState) {
        when (val state = patientEvaState) {
            is PatientEvaApiState.Success -> {
                Log.d(TAG, "✅ API 응답 성공")
                isInitialLoad = false
                val apiData = state.patientEvaResponse.data.data.assessment

                selectedLevel = when (apiData.patientLevel) {
                    "LEVEL1" -> "LEVEL 1"
                    "LEVEL2" -> "LEVEL 2"
                    "LEVEL3" -> "LEVEL 3"
                    "LEVEL4" -> "LEVEL 4"
                    "LEVEL5" -> "LEVEL 5"
                    else -> apiData.patientLevel ?: ""
                }

                apiData.consciousness?.first?.let { first ->
                    consciousness1stTime = first.time ?: ""
                    when (first.state) {
                        "A" -> {
                            consciousness1stAlert = true
                            consciousness1stVerbal = false
                            consciousness1stPainful = false
                            consciousness1stUnresponsive = false
                        }
                        "V" -> {
                            consciousness1stAlert = false
                            consciousness1stVerbal = true
                            consciousness1stPainful = false
                            consciousness1stUnresponsive = false
                        }
                        "P" -> {
                            consciousness1stAlert = false
                            consciousness1stVerbal = false
                            consciousness1stPainful = true
                            consciousness1stUnresponsive = false
                        }
                        "U" -> {
                            consciousness1stAlert = false
                            consciousness1stVerbal = false
                            consciousness1stPainful = false
                            consciousness1stUnresponsive = true
                        }
                    }
                }

                apiData.consciousness?.second?.let { second ->
                    consciousness2ndTime = second.time ?: ""
                    when (second.state) {
                        "A" -> {
                            consciousness2ndAlert = true
                            consciousness2ndVerbal = false
                            consciousness2ndPainful = false
                            consciousness2ndUnresponsive = false
                        }
                        "V" -> {
                            consciousness2ndAlert = false
                            consciousness2ndVerbal = true
                            consciousness2ndPainful = false
                            consciousness2ndUnresponsive = false
                        }
                        "P" -> {
                            consciousness2ndAlert = false
                            consciousness2ndVerbal = false
                            consciousness2ndPainful = true
                            consciousness2ndUnresponsive = false
                        }
                        "U" -> {
                            consciousness2ndAlert = false
                            consciousness2ndVerbal = false
                            consciousness2ndPainful = false
                            consciousness2ndUnresponsive = true
                        }
                    }
                }

                apiData.pupilReaction?.left?.let { left ->
                    leftPupilStatus = left.status ?: ""
                    leftPupilReaction = left.reaction ?: ""
                }

                apiData.pupilReaction?.right?.let { right ->
                    rightPupilStatus = right.status ?: ""
                    rightPupilReaction = right.reaction ?: ""
                }

                apiData.vitalSigns?.first?.let { first ->
                    leftTime = first.time ?: ""
                    leftBloodPressure = first.bloodPressure ?: ""
                    leftPulse = first.pulse?.toString() ?: ""
                    leftRespiratoryRate = first.respiration?.toString() ?: ""
                    leftTemperature = first.temperature?.toString() ?: ""
                    leftOxygenSaturation = first.spo2?.toString() ?: ""
                    leftBloodSugar = first.bloodSugar?.toString() ?: ""
                }

                apiData.vitalSigns?.second?.let { second ->
                    rightTime = second.time ?: ""
                    rightBloodPressure = second.bloodPressure ?: ""
                    rightPulse = second.pulse?.toString() ?: ""
                    rightRespiratoryRate = second.respiration?.toString() ?: ""
                    rightTemperature = second.temperature?.toString() ?: ""
                    rightOxygenSaturation = second.spo2?.toString() ?: ""
                    rightBloodSugar = second.bloodSugar?.toString() ?: ""
                }

                saveData()
                Log.d(TAG, "💾 LogViewModel 동기화 완료")
            }
            is PatientEvaApiState.Error -> {
                Log.e(TAG, "❌ API 오류: ${state.message}")
            }
            is PatientEvaApiState.Loading -> {
                Log.d(TAG, "⏳ 로딩 중...")
            }
            else -> {}
        }
    }

    if (isInitialLoad && patientEvaState is PatientEvaApiState.Loading) {
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1a1a1a))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ==========================================
            // 환자 분류 (Level 1-5)
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "환자 분류",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("LEVEL 1", "LEVEL 2", "LEVEL 3", "LEVEL 4", "LEVEL 5").forEach { level ->
                        SelectButton(
                            text = level,
                            isSelected = selectedLevel == level,
                            onClick = {
                                selectedLevel = level
                                saveData()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                    }
                }
            }

            // ==========================================
            // 의식 상태
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "의식 상태",
                    color = Color.White,
                    fontSize = 14.sp
                )

                // 1차
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.width(120.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "1차",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            BasicTextField(
                                value = consciousness1stTime,
                                onValueChange = {
                                    consciousness1stTime = it
                                    saveData()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                textStyle = TextStyle(
                                    color = if (isReadOnly) Color(0xFF666666) else Color.White,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Start
                                ),
                                singleLine = true,
                                readOnly = isReadOnly,
                                enabled = !isReadOnly
                            )
                            HorizontalDivider(
                                color = Color(0xFF4a4a4a),
                                thickness = 1.dp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SelectButton(
                            text = "Alert",
                            isSelected = consciousness1stAlert,
                            onClick = {
                                consciousness1stAlert = true
                                consciousness1stVerbal = false
                                consciousness1stPainful = false
                                consciousness1stUnresponsive = false
                                saveData()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                        SelectButton(
                            text = "Verbal",
                            isSelected = consciousness1stVerbal,
                            onClick = {
                                consciousness1stAlert = false
                                consciousness1stVerbal = true
                                consciousness1stPainful = false
                                consciousness1stUnresponsive = false
                                saveData()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                        SelectButton(
                            text = "Painful",
                            isSelected = consciousness1stPainful,
                            onClick = {
                                consciousness1stAlert = false
                                consciousness1stVerbal = false
                                consciousness1stPainful = true
                                consciousness1stUnresponsive = false
                                saveData()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                        SelectButton(
                            text = "Unresponsive",
                            isSelected = consciousness1stUnresponsive,
                            onClick = {
                                consciousness1stAlert = false
                                consciousness1stVerbal = false
                                consciousness1stPainful = false
                                consciousness1stUnresponsive = true
                                saveData()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                    }
                }

                // 2차
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.width(120.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "2차",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            BasicTextField(
                                value = consciousness2ndTime,
                                onValueChange = {
                                    consciousness2ndTime = it
                                    saveData()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                textStyle = TextStyle(
                                    color = if (isReadOnly) Color(0xFF666666) else Color.White,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Start
                                ),
                                singleLine = true,
                                readOnly = isReadOnly,
                                enabled = !isReadOnly
                            )
                            HorizontalDivider(
                                color = Color(0xFF4a4a4a),
                                thickness = 1.dp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SelectButton(
                            text = "Alert",
                            isSelected = consciousness2ndAlert,
                            onClick = {
                                consciousness2ndAlert = true
                                consciousness2ndVerbal = false
                                consciousness2ndPainful = false
                                consciousness2ndUnresponsive = false
                                saveData()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                        SelectButton(
                            text = "Verbal",
                            isSelected = consciousness2ndVerbal,
                            onClick = {
                                consciousness2ndAlert = false
                                consciousness2ndVerbal = true
                                consciousness2ndPainful = false
                                consciousness2ndUnresponsive = false
                                saveData()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                        SelectButton(
                            text = "Painful",
                            isSelected = consciousness2ndPainful,
                            onClick = {
                                consciousness2ndAlert = false
                                consciousness2ndVerbal = false
                                consciousness2ndPainful = true
                                consciousness2ndUnresponsive = false
                                saveData()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                        SelectButton(
                            text = "Unresponsive",
                            isSelected = consciousness2ndUnresponsive,
                            onClick = {
                                consciousness2ndAlert = false
                                consciousness2ndVerbal = false
                                consciousness2ndPainful = false
                                consciousness2ndUnresponsive = true
                                saveData()
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isReadOnly
                        )
                    }
                }
            }

            // ==========================================
            // 동공반응
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "동공반응",
                    color = Color.White,
                    fontSize = 14.sp
                )

                // 좌
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "좌",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.width(40.dp)
                    )

                    SelectButton(
                        text = "정상",
                        isSelected = leftPupilStatus == "정상",
                        onClick = {
                            leftPupilStatus = if (leftPupilStatus == "정상") "" else "정상"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "축동",
                        isSelected = leftPupilStatus == "축소",
                        onClick = {
                            leftPupilStatus = if (leftPupilStatus == "축소") "" else "축소"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "산동",
                        isSelected = leftPupilStatus == "확대",
                        onClick = {
                            leftPupilStatus = if (leftPupilStatus == "확대") "" else "확대"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "측정불가",
                        isSelected = leftPupilStatus == "측정불가",
                        onClick = {
                            leftPupilStatus = if (leftPupilStatus == "측정불가") "" else "측정불가"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "반응",
                        isSelected = leftPupilReaction == "반응",
                        onClick = {
                            leftPupilReaction = if (leftPupilReaction == "반응") "" else "반응"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "무반응",
                        isSelected = leftPupilReaction == "무반응",
                        onClick = {
                            leftPupilReaction = if (leftPupilReaction == "무반응") "" else "무반응"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "측정불가",
                        isSelected = leftPupilReaction == "측정불가",
                        onClick = {
                            leftPupilReaction = if (leftPupilReaction == "측정불가") "" else "측정불가"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                }

                // 우
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "우",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.width(40.dp)
                    )

                    SelectButton(
                        text = "정상",
                        isSelected = rightPupilStatus == "정상",
                        onClick = {
                            rightPupilStatus = if (rightPupilStatus == "정상") "" else "정상"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "축동",
                        isSelected = rightPupilStatus == "축소",
                        onClick = {
                            rightPupilStatus = if (rightPupilStatus == "축소") "" else "축소"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "산동",
                        isSelected = rightPupilStatus == "확대",
                        onClick = {
                            rightPupilStatus = if (rightPupilStatus == "확대") "" else "확대"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "측정불가",
                        isSelected = rightPupilStatus == "측정불가",
                        onClick = {
                            rightPupilStatus = if (rightPupilStatus == "측정불가") "" else "측정불가"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "반응",
                        isSelected = rightPupilReaction == "반응",
                        onClick = {
                            rightPupilReaction = if (rightPupilReaction == "반응") "" else "반응"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "무반응",
                        isSelected = rightPupilReaction == "무반응",
                        onClick = {
                            rightPupilReaction = if (rightPupilReaction == "무반응") "" else "무반응"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    SelectButton(
                        text = "측정불가",
                        isSelected = rightPupilReaction == "측정불가",
                        onClick = {
                            rightPupilReaction = if (rightPupilReaction == "측정불가") "" else "측정불가"
                            saveData()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                }
            }

            // ==========================================
            // 활력 징후
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "활력 징후",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    SelectButton(
                        text = "불가",
                        isSelected = vitalSignsStatus == "불가",
                        onClick = {
                            vitalSignsStatus = if (vitalSignsStatus == "불가") "" else "불가"
                            saveData()
                        },
                        modifier = Modifier.width(80.dp),
                        enabled = !isReadOnly
                    )

                    SelectButton(
                        text = "거부",
                        isSelected = vitalSignsStatus == "거부",
                        onClick = {
                            vitalSignsStatus = if (vitalSignsStatus == "거부") "" else "거부"
                            saveData()
                        },
                        modifier = Modifier.width(80.dp),
                        enabled = !isReadOnly
                    )
                }

                // 좌
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "좌",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.width(40.dp)
                    )

                    VitalSignInputField(
                        value = leftTime,
                        onValueChange = {
                            leftTime = it
                            saveData()
                        },
                        placeholder = "시각(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = leftBloodPressure,
                        onValueChange = {
                            leftBloodPressure = it
                            saveData()
                        },
                        placeholder = "혈압(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = leftPulse,
                        onValueChange = {
                            leftPulse = it
                            saveData()
                        },
                        placeholder = "맥박(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = leftRespiratoryRate,
                        onValueChange = {
                            leftRespiratoryRate = it
                            saveData()
                        },
                        placeholder = "호흡(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = leftTemperature,
                        onValueChange = {
                            leftTemperature = it
                            saveData()
                        },
                        placeholder = "체온(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = leftOxygenSaturation,
                        onValueChange = {
                            leftOxygenSaturation = it
                            saveData()
                        },
                        placeholder = "SpO2(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = leftBloodSugar,
                        onValueChange = {
                            leftBloodSugar = it
                            saveData()
                        },
                        placeholder = "혈당체크",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                }

                // 우
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "우",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.width(40.dp)
                    )

                    VitalSignInputField(
                        value = rightTime,
                        onValueChange = {
                            rightTime = it
                            saveData()
                        },
                        placeholder = "시각(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = rightBloodPressure,
                        onValueChange = {
                            rightBloodPressure = it
                            saveData()
                        },
                        placeholder = "혈압(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = rightPulse,
                        onValueChange = {
                            rightPulse = it
                            saveData()
                        },
                        placeholder = "맥박(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = rightRespiratoryRate,
                        onValueChange = {
                            rightRespiratoryRate = it
                            saveData()
                        },
                        placeholder = "호흡(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = rightTemperature,
                        onValueChange = {
                            rightTemperature = it
                            saveData()
                        },
                        placeholder = "체온(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = rightOxygenSaturation,
                        onValueChange = {
                            rightOxygenSaturation = it
                            saveData()
                        },
                        placeholder = "SpO2(p)",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
                    VitalSignInputField(
                        value = rightBloodSugar,
                        onValueChange = {
                            rightBloodSugar = it
                            saveData()
                        },
                        placeholder = "혈당체크",
                        modifier = Modifier.weight(1f),
                        enabled = !isReadOnly
                    )
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

// ==========================================
// 하위 컴포넌트들
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
            color = Color.White,
            maxLines = 1
        )
    }
}

@Composable
private fun VitalSignInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Column(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textStyle = TextStyle(
                color = if (enabled) Color.White else Color(0xFF666666),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            ),
            singleLine = true,
            enabled = enabled,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color(0xFF666666),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
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