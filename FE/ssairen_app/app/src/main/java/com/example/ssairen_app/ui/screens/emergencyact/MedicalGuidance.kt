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

// ==========================================
// 의료지도 섹션 메인 화면
// ==========================================
@Composable
fun MedicalGuidance(
    modifier: Modifier = Modifier
) {
    // ==== 상태 관리 ====
    var selectedConnection by remember { mutableStateOf("연결") }
    var requestTime by remember { mutableStateOf("00:00") }
    var selectedRequestMethod by remember { mutableStateOf("일반 전화") }
    var selectedInstitution by remember { mutableStateOf("소방") }
    var doctorName by remember { mutableStateOf("") }
    var selectedEmergencyCare by remember { mutableStateOf(setOf("airway")) }
    var selectedMedication by remember { mutableStateOf(setOf<String>()) }
    var selectedHospitalSelection by remember { mutableStateOf("환자평가") }

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
                        columns = 2
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
                    options = listOf("일반 전화", "휴대전화 음성", "휴대전화 전화", "무전기", "기타 서술"),
                    selectedOption = selectedRequestMethod,
                    onOptionSelected = { selectedRequestMethod = it },
                    columns = 5
                )
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
                        options = listOf("소방", "병원", "기타 서술"),
                        selectedOption = selectedInstitution,
                        onOptionSelected = { selectedInstitution = it },
                        columns = 3
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
                }
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
                        "airway", "Intubation", "Supraglottic airway", "ECG", "AED",
                        "CPR", "IV", "BVM", "산소투여", "고정",
                        "상처처치", "혈당체크", "보온", "기타서술"
                    ),
                    selectedOptions = selectedEmergencyCare,
                    onOptionsChanged = { selectedEmergencyCare = it },
                    columns = 5
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
                        "N/S", "D/W", "NTG", "기관지확장제", "에피네프린", "아미오다론", "기타 서술"
                    ),
                    selectedOptions = selectedMedication,
                    onOptionsChanged = { selectedMedication = it },
                    columns = 7
                )
            }
        }

        // ==== 6. 병원선정 ====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "병원선정",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                SingleSelectButtonGroup(
                    options = listOf("환자평가", "CPR유보중단", "이송거절", "이송거부", "기타"),
                    selectedOption = selectedHospitalSelection,
                    onOptionSelected = { selectedHospitalSelection = it },
                    columns = 5
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
// Preview
// ==========================================
@Preview(
    showBackground = true,
    backgroundColor = 0xFF1a1a1a,
    heightDp = 2000,
    widthDp = 400
)
@Composable
private fun MedicalGuidancePreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1a1a1a)
    ) {
        MedicalGuidance()
    }
}
