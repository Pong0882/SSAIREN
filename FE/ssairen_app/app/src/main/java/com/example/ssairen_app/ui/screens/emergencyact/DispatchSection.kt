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
import com.example.ssairen_app.viewmodel.DispatchApiState
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 구급출동 섹션 메인 화면
 *
 * @param viewModel LogViewModel
 * @param data ActivityLogData
 * @param isReadOnly 읽기 전용 모드
 * @param activityViewModel ActivityViewModel (API 호출용)
 */
@Composable
fun DispatchSection(
    viewModel: LogViewModel,
    data: ActivityLogData,
    isReadOnly: Boolean = false,
    activityViewModel: ActivityViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val dispatchData = data.dispatch

    // API 상태 관찰
    val dispatchState by activityViewModel.dispatchState.observeAsState(DispatchApiState.Idle)
    val currentReportId by activityViewModel.currentEmergencyReportId.observeAsState()

    // API 호출 (currentReportId가 설정되면 자동 실행)
    LaunchedEffect(currentReportId) {
        currentReportId?.let { reportId ->
            Log.d("DispatchSection", "📞 API 호출: getDispatch($reportId)")
            activityViewModel.getDispatch(reportId)
        }
    }

    // 로컬 UI 상태 - ViewModel에서 가져온 값으로 초기화
    var reportDatetime by remember { mutableStateOf(dispatchData.reportDatetime) }
    var departureTime by remember { mutableStateOf(dispatchData.departureTime) }
    var arrivalSceneTime by remember { mutableStateOf(dispatchData.arrivalSceneTime) }
    var departureSceneTime by remember { mutableStateOf(dispatchData.departureSceneTime) }
    var contactTime by remember { mutableStateOf(dispatchData.contactTime) }
    var arrivalHospitalTime by remember { mutableStateOf(dispatchData.arrivalHospitalTime) }
    var returnTime by remember { mutableStateOf(dispatchData.returnTime) }
    var distance by remember { mutableStateOf(if (dispatchData.distanceKm > 0) "${dispatchData.distanceKm}" else "") }

    var selectedDispatchType by remember { mutableStateOf(dispatchData.dispatchType) }
    var selectedLocation by remember { mutableStateOf(dispatchData.sceneLocationName) }
    var locationDetailValue by remember { mutableStateOf(dispatchData.sceneLocationValue ?: "") }

    var selectedPains by remember { mutableStateOf(dispatchData.painSymptoms) }
    var selectedInjuries by remember { mutableStateOf(dispatchData.traumaSymptoms) }
    var selectedSymptoms by remember { mutableStateOf(dispatchData.otherSymptoms) }

    var otherPainValue by remember { mutableStateOf(dispatchData.otherPainValue ?: "") }
    var otherSymptomValue by remember { mutableStateOf(dispatchData.otherSymptomValue ?: "") }

    // API 응답 처리
    LaunchedEffect(dispatchState) {
        Log.d("DispatchSection", "🟢 dispatchState 변경: $dispatchState")

        when (val state = dispatchState) {
            is DispatchApiState.Success -> {
                Log.d("DispatchSection", "✅ API 성공 - 데이터 매핑 시작")
                val apiData = state.dispatchResponse.data.data.dispatch

                reportDatetime = apiData.reportDatetime ?: ""
                departureTime = apiData.departureTime ?: ""
                arrivalSceneTime = apiData.arrivalSceneTime ?: ""
                departureSceneTime = apiData.departureSceneTime ?: ""
                contactTime = apiData.contactTime ?: ""
                arrivalHospitalTime = apiData.arrivalHospitalTime ?: ""
                returnTime = apiData.returnTime ?: ""
                distance = apiData.distanceKm?.toString() ?: ""

                selectedDispatchType = apiData.dispatchType ?: "정상"
                selectedLocation = apiData.sceneLocation.name ?: "집"
                locationDetailValue = apiData.sceneLocation.value ?: ""

                // 증상 데이터 파싱 (name을 사용)
                selectedPains = apiData.symptoms.pain?.mapNotNull { it.name }?.toSet() ?: setOf()
                selectedInjuries = apiData.symptoms.trauma?.mapNotNull { it.name }?.toSet() ?: setOf()
                selectedSymptoms = apiData.symptoms.otherSymptoms?.mapNotNull { it.name }?.toSet() ?: setOf()

                // 기타 값 처리
                otherPainValue = apiData.symptoms.pain?.find { it.name == "그 밖의 통증" }?.value ?: ""
                otherSymptomValue = apiData.symptoms.otherSymptoms?.find { it.name == "기타" }?.value ?: ""

                Log.d("DispatchSection", "✅ 데이터 매핑 완료")
            }
            is DispatchApiState.Error -> {
                Log.e("DispatchSection", "❌ API 오류: ${state.message}")
            }
            is DispatchApiState.Loading -> {
                Log.d("DispatchSection", "⏳ 로딩 중...")
            }
            else -> {
                Log.d("DispatchSection", "⚪ Idle 상태")
            }
        }
    }

    // ViewModel 데이터가 변경되면 UI 상태 업데이트
    LaunchedEffect(dispatchData) {
        reportDatetime = dispatchData.reportDatetime
        departureTime = dispatchData.departureTime
        arrivalSceneTime = dispatchData.arrivalSceneTime
        departureSceneTime = dispatchData.departureSceneTime
        contactTime = dispatchData.contactTime
        arrivalHospitalTime = dispatchData.arrivalHospitalTime
        returnTime = dispatchData.returnTime
        distance = if (dispatchData.distanceKm > 0) "${dispatchData.distanceKm}" else ""
        selectedDispatchType = dispatchData.dispatchType
        selectedLocation = dispatchData.sceneLocationName
        locationDetailValue = dispatchData.sceneLocationValue ?: ""
        selectedPains = dispatchData.painSymptoms
        selectedInjuries = dispatchData.traumaSymptoms
        selectedSymptoms = dispatchData.otherSymptoms
        otherPainValue = dispatchData.otherPainValue ?: ""
        otherSymptomValue = dispatchData.otherSymptomValue ?: ""
    }

    // 값이 변경될 때마다 ViewModel 업데이트 (읽기 전용이 아닐 때만)
    LaunchedEffect(
        departureTime, arrivalSceneTime, departureSceneTime, contactTime,
        arrivalHospitalTime, returnTime, distance, selectedDispatchType,
        selectedLocation, locationDetailValue, selectedPains, selectedInjuries, selectedSymptoms,
        otherPainValue, otherSymptomValue
    ) {
        if (!isReadOnly) {
            viewModel.updateDispatch(
                com.example.ssairen_app.viewmodel.DispatchData(
                reportDatetime = reportDatetime,
                departureTime = departureTime,
                arrivalSceneTime = arrivalSceneTime,
                departureSceneTime = departureSceneTime,
                contactTime = contactTime,
                arrivalHospitalTime = arrivalHospitalTime,
                distanceKm = distance.toDoubleOrNull() ?: 0.0,
                returnTime = returnTime,
                dispatchType = selectedDispatchType,
                sceneLocationName = selectedLocation,
                sceneLocationValue = if (selectedLocation == "기타") locationDetailValue else null,
                painSymptoms = selectedPains,
                traumaSymptoms = selectedInjuries,
                otherSymptoms = selectedSymptoms,
                otherPainValue = if (selectedPains.contains("그 밖의 통증")) otherPainValue else null,
                otherSymptomValue = if (selectedSymptoms.contains("기타")) otherSymptomValue else null
                )
            )
        }
    }

    // 로딩 중일 때 표시
    if (dispatchState is DispatchApiState.Loading) {
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
        // 시간 정보 섹션
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeFieldRow(
                        label = "신고 일시",
                        value = reportDatetime,
                        onValueChange = { reportDatetime = it },
                        readOnly = true,
                        placeholder = "상황실에서 자동 입력",
                        modifier = Modifier.weight(1f)
                    )

                    TimeFieldRow(
                        label = "출동 시작",
                        value = departureTime,
                        onValueChange = { departureTime = it },
                        placeholder = "HH:mm",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeFieldRowWithButton(
                        label = "현장 도착",
                        value = arrivalSceneTime,
                        onValueChange = { arrivalSceneTime = it },
                        buttonText = "도착",
                        onButtonClick = { arrivalSceneTime = getCurrentTime() },
                        modifier = Modifier.weight(1f)
                    )

                    TimeFieldRowWithButton(
                        label = "현장 출발",
                        value = departureSceneTime,
                        onValueChange = { departureSceneTime = it },
                        buttonText = "출발",
                        onButtonClick = { departureSceneTime = getCurrentTime() },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeFieldRow(
                        label = "환자 접촉",
                        value = contactTime,
                        onValueChange = { contactTime = it },
                        placeholder = "HH:mm",
                        modifier = Modifier.weight(1f)
                    )

                    TimeFieldRow(
                        label = "병원 도착",
                        value = arrivalHospitalTime,
                        onValueChange = { arrivalHospitalTime = it },
                        placeholder = "HH:mm",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeFieldRow(
                        label = "거리 (km)",
                        value = distance,
                        onValueChange = { distance = it },
                        placeholder = "0",
                        modifier = Modifier.weight(1f)
                    )

                    TimeFieldRow(
                        label = "귀소 시각",
                        value = returnTime,
                        onValueChange = { returnTime = it },
                        placeholder = "HH:mm",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 출동유형 섹션
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "출동유형",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                SingleSelectButtonGroup(
                    options = listOf("정상", "오인", "거짓", "취소", "기타"),
                    selectedOption = selectedDispatchType,
                    onOptionSelected = { selectedDispatchType = it }
                )
            }
        }

        // 환자 발생 장소 섹션
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "환자 발생 장소",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                SingleSelectButtonGroup(
                    options = listOf(
                        "집", "집단거주시설", "도로", "도로외 교통지역", "오락/문화/교통시설",
                        "학교/교육시설", "운동시설", "상업시설", "의료 관련 시설", "공장/산업/건설시설",
                        "일차산업장", "바다/강/산/논밭", "기타"
                    ),
                    selectedOption = selectedLocation,
                    onOptionSelected = { selectedLocation = it },
                    columns = 5
                )

                if (selectedLocation == "기타") {
                    TextField(
                        value = locationDetailValue,
                        onValueChange = { locationDetailValue = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = {
                            Text("환자 발생 장소를 입력하세요", color = Color(0xFF999999), fontSize = 14.sp)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF2a2a2a),
                            unfocusedContainerColor = Color(0xFF2a2a2a),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color(0xFF3b7cff),
                            unfocusedIndicatorColor = Color(0xFF3a3a3a),
                            cursorColor = Color(0xFF3b7cff)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                        singleLine = true
                    )
                }
            }
        }

        // 환자 증상 - 통증
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "환자 증상",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "어떤 통증이 있습니까?",
                    color = Color.White,
                    fontSize = 14.sp
                )

                MultiSelectButtonGroup(
                    options = listOf("두통", "흉통", "복통", "요통", "분만진통", "그 밖의 통증"),
                    selectedOptions = selectedPains,
                    onOptionsChanged = { selectedPains = it },
                    columns = 5
                )

                if (selectedPains.contains("그 밖의 통증")) {
                    TextField(
                        value = otherPainValue,
                        onValueChange = { otherPainValue = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = {
                            Text("그 밖의 통증을 입력하세요", color = Color(0xFF999999), fontSize = 14.sp)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF2a2a2a),
                            unfocusedContainerColor = Color(0xFF2a2a2a),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color(0xFF3b7cff),
                            unfocusedIndicatorColor = Color(0xFF3a3a3a),
                            cursorColor = Color(0xFF3b7cff)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                        singleLine = true
                    )
                }
            }
        }

        // 환자 증상 - 외상
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "어떤 외상이 있습니까?",
                    color = Color.White,
                    fontSize = 14.sp
                )

                MultiSelectButtonGroup(
                    options = listOf("골절", "탈구", "삠", "열상", "찰과상", "타박상", "절단", "압궤손상", "화상"),
                    selectedOptions = selectedInjuries,
                    onOptionsChanged = { selectedInjuries = it },
                    columns = 4
                )
            }
        }

        // 환자 증상 - 그 외 증상
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "그 외 증상",
                    color = Color.White,
                    fontSize = 14.sp
                )

                MultiSelectButtonGroup(
                    options = listOf(
                        "의식장애", "기도이물", "기침", "호흡곤란", "호흡정지", "두근거림",
                        "가슴불편감", "심정지", "경련/발작", "실신", "오심", "구토",
                        "설사", "변비", "배뇨장애", "객혈", "토혈", "혈변",
                        "비출혈", "질출혈", "그 밖의 출혈", "고열", "저체온증", "어지러움",
                        "마비", "전신쇠약", "정신장애", "그 밖의 이물질", "기타"
                    ),
                    selectedOptions = selectedSymptoms,
                    onOptionsChanged = { selectedSymptoms = it },
                    columns = 6
                )

                if (selectedSymptoms.contains("기타")) {
                    TextField(
                        value = otherSymptomValue,
                        onValueChange = { otherSymptomValue = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        placeholder = {
                            Text("기타 증상을 입력하세요", color = Color(0xFF999999), fontSize = 14.sp)
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF2a2a2a),
                            unfocusedContainerColor = Color(0xFF2a2a2a),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color(0xFF3b7cff),
                            unfocusedIndicatorColor = Color(0xFF3a3a3a),
                            cursorColor = Color(0xFF3b7cff)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                        singleLine = true
                    )
                }
            }
        }
    }
}

// 시간 필드 (라벨 + 입력 필드)
@Composable
private fun TimeFieldRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    placeholder: String = ""
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            readOnly = readOnly,
            placeholder = {
                Text(
                    text = placeholder,
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
                disabledTextColor = Color(0xFF999999),
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

// 시간 필드 + 버튼
@Composable
private fun TimeFieldRowWithButton(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    buttonText: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "HH:mm",
                        color = Color(0xFF999999),
                        fontSize = 14.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color(0xFF3a3a3a),
                    unfocusedIndicatorColor = Color(0xFF3a3a3a),
                    cursorColor = Color(0xFF3b7cff)
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 14.sp,
                    color = Color.White
                ),
                singleLine = true
            )

            Button(
                onClick = onButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3b7cff),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = buttonText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// 단일 선택 버튼 그룹
@Composable
private fun SingleSelectButtonGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 5
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
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// 다중 선택 버튼 그룹
@Composable
private fun MultiSelectButtonGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionsChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 5
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
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(columns - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// 선택 버튼
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
            maxLines = 1
        )
    }
}

// 유틸리티: 현재 시간 가져오기
private fun getCurrentTime(): String {
    val currentTime = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern("HH:mm")
    return currentTime.format(formatter)
}
