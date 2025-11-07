// FirstAid.kt
package com.example.ssairen_app.ui.screens.emergencyact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ssairen_app.ui.components.MainButton
import com.example.ssairen_app.viewmodel.ActivityLogData
import com.example.ssairen_app.viewmodel.FirstAidData
import com.example.ssairen_app.viewmodel.LogViewModel

@Composable
fun FirstAid(
    viewModel: LogViewModel,
    data: ActivityLogData
) {
    // ✅ 응급 처치 (기도 확보)
    var airwayJawThrust by remember { mutableStateOf(data.firstAid.airwayJawThrust) }
    var airwayHeadTilt by remember { mutableStateOf(data.firstAid.airwayHeadTilt) }
    var airwayNPA by remember { mutableStateOf(data.firstAid.airwayNPA) }
    var airwayOPA by remember { mutableStateOf(data.firstAid.airwayOPA) }
    var airwayIntubation by remember { mutableStateOf(data.firstAid.airwayIntubation) }
    var airwaySupraglottic by remember { mutableStateOf(data.firstAid.airwaySupraglottic) }

    // 산소 투여
    var oxygenMask by remember { mutableStateOf(data.firstAid.oxygenMask) }
    var oxygenNasal by remember { mutableStateOf(data.firstAid.oxygenNasal) }
    var oxygenBVM by remember { mutableStateOf(data.firstAid.oxygenBVM) }
    var oxygenVentilator by remember { mutableStateOf(data.firstAid.oxygenVentilator) }
    var oxygenSuction by remember { mutableStateOf(data.firstAid.oxygenSuction) }

    // CPR
    var cprPerformed by remember { mutableStateOf(data.firstAid.cprPerformed) }
    var cprManual by remember { mutableStateOf(data.firstAid.cprManual) }
    var cprDNR by remember { mutableStateOf(data.firstAid.cprDNR) }
    var cprTermination by remember { mutableStateOf(data.firstAid.cprTermination) }

    // AED
    var aedShock by remember { mutableStateOf(data.firstAid.aedShock) }
    var aedMonitoring by remember { mutableStateOf(data.firstAid.aedMonitoring) }
    var aedApplicationOnly by remember { mutableStateOf(data.firstAid.aedApplicationOnly) }

    // 처치
    var treatmentOxygenSaturation by remember { mutableStateOf(data.firstAid.treatmentOxygenSaturation) }
    var treatmentShockPrevention by remember { mutableStateOf(data.firstAid.treatmentShockPrevention) }
    var treatmentInjection by remember { mutableStateOf(data.firstAid.treatmentInjection) }

    // 고정
    var immobilizationSpinal by remember { mutableStateOf(data.firstAid.immobilizationSpinal) }
    var immobilizationCSpine by remember { mutableStateOf(data.firstAid.immobilizationCSpine) }
    var immobilizationSplint by remember { mutableStateOf(data.firstAid.immobilizationSplint) }
    var immobilizationOther by remember { mutableStateOf(data.firstAid.immobilizationOther) }

    // 상처 처치
    var woundDressing by remember { mutableStateOf(data.firstAid.woundDressing) }
    var woundBandage by remember { mutableStateOf(data.firstAid.woundBandage) }
    var woundHemostasis by remember { mutableStateOf(data.firstAid.woundHemostasis) }
    var woundParalysis by remember { mutableStateOf(data.firstAid.woundParalysis) }

    // ✅ 자동 저장 함수
    fun saveData() {
        val firstAidData = FirstAidData(
            airwayJawThrust = airwayJawThrust,
            airwayHeadTilt = airwayHeadTilt,
            airwayNPA = airwayNPA,
            airwayOPA = airwayOPA,
            airwayIntubation = airwayIntubation,
            airwaySupraglottic = airwaySupraglottic,
            oxygenMask = oxygenMask,
            oxygenNasal = oxygenNasal,
            oxygenBVM = oxygenBVM,
            oxygenVentilator = oxygenVentilator,
            oxygenSuction = oxygenSuction,
            cprPerformed = cprPerformed,
            cprManual = cprManual,
            cprDNR = cprDNR,
            cprTermination = cprTermination,
            aedShock = aedShock,
            aedMonitoring = aedMonitoring,
            aedApplicationOnly = aedApplicationOnly,
            treatmentOxygenSaturation = treatmentOxygenSaturation,
            treatmentShockPrevention = treatmentShockPrevention,
            treatmentInjection = treatmentInjection,
            immobilizationSpinal = immobilizationSpinal,
            immobilizationCSpine = immobilizationCSpine,
            immobilizationSplint = immobilizationSplint,
            immobilizationOther = immobilizationOther,
            woundDressing = woundDressing,
            woundBandage = woundBandage,
            woundHemostasis = woundHemostasis,
            woundParalysis = woundParalysis
        )
        viewModel.updateFirstAid(firstAidData)
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
            text = "세부항목-응급처치",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // ==========================================
        // 기도 확보
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
                    text = "기도 확보",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TreatmentButton("Jaw Thrust", airwayJawThrust) {
                        airwayJawThrust = it
                        saveData()
                    }
                    TreatmentButton("Head Tilt", airwayHeadTilt) {
                        airwayHeadTilt = it
                        saveData()
                    }
                    TreatmentButton("NPA", airwayNPA) {
                        airwayNPA = it
                        saveData()
                    }
                    TreatmentButton("OPA", airwayOPA) {
                        airwayOPA = it
                        saveData()
                    }
                    TreatmentButton("기도삽관", airwayIntubation) {
                        airwayIntubation = it
                        saveData()
                    }
                    TreatmentButton("성문상기도", airwaySupraglottic) {
                        airwaySupraglottic = it
                        saveData()
                    }
                }
            }
        }

        // ==========================================
        // 산소 투여
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
                    text = "산소 투여",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TreatmentButton("L/min", oxygenMask) {
                        oxygenMask = it
                        saveData()
                    }
                    TreatmentButton("마스크", oxygenNasal) {
                        oxygenNasal = it
                        saveData()
                    }
                    TreatmentButton("비강", oxygenBVM) {
                        oxygenBVM = it
                        saveData()
                    }
                    TreatmentButton("BVM", oxygenVentilator) {
                        oxygenVentilator = it
                        saveData()
                    }
                    TreatmentButton("산소포화도", oxygenSuction) {
                        oxygenSuction = it
                        saveData()
                    }
                    TreatmentButton("흡입기", false) { }
                    TreatmentButton("네블라이져", false) { }
                }
            }
        }

        // ==========================================
        // CPR
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
                    text = "CPR",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TreatmentButton("실시", cprPerformed) {
                        cprPerformed = it
                        saveData()
                    }
                    TreatmentButton("개방", cprManual) {
                        cprManual = it
                        saveData()
                    }
                    TreatmentButton("DNR", cprDNR) {
                        cprDNR = it
                        saveData()
                    }
                    TreatmentButton("중단", cprTermination) {
                        cprTermination = it
                        saveData()
                    }
                }
            }
        }

        // ==========================================
        // ECG / AED
        // ==========================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ECG
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2a2a2a)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ECG",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    MainButton(
                        onClick = { },
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        backgroundColor = Color(0xFF3a3a3a),
                        cornerRadius = 6.dp
                    ) {
                        Text("ECG", fontSize = 13.sp)
                    }
                }
            }

            // AED
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2a2a2a)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "AED",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TreatmentButton("Shock", aedShock, Modifier.weight(1f)) {
                            aedShock = it
                            saveData()
                        }
                        TreatmentButton("Monitoring", aedMonitoring, Modifier.weight(1f)) {
                            aedMonitoring = it
                            saveData()
                        }
                        TreatmentButton("기타 사용", aedApplicationOnly, Modifier.weight(1f)) {
                            aedApplicationOnly = it
                            saveData()
                        }
                    }
                }
            }
        }

        // ==========================================
        // 응급 처치
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
                    text = "응급 처치",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TreatmentButton("냉/온찜질", treatmentOxygenSaturation) {
                        treatmentOxygenSaturation = it
                        saveData()
                    }
                    TreatmentButton("쇼크방지", treatmentShockPrevention) {
                        treatmentShockPrevention = it
                        saveData()
                    }
                    TreatmentButton("약물투여", treatmentInjection) {
                        treatmentInjection = it
                        saveData()
                    }
                }
            }
        }

        // ==========================================
        // 고정
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
                    text = "고정",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TreatmentButton("척추", immobilizationSpinal) {
                        immobilizationSpinal = it
                        saveData()
                    }
                    TreatmentButton("목 보호", immobilizationCSpine) {
                        immobilizationCSpine = it
                        saveData()
                    }
                    TreatmentButton("부목", immobilizationSplint) {
                        immobilizationSplint = it
                        saveData()
                    }
                    TreatmentButton("마비", immobilizationOther) {
                        immobilizationOther = it
                        saveData()
                    }
                }
            }
        }

        // ==========================================
        // 상처 처치
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
                    text = "상처 처치",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TreatmentButton("드레싱", woundDressing) {
                        woundDressing = it
                        saveData()
                    }
                    TreatmentButton("붕대", woundBandage) {
                        woundBandage = it
                        saveData()
                    }
                    TreatmentButton("압박", woundHemostasis) {
                        woundHemostasis = it
                        saveData()
                    }
                    TreatmentButton("마비", woundParalysis) {
                        woundParalysis = it
                        saveData()
                    }
                }
            }
        }
    }
}

// ==========================================
// 처치 버튼 컴포넌트
// ==========================================
@Composable
private fun RowScope.TreatmentButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onToggle: (Boolean) -> Unit
) {
    MainButton(
        onClick = { onToggle(!isSelected) },
        modifier = modifier.height(40.dp),
        backgroundColor = if (isSelected) Color(0xFF3b7cff) else Color(0xFF3a3a3a),
        cornerRadius = 6.dp
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal
        )
    }
}