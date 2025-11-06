package com.example.ssairen_app.ui.screens.emergencyact

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ==========================================
// 구급출동 섹션 메인 화면
// ==========================================
@Composable
fun DispatchSection(
    modifier: Modifier = Modifier,
    initialReportTime: String = "상황실",
    initialDispatchTime: String = "상황실"
) {
    // ==== 상태 관리 ====
    var arrivalTime by remember { mutableStateOf("00:00:00") }
    var departureTime by remember { mutableStateOf("00:00:00") }
    var contactTime by remember { mutableStateOf("00:00:00") }
    var hospitalArrivalTime by remember { mutableStateOf("00:00:00") }
    var returnTime by remember { mutableStateOf("00:00:00") }
    var distance by remember { mutableStateOf("1 km") }

    var selectedDispatchType by remember { mutableStateOf("정상") }
    var selectedLocation by remember { mutableStateOf("집") }

    var selectedPains by remember { mutableStateOf(setOf<String>()) }
    var selectedInjuries by remember { mutableStateOf(setOf<String>()) }
    var selectedSymptoms by remember { mutableStateOf(setOf<String>()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ==== 1. 시간 정보 섹션 ====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // 신고 일시 / 출동 시작 (읽기 전용)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeFieldRow(
                        label = "신고 일시",
                        value = initialReportTime,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = "상황실",
                        modifier = Modifier.weight(1f)
                    )

                    TimeFieldRow(
                        label = "출동 시작",
                        value = initialDispatchTime,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = "상황실",
                        modifier = Modifier.weight(1f)
                    )
                }

                // 현장 도착 / 현장 출발
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeFieldRowWithButton(
                        label = "현장 도착",
                        value = arrivalTime,
                        onValueChange = { arrivalTime = it },
                        buttonText = "도착",
                        onButtonClick = {
                            arrivalTime = getCurrentTime()
                        },
                        modifier = Modifier.weight(1f)
                    )

                    TimeFieldRowWithButton(
                        label = "현장 출발",
                        value = departureTime,
                        onValueChange = { departureTime = it },
                        buttonText = "출발",
                        onButtonClick = {
                            departureTime = getCurrentTime()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 환자 접촉 / 병원 도착
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeFieldRow(
                        label = "환자 접촉",
                        value = contactTime,
                        onValueChange = { contactTime = it },
                        placeholder = "00:00:00",
                        modifier = Modifier.weight(1f)
                    )

                    TimeFieldRow(
                        label = "병원 도착",
                        value = hospitalArrivalTime,
                        onValueChange = { hospitalArrivalTime = it },
                        placeholder = "00:00:00",
                        modifier = Modifier.weight(1f)
                    )
                }

                // 거리 / 귀소 시각
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeFieldRow(
                        label = "거리",
                        value = distance,
                        onValueChange = { distance = it },
                        placeholder = "0 km",
                        modifier = Modifier.weight(1f)
                    )

                    TimeFieldRow(
                        label = "귀소 시각",
                        value = returnTime,
                        onValueChange = { returnTime = it },
                        placeholder = "00:00:00",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ==== 2. 출동유형 섹션 ====
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

        // ==== 3. 환자 발생 장소 섹션 ====
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
            }
        }

        // ==== 4. 환자 증상 - 통증 ====
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
            }
        }

        // ==== 5. 환자 증상 - 외상 ====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "어떤 외상이 있습니까?",
                    color = Color.White,
                    fontSize = 14.sp
                )

                MultiSelectButtonGroup(
                    options = listOf("골절", "탈구", "뼈", "열상", "찰과상", "타박상", "절단", "압궤손상", "화상"),
                    selectedOptions = selectedInjuries,
                    onOptionsChanged = { selectedInjuries = it },
                    columns = 4
                )
            }
        }

        // ==== 6. 환자 증상 - 그 외 증상 ====
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
            }
        }
    }
}

// ==========================================
// 시간 필드 (라벨 + 입력 필드)
// ==========================================
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

// ==========================================
// 시간 필드 + 버튼 (라벨 + 입력 필드 + 액션 버튼)
// ==========================================
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
                        text = "00:00:00",
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

// ==========================================
// 단일 선택 버튼 그룹
// ==========================================
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

// ==========================================
// 유틸리티: 현재 시간 가져오기
// ==========================================
private fun getCurrentTime(): String {
    val currentTime = LocalTime.now()
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    return currentTime.format(formatter)
}

// ==========================================
// Preview
// ==========================================
@Preview(
    showBackground = true,
    backgroundColor = 0xFF1a1a1a,
    heightDp = 2000,
    widthDp = 400
)
@Composable
private fun DispatchSectionPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1a1a1a)
    ) {
        DispatchSection(
            initialReportTime = "2025-01-05 07:52",
            initialDispatchTime = "2025-01-05 07:55"
        )
    }
}
