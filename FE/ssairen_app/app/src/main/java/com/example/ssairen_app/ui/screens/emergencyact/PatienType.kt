// PatientType.kt
package com.example.ssairen_app.ui.screens.emergencyact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign  // ✅ 추가
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ssairen_app.ui.components.MainButton
import com.example.ssairen_app.viewmodel.ActivityLogData
import com.example.ssairen_app.viewmodel.LogViewModel
import com.example.ssairen_app.viewmodel.PatienTypeData

@Composable
fun PatientType(
    viewModel: LogViewModel,
    data: ActivityLogData
) {
    // ✅ ViewModel 데이터로 초기화
    var hasMedicalHistory by remember { mutableStateOf(data.patienType.hasMedicalHistory) }
    var medicalHistoryList by remember { mutableStateOf(data.patienType.medicalHistoryList) }
    var mainType by remember { mutableStateOf(data.patienType.mainType) }
    var crimeOption by remember { mutableStateOf(data.patienType.crimeOption) }
    var subType by remember { mutableStateOf(data.patienType.subType) }
    var accidentVictimType by remember { mutableStateOf(data.patienType.accidentVictimType) }
    var etcType by remember { mutableStateOf(data.patienType.etcType) }

    // ✅ 자동 저장 함수
    fun saveData() {
        val patienTypeData = PatienTypeData(
            hasMedicalHistory = hasMedicalHistory,
            medicalHistoryList = medicalHistoryList,
            mainType = mainType,
            crimeOption = crimeOption,
            subType = subType,
            accidentVictimType = accidentVictimType,
            etcType = etcType
        )
        viewModel.updatePatienType(patienTypeData)
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
            text = "세부항목-환자발생유형",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // ==========================================
        // 1. 병력 유무
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2a2a2a)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "병력 유무",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MainButton(
                        onClick = {
                            hasMedicalHistory = "있음"
                            saveData()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        backgroundColor = if (hasMedicalHistory == "있음")
                            Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                        cornerRadius = 6.dp
                    ) {
                        Text(text = "있음", fontSize = 14.sp)
                    }
                    MainButton(
                        onClick = {
                            hasMedicalHistory = "없음"
                            medicalHistoryList = setOf() // 없음 선택 시 병력 초기화
                            saveData()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        backgroundColor = if (hasMedicalHistory == "없음")
                            Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                        cornerRadius = 6.dp
                    ) {
                        Text(text = "없음", fontSize = 14.sp)
                    }
                    MainButton(
                        onClick = {
                            hasMedicalHistory = "미상"
                            medicalHistoryList = setOf() // 미상 선택 시 병력 초기화
                            saveData()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        backgroundColor = if (hasMedicalHistory == "미상")
                            Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                        cornerRadius = 6.dp
                    ) {
                        Text(text = "미상", fontSize = 14.sp)
                    }
                }

                // ✅ 병력 종류 (병력이 "있음"일 때만 표시)
                if (hasMedicalHistory == "있음") {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "병력 종류 (카테고리 새로 입력)",
                        color = Color(0xFF999999),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // 1행: 고혈압, 당뇨, 뇌혈관질환, 심장질환, 폐질환
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("고혈압", "당뇨", "뇌혈관질환", "심장질환", "폐질환").forEach { item ->
                            MainButton(
                                onClick = {
                                    medicalHistoryList = if (medicalHistoryList.contains(item)) {
                                        medicalHistoryList - item
                                    } else {
                                        medicalHistoryList + item
                                    }
                                    saveData()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (medicalHistoryList.contains(item))
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = item, fontSize = 12.sp)
                            }
                        }
                    }

                    // 2행: 결핵, 간염, 간경화, 알레르기, 암
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("결핵", "간염", "간경화", "알레르기", "암").forEach { item ->
                            MainButton(
                                onClick = {
                                    medicalHistoryList = if (medicalHistoryList.contains(item)) {
                                        medicalHistoryList - item
                                    } else {
                                        medicalHistoryList + item
                                    }
                                    saveData()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (medicalHistoryList.contains(item))
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = item, fontSize = 12.sp)
                            }
                        }
                    }

                    // 3행: 신부전, 감염병, 기타
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("신부전", "감염병", "기타").forEach { item ->
                            MainButton(
                                onClick = {
                                    medicalHistoryList = if (medicalHistoryList.contains(item)) {
                                        medicalHistoryList - item
                                    } else {
                                        medicalHistoryList + item
                                    }
                                    saveData()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (medicalHistoryList.contains(item))
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = item, fontSize = 12.sp)
                            }
                        }
                        // 빈 공간 채우기
                        Spacer(modifier = Modifier.weight(2f))
                    }
                }
            }
        }

        // ==========================================
        // 2. 환자 발생 유형을 제대로주세요
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF2a2a2a)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "환자 발생 유형을 제대로주세요",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MainButton(
                        onClick = {
                            mainType = "질병"
                            crimeOption = ""
                            subType = ""
                            accidentVictimType = ""
                            etcType = ""
                            saveData()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        backgroundColor = if (mainType == "질병")
                            Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                        cornerRadius = 6.dp
                    ) {
                        Text(text = "질병", fontSize = 14.sp)
                    }
                    MainButton(
                        onClick = {
                            mainType = "질병 외"
                            etcType = ""
                            saveData()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        backgroundColor = if (mainType == "질병 외")
                            Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                        cornerRadius = 6.dp
                    ) {
                        Text(text = "질병 외", fontSize = 14.sp)
                    }
                    MainButton(
                        onClick = {
                            mainType = "기타"
                            crimeOption = ""
                            subType = ""
                            accidentVictimType = ""
                            saveData()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp),
                        backgroundColor = if (mainType == "기타")
                            Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                        cornerRadius = 6.dp
                    ) {
                        Text(text = "기타", fontSize = 14.sp)
                    }
                }
            }
        }

        // ==========================================
        // 3. 질병 외 선택 시
        // ==========================================
        if (mainType == "질병 외") {
            // 범죄가 의심입니까?
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2a2a2a)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "범죄가 의심입니까?",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("경찰통보", "경찰입회", "긴급이송", "관련기관 통보").forEach { item ->
                            MainButton(
                                onClick = {
                                    crimeOption = item
                                    saveData()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (crimeOption == item)
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = item, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 교통사고 / 그 외 외상 / 비외상성 손상
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2a2a2a)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MainButton(
                            onClick = {
                                subType = "교통사고"
                                saveData()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            backgroundColor = if (subType == "교통사고")
                                Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                            cornerRadius = 6.dp
                        ) {
                            Text(text = "교통사고", fontSize = 14.sp)
                        }
                        MainButton(
                            onClick = {
                                subType = "그 외 외상"
                                accidentVictimType = "" // 다른 타입 선택 시 초기화
                                saveData()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            backgroundColor = if (subType == "그 외 외상")
                                Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                            cornerRadius = 6.dp
                        ) {
                            Text(text = "그 외 외상", fontSize = 14.sp)
                        }
                        MainButton(
                            onClick = {
                                subType = "비외상성 손상"
                                accidentVictimType = "" // 다른 타입 선택 시 초기화
                                saveData()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            backgroundColor = if (subType == "비외상성 손상")
                                Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                            cornerRadius = 6.dp
                        ) {
                            Text(text = "비외상성 손상", fontSize = 14.sp)
                        }
                    }
                }
            }

            // ✅ 교통사고 선택 시
            if (subType == "교통사고") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2a2a2a)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "교통사고의 사상자가 있습니까?",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // 1행: 운전자, 동승자, 보행자, 자전거, 오토바이
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("운전자", "동승자", "보행자", "자전거", "오토바이").forEach { item ->
                                MainButton(
                                    onClick = {
                                        accidentVictimType = item
                                        saveData()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    backgroundColor = if (accidentVictimType == item)
                                        Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                    cornerRadius = 6.dp
                                ) {
                                    Text(text = item, fontSize = 12.sp)
                                }
                            }
                        }

                        // 2행: 개인형 이동장치, 그 밖의 탈 것, 미상
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("개인형 이동장치", "그 밖의 탈 것", "미상").forEach { item ->
                                MainButton(
                                    onClick = {
                                        accidentVictimType = item
                                        saveData()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    backgroundColor = if (accidentVictimType == item)
                                        Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                    cornerRadius = 6.dp
                                ) {
                                    Text(text = item, fontSize = 12.sp)
                                }
                            }
                            // 빈 공간 채우기
                            Spacer(modifier = Modifier.weight(2f))
                        }
                    }
                }
            }

            // ✅ 그 외 외상 선택 시
            if (subType == "그 외 외상") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2a2a2a)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "그 외 외상을 선택하세요.",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // 1행: 낙상, 추락, 관통상, 기계, 농기계, 그 밖의 둔상
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("낙상", "추락", "관통상", "기계", "농기계", "그 밖의 둔상").forEach { item ->
                                MainButton(
                                    onClick = {
                                        accidentVictimType = item
                                        saveData()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    backgroundColor = if (accidentVictimType == item)
                                        Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                    cornerRadius = 6.dp
                                ) {
                                    Text(text = item, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ✅ 비외상성 손상 선택 시
            if (subType == "비외상성 손상") {
                // 1. 호흡위험이 있었나요?
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2a2a2a)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "호흡위험이 있었나요?",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("익수", "외력에 의한 압박", "이물질에 의한 기도막힘").forEach { item ->
                                MainButton(
                                    onClick = {
                                        accidentVictimType = item
                                        saveData()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    backgroundColor = if (accidentVictimType == item)
                                        Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                    cornerRadius = 6.dp
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 11.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. 화상이 있었나요?
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2a2a2a)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "화상이 있었나요?",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("화염", "고온체", "전기", "물").forEach { item ->
                                MainButton(
                                    onClick = {
                                        accidentVictimType = item
                                        saveData()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    backgroundColor = if (accidentVictimType == item)
                                        Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                    cornerRadius = 6.dp
                                ) {
                                    Text(text = item, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // 3. 그 외 발생 유형을 선택해주세요
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF2a2a2a)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "그 외 발생 유형을 선택해주세요.",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // 1행: 연기흡입, 중독, 화학물질, 동물/곤충
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("연기흡입", "중독", "화학물질", "동물/곤충").forEach { item ->
                                MainButton(
                                    onClick = {
                                        accidentVictimType = item
                                        saveData()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    backgroundColor = if (accidentVictimType == item)
                                        Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                    cornerRadius = 6.dp
                                ) {
                                    Text(text = item, fontSize = 12.sp)
                                }
                            }
                        }

                        // 2행: 온열손상, 한랭손상, 성폭행, 상해
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("온열손상", "한랭손상", "성폭행", "상해").forEach { item ->
                                MainButton(
                                    onClick = {
                                        accidentVictimType = item
                                        saveData()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp),
                                    backgroundColor = if (accidentVictimType == item)
                                        Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                    cornerRadius = 6.dp
                                ) {
                                    Text(text = item, fontSize = 12.sp)
                                }
                            }
                        }

                        // 3행: 기타 (입력 가능)
                        var etcInput by remember { mutableStateOf("") }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            MainButton(
                                onClick = {
                                    accidentVictimType = "기타: $etcInput"
                                    saveData()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (accidentVictimType.startsWith("기타"))
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = "기타", fontSize = 12.sp)
                            }

                            // 기타 입력 필드
                            OutlinedTextField(
                                value = etcInput,
                                onValueChange = {
                                    etcInput = it
                                    if (accidentVictimType.startsWith("기타")) {
                                        accidentVictimType = "기타: $it"
                                        saveData()
                                    }
                                },
                                modifier = Modifier
                                    .weight(3f)
                                    .height(36.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF3a3a3a),
                                    unfocusedContainerColor = Color(0xFF3a3a3a),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.White,
                                    focusedBorderColor = Color(0xFF3b7cff),
                                    unfocusedBorderColor = Color(0xFF4a4a4a)
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 12.sp
                                ),
                                singleLine = true,
                                placeholder = {
                                    Text(
                                        text = "직접 입력",
                                        color = Color(0xFF666666),
                                        fontSize = 12.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 4. 기타 선택 시
        // ==========================================
        if (mainType == "기타") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2a2a2a)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("자연재해", "임신분만", "신생아", "단순구조", "기타").forEach { item ->
                            MainButton(
                                onClick = {
                                    etcType = item
                                    saveData()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                backgroundColor = if (etcType == item)
                                    Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                                cornerRadius = 6.dp
                            ) {
                                Text(text = item, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}