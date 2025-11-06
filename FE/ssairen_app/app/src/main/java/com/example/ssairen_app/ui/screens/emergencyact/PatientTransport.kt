package com.example.ssairen_app.ui.screens.emergencyact

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==========================================
// 환자이송 섹션 메인 화면
// ==========================================
@Composable
fun PatientTransport(
    modifier: Modifier = Modifier
) {
    // ==== 1차 이송 상태 관리 ====
    var firstInstitutionName by remember { mutableStateOf("") }
    var firstArrivalTime by remember { mutableStateOf("00:00:00") }
    var firstDistance by remember { mutableStateOf("10") }
    var selectedFirstMedicalSelector by remember { mutableStateOf("") }
    var selectedFirstRetransportReason by remember { mutableStateOf("") }
    var selectedFirstRetransportReasonOther by remember { mutableStateOf("") }
    var selectedFirstPatientReceiver by remember { mutableStateOf("") }

    // ==== 2차 이송 상태 관리 ====
    var secondInstitutionName by remember { mutableStateOf("") }
    var secondArrivalTime by remember { mutableStateOf("00:00:00") }
    var secondDistance by remember { mutableStateOf("10") }
    var selectedSecondMedicalSelector by remember { mutableStateOf("") }
    var selectedSecondRetransportReason by remember { mutableStateOf("") }
    var selectedSecondRetransportReasonOther by remember { mutableStateOf("") }
    var selectedSecondPatientReceiver by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ==== 1차/2차 이송(연계) 기관명 (가로 배치) ====
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1차 이송
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1차\n이송(연계) 기관명",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    TextField(
                        value = firstInstitutionName,
                        onValueChange = { firstInstitutionName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        placeholder = {
                            Text(
                                text = "",
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

                // 2차 이송
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "2차\n이송(연계) 기관명",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    TextField(
                        value = secondInstitutionName,
                        onValueChange = { secondInstitutionName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        placeholder = {
                            Text(
                                text = "",
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

        // ==== 도착시간 + 거리(km) (가로 배치) ====
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1차 도착시간 + 거리
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 도착시간
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "도착시간",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            TextField(
                                value = firstArrivalTime,
                                onValueChange = { firstArrivalTime = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
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
                        }

                        // 거리(km)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "거리(km)",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            TextField(
                                value = firstDistance,
                                onValueChange = { firstDistance = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                placeholder = {
                                    Text(
                                        text = "10",
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

                // 2차 도착시간 + 거리
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 도착시간
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "도착시간",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            TextField(
                                value = secondArrivalTime,
                                onValueChange = { secondArrivalTime = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
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
                        }

                        // 거리(km)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "거리(km)",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            TextField(
                                value = secondDistance,
                                onValueChange = { secondDistance = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                placeholder = {
                                    Text(
                                        text = "10",
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
            }
        }

        // ==== 의료기관 선정자 등 (가로 배치) ====
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1차 의료기관 선정자
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "의료기관 선정자 등",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    CompactSingleSelectButtonGroup(
                        options = listOf("구급대", "119 상황실", "구급상황 센터", "환자/보호자", "병원 수용 곤란 등", "기타 서술"),
                        selectedOption = selectedFirstMedicalSelector,
                        onOptionSelected = { selectedFirstMedicalSelector = it },
                        columns = 3
                    )
                }

                // 2차 의료기관 선정자
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "의료기관 선정자 등",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    CompactSingleSelectButtonGroup(
                        options = listOf("구급대", "119 상황실", "구급상황 센터", "환자/보호자", "병원 수용 곤란 등", "기타 서술"),
                        selectedOption = selectedSecondMedicalSelector,
                        onOptionSelected = { selectedSecondMedicalSelector = it },
                        columns = 3
                    )
                }
            }
        }

        // ==== 재이송 사유 - 병상부족 (가로 배치) ====
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1차 재이송 사유
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "재이송 사유",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "병상부족",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    CompactSingleSelectButtonGroup(
                        options = listOf("응급실", "수술실", "입원실", "중환자실"),
                        selectedOption = selectedFirstRetransportReason,
                        onOptionSelected = { selectedFirstRetransportReason = it },
                        columns = 4
                    )
                }

                // 2차 재이송 사유
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "재이송 사유",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "병상부족",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    CompactSingleSelectButtonGroup(
                        options = listOf("응급실", "수술실", "입원실", "중환자실"),
                        selectedOption = selectedSecondRetransportReason,
                        onOptionSelected = { selectedSecondRetransportReason = it },
                        columns = 4
                    )
                }
            }
        }

        // ==== 재이송 사유 - 이외 (가로 배치) ====
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1차 이외
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "이외",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    CompactSingleSelectButtonGroup(
                        options = listOf("전문의 부재", "환자/보호자의 변심", "의료장비 고장", "1차 응급처치", "주취자 등", "기타 서술"),
                        selectedOption = selectedFirstRetransportReasonOther,
                        onOptionSelected = { selectedFirstRetransportReasonOther = it },
                        columns = 3
                    )
                }

                // 2차 이외
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "이외",
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    CompactSingleSelectButtonGroup(
                        options = listOf("전문의 부재", "환자/보호자의 변심", "의료장비 고장", "1차 응급처치", "주취자 등", "기타 서술"),
                        selectedOption = selectedSecondRetransportReasonOther,
                        onOptionSelected = { selectedSecondRetransportReasonOther = it },
                        columns = 3
                    )
                }
            }
        }

        // ==== 환자 인수자 (가로 배치) ====
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1차 환자 인수자
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "환자 인수자",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    CompactSingleSelectButtonGroup(
                        options = listOf("의사", "간호사", "응급구조사", "기타"),
                        selectedOption = selectedFirstPatientReceiver,
                        onOptionSelected = { selectedFirstPatientReceiver = it },
                        columns = 4
                    )
                }

                // 2차 환자 인수자
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "환자 인수자",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )

                    CompactSingleSelectButtonGroup(
                        options = listOf("의사", "간호사", "응급구조사", "기타"),
                        selectedOption = selectedSecondPatientReceiver,
                        onOptionSelected = { selectedSecondPatientReceiver = it },
                        columns = 4
                    )
                }
            }
        }
    }
}

// ==========================================
// 콤팩트 단일 선택 버튼 그룹 (작은 버튼용)
// ==========================================
@Composable
private fun CompactSingleSelectButtonGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 4
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.chunked(columns).forEach { rowOptions ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowOptions.forEach { option ->
                    CompactSelectButton(
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
// 콤팩트 다중 선택 버튼 그룹 (작은 버튼용)
// ==========================================
@Composable
private fun CompactMultiSelectButtonGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionsChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 3
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.chunked(columns).forEach { rowOptions ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowOptions.forEach { option ->
                    val isSelected = option in selectedOptions
                    CompactSelectButton(
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
// 콤팩트 선택 버튼 (작은 버튼용)
// ==========================================
@Composable
private fun CompactSelectButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF3b7cff) else Color(0xFF3a3a3a),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(4.dp),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF4a4a4a))
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
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
    widthDp = 800
)
@Composable
private fun PatientTransportPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1a1a1a)
    ) {
        PatientTransport()
    }
}
