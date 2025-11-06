// PatientInfo.kt
package com.example.ssairen_app.ui.screens.emergencyact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField // ✅ import 추가
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle // ✅ import 추가
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign // ✅ import 추가
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ssairen_app.ui.components.MainButton
import com.example.ssairen_app.viewmodel.ActivityLogData
import com.example.ssairen_app.viewmodel.LogViewModel
import com.example.ssairen_app.viewmodel.PatientInfoData  // ✅ import 유지

@Composable
fun PatientInfo(
    viewModel: LogViewModel,
    data: ActivityLogData
) {
    // ✅ ViewModel 데이터로 초기화 (data.patientInfo 경로 사용)
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

    // ✅ 자동 저장 함수 (PatientInfoData 객체로 묶어서 전달)
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
                            saveData()  // ✅ 자동 저장
                        },
                        modifier = Modifier.weight(1f)
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
                                    selectedReportMethod = "일반전화"
                                    saveData()  // ✅ 자동 저장
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (selectedReportMethod == "일반전화")
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = "일반전화", fontSize = 12.sp)
                            }
                            MainButton(
                                onClick = {
                                    selectedReportMethod = "유선전화"
                                    saveData()  // ✅ 자동 저장
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
                                    selectedReportMethod = "기타"
                                    saveData()  // ✅ 자동 저장
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
                            saveData()  // ✅ 자동 저장
                        },
                        modifier = Modifier.weight(1f)
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
                                    selectedGender = "남성"
                                    saveData()  // ✅ 자동 저장
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
                                    selectedGender = "여성"
                                    saveData()  // ✅ 자동 저장
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

                // ✅ 생년월일 (년/월/일) + 나이 (세 고정)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 생년월일 (년, 월, 일)
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
                            // 년
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = birthYear,
                                        onValueChange = {
                                            birthYear = it
                                            saveData()  // ✅ 자동 저장
                                        },
                                        modifier = Modifier.padding(bottom = 4.dp),
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal,
                                            textAlign = TextAlign.End
                                        ),
                                        singleLine = true
                                    )
                                    Text(
                                        text = "년",
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

                            // 월
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = birthMonth,
                                        onValueChange = {
                                            birthMonth = it
                                            saveData()  // ✅ 자동 저장
                                        },
                                        modifier = Modifier.padding(bottom = 4.dp),
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal,
                                            textAlign = TextAlign.End
                                        ),
                                        singleLine = true
                                    )
                                    Text(
                                        text = "월",
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

                            // 일
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicTextField(
                                        value = birthDay,
                                        onValueChange = {
                                            birthDay = it
                                            saveData()  // ✅ 자동 저장
                                        },
                                        modifier = Modifier.padding(bottom = 4.dp),
                                        textStyle = TextStyle(
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Normal,
                                            textAlign = TextAlign.End
                                        ),
                                        singleLine = true
                                    )
                                    Text(
                                        text = "일",
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
                    }

                    // 나이 (세 고정)
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
                                    saveData()  // ✅ 자동 저장
                                },
                                modifier = Modifier.padding(bottom = 4.dp),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.End
                                ),
                                singleLine = true
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

                // 환자주소 (한 줄)
                UnderlineInputField(
                    label = "환자주소",
                    value = patientAddress,
                    onValueChange = {
                        patientAddress = it
                        saveData()  // ✅ 자동 저장
                    }
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
                            saveData()  // ✅ 자동 저장
                        },
                        modifier = Modifier.weight(1f)
                    )
                    UnderlineInputField(
                        label = "보호자 관계",
                        value = guardianRelation,
                        onValueChange = {
                            guardianRelation = it
                            saveData()  // ✅ 자동 저장
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                // 보호자 연락처
                UnderlineInputField(
                    label = "보호자 연락처",
                    value = guardianPhone,
                    onValueChange = {
                        guardianPhone = it
                        saveData()  // ✅ 자동 저장
                    }
                )
            }
        }
    }
}

// ==========================================
// 밑줄 스타일 입력 필드 컴포넌트 (오른쪽 정렬 기본값)
// ==========================================
@Composable
private fun UnderlineInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.End // ✅ 오른쪽 정렬 기본값
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
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
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

        // 밑줄
        HorizontalDivider(
            color = Color(0xFF4a4a4a),
            thickness = 1.dp
        )
    }
}