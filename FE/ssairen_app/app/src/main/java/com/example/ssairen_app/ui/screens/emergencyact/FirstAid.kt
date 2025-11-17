// FirstAid.kt
package com.example.ssairen_app.ui.screens.emergencyact

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
import com.example.ssairen_app.viewmodel.FirstAidData
import com.example.ssairen_app.viewmodel.FirstAidApiState
import com.example.ssairen_app.viewmodel.LogViewModel

@Composable
fun FirstAid(
    viewModel: LogViewModel,
    data: ActivityLogData,
    isReadOnly: Boolean = false,
    activityViewModel: ActivityViewModel = viewModel()
) {
    val firstAidState by activityViewModel.firstAidState.observeAsState(FirstAidApiState.Idle)
    val currentReportId by activityViewModel.currentEmergencyReportId.observeAsState()

    var isApiDataLoaded by remember { mutableStateOf(false) }

    var airwayJawThrust by remember { mutableStateOf(false) }
    var airwayHeadTilt by remember { mutableStateOf(false) }
    var airwayNPA by remember { mutableStateOf(false) }
    var airwayOPA by remember { mutableStateOf(false) }
    var airwayIntubation by remember { mutableStateOf(false) }
    var airwaySupraglottic by remember { mutableStateOf(false) }

    var oxygenLiterPerMinute by remember { mutableStateOf("") }
    var oxygenMask by remember { mutableStateOf(false) }
    var oxygenNasal by remember { mutableStateOf(false) }
    var oxygenBVM by remember { mutableStateOf(false) }
    var oxygenVentilator by remember { mutableStateOf(false) }
    var oxygenSuction by remember { mutableStateOf(false) }
    var oxygenNebulizer by remember { mutableStateOf(false) }

    var cprPerformed by remember { mutableStateOf(false) }
    var cprManual by remember { mutableStateOf(false) }
    var cprDNR by remember { mutableStateOf(false) }
    var cprTermination by remember { mutableStateOf(false) }

    var ecgUsed by remember { mutableStateOf(false) }

    var aedShock by remember { mutableStateOf(false) }
    var aedMonitoring by remember { mutableStateOf(false) }
    var aedApplicationOnly by remember { mutableStateOf(false) }

    var circulationIV by remember { mutableStateOf(false) }
    var circulationFluid by remember { mutableStateOf("") }
    var circulationDrug by remember { mutableStateOf(false) }

    var immobilizationCervical by remember { mutableStateOf(false) }
    var immobilizationSpinal by remember { mutableStateOf(false) }
    var immobilizationSplint by remember { mutableStateOf(false) }
    var immobilizationHead by remember { mutableStateOf(false) }

    var woundHemostasis by remember { mutableStateOf(false) }
    var woundDressing by remember { mutableStateOf(false) }
    var woundBandage by remember { mutableStateOf(false) }
    var woundHandProtection by remember { mutableStateOf(false) }
    var woundFootProtection by remember { mutableStateOf(false) }

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
            treatmentOxygenSaturation = false,
            treatmentShockPrevention = false,
            treatmentInjection = false,
            immobilizationSpinal = immobilizationSpinal,
            immobilizationCSpine = immobilizationCervical,
            immobilizationSplint = immobilizationSplint,
            immobilizationOther = false,
            woundDressing = woundDressing,
            woundBandage = woundBandage,
            woundHemostasis = woundHemostasis,
            woundParalysis = false
        )
        viewModel.updateFirstAid(firstAidData)
    }

    LaunchedEffect(data) {
        if (!isApiDataLoaded) {
            airwayJawThrust = data.firstAid.airwayJawThrust
            airwayHeadTilt = data.firstAid.airwayHeadTilt
            airwayNPA = data.firstAid.airwayNPA
            airwayOPA = data.firstAid.airwayOPA
            airwayIntubation = data.firstAid.airwayIntubation
            airwaySupraglottic = data.firstAid.airwaySupraglottic
            oxygenMask = data.firstAid.oxygenMask
            oxygenNasal = data.firstAid.oxygenNasal
            oxygenBVM = data.firstAid.oxygenBVM
            oxygenVentilator = data.firstAid.oxygenVentilator
            oxygenSuction = data.firstAid.oxygenSuction
            cprPerformed = data.firstAid.cprPerformed
            cprManual = data.firstAid.cprManual
            cprDNR = data.firstAid.cprDNR
            cprTermination = data.firstAid.cprTermination
            aedShock = data.firstAid.aedShock
            aedMonitoring = data.firstAid.aedMonitoring
            aedApplicationOnly = data.firstAid.aedApplicationOnly
            immobilizationSpinal = data.firstAid.immobilizationSpinal
            immobilizationCervical = data.firstAid.immobilizationCSpine
            immobilizationSplint = data.firstAid.immobilizationSplint
            woundDressing = data.firstAid.woundDressing
            woundBandage = data.firstAid.woundBandage
            woundHemostasis = data.firstAid.woundHemostasis
        }
    }

    LaunchedEffect(currentReportId) {
        currentReportId?.let { reportId ->
            android.util.Log.d("FirstAid", "📞 API 호출: getFirstAid($reportId)")
            activityViewModel.getFirstAid(reportId)
        }
    }

    LaunchedEffect(firstAidState) {
        when (val state = firstAidState) {
            is FirstAidApiState.Success -> {
                val treatment = state.firstAidResponse.data.data.treatment

                treatment.airwayManagement?.methods?.let { methods ->
                    airwayJawThrust = methods.any { it.contains("Jaw Thrust") || it.contains("하악거상") || it.contains("기도유지") }
                    airwayHeadTilt = methods.any { it.contains("Head Tilt") || it.contains("두부후굴") }
                    airwayNPA = methods.any { it.contains("NPA") || it.contains("비인두") }
                    airwayOPA = methods.any { it.contains("OPA") || it.contains("구인두") }
                    airwayIntubation = methods.any { it.contains("기도삽관") || it.contains("전문기도") }
                    airwaySupraglottic = methods.any { it.contains("성문상") }
                }

                treatment.oxygenTherapy?.let { oxygen ->
                    oxygenMask = false
                    oxygenNasal = false
                    oxygenBVM = false
                    oxygenVentilator = false

                    when (oxygen.device) {
                        "비재호흡마스크" -> oxygenMask = true
                        "비강캐뉼라" -> oxygenNasal = true
                        "백밸브마스크" -> oxygenBVM = true
                        "인공호흡기" -> oxygenVentilator = true
                    }
                }

                treatment.cpr?.let { cprStatus ->
                    cprPerformed = cprStatus.contains("실시")
                    cprManual = cprStatus.contains("개방") || cprStatus.contains("1회") || cprStatus.contains("다회")
                    cprDNR = cprStatus.contains("DNR")
                    cprTermination = cprStatus.contains("중단")
                }

                treatment.aed?.let { aed ->
                    aedShock = false
                    aedMonitoring = false

                    when (aed.type) {
                        "shock" -> aedShock = true
                        "monitoring" -> aedMonitoring = true
                    }
                }

                treatment.woundCare?.let { woundCare ->
                    woundDressing = woundCare.contains("드레싱") || woundCare.contains("소독")
                    woundBandage = woundCare.contains("붕대")
                    woundHemostasis = woundCare.contains("압박") || woundCare.contains("지혈")
                }

                treatment.fixed?.let { fixed ->
                    immobilizationSpinal = fixed.contains("척추")
                    immobilizationCervical = fixed.contains("목") || fixed.contains("경추")
                    immobilizationSplint = fixed.contains("부목")
                }

                isApiDataLoaded = true
                saveData()
            }
            is FirstAidApiState.Error -> {
                android.util.Log.e("FirstAid", "❌ API 오류: ${state.message}")
            }
            else -> { }
        }
    }

    if (firstAidState is FirstAidApiState.Loading) {
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
            // 기도 확보
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "기도 확보",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SelectButton("도수 조작", airwayJawThrust, Modifier.weight(1f), !isReadOnly) {
                        airwayJawThrust = it
                        saveData()
                    }
                    SelectButton("기도유지기", airwayHeadTilt, Modifier.weight(1f), !isReadOnly) {
                        airwayHeadTilt = it
                        saveData()
                    }
                    SelectButton("기도삽관", airwayNPA, Modifier.weight(1f), !isReadOnly) {
                        airwayNPA = it
                        saveData()
                    }
                    SelectButton("성문외기도유지기", airwayOPA, Modifier.weight(1f), !isReadOnly) {
                        airwayOPA = it
                        saveData()
                    }
                    SelectButton("흡인기", airwayIntubation, Modifier.weight(1f), !isReadOnly) {
                        airwayIntubation = it
                        saveData()
                    }
                    SelectButton("그 밖의 도수법", airwaySupraglottic, Modifier.weight(1f), !isReadOnly) {
                        airwaySupraglottic = it
                        saveData()
                    }
                }
            }

            // ==========================================
            // 산소 투여
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "산소 투여",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Column(modifier = Modifier.width(150.dp)) {
                    Text(
                        text = "L/min",
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    BasicTextField(
                        value = oxygenLiterPerMinute,
                        onValueChange = {
                            oxygenLiterPerMinute = it
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SelectButton("비관", oxygenMask, Modifier.weight(1f), !isReadOnly) {
                        oxygenMask = it
                        saveData()
                    }
                    SelectButton("안면마스크", oxygenNasal, Modifier.weight(1f), !isReadOnly) {
                        oxygenNasal = it
                        saveData()
                    }
                    SelectButton("비재호흡마스크", oxygenBVM, Modifier.weight(1f), !isReadOnly) {
                        oxygenBVM = it
                        saveData()
                    }
                    SelectButton("BVM", oxygenVentilator, Modifier.weight(1f), !isReadOnly) {
                        oxygenVentilator = it
                        saveData()
                    }
                    SelectButton("산소소생기", oxygenSuction, Modifier.weight(1f), !isReadOnly) {
                        oxygenSuction = it
                        saveData()
                    }
                    SelectButton("네뷸라이저", false, Modifier.weight(1f), !isReadOnly) { }
                }
            }

            // ==========================================
            // CPR
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "CPR",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SelectButton("실시", cprPerformed, Modifier.weight(1f), !isReadOnly) {
                        cprPerformed = it
                        saveData()
                    }
                    SelectButton("거부", cprManual, Modifier.weight(1f), !isReadOnly) {
                        cprManual = it
                        saveData()
                    }
                    SelectButton("DNR", cprDNR, Modifier.weight(1f), !isReadOnly) {
                        cprDNR = it
                        saveData()
                    }
                    SelectButton("유보", cprTermination, Modifier.weight(1f), !isReadOnly) {
                        cprTermination = it
                        saveData()
                    }
                }
            }

            // ==========================================
            // ECG
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ECG",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SelectButton("ECG", ecgUsed, Modifier.weight(1f), !isReadOnly) {
                        ecgUsed = it
                        saveData()
                    }
                    Spacer(modifier = Modifier.weight(6f))
                }
            }

            // ==========================================
            // AED
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "AED",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SelectButton("Shock", aedShock, Modifier.weight(1f), !isReadOnly) {
                        aedShock = it
                        saveData()
                    }
                    SelectButton("Monitoring", aedMonitoring, Modifier.weight(1f), !isReadOnly) {
                        aedMonitoring = it
                        saveData()
                    }
                    SelectButton("기타 사용", aedApplicationOnly, Modifier.weight(1f), !isReadOnly) {
                        aedApplicationOnly = it
                        saveData()
                    }
                }
            }

            // ==========================================
            // 순환 보조
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "순환 보조",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SelectButton("정맥로확보", circulationIV, Modifier.weight(1f), !isReadOnly) {
                        circulationIV = it
                        saveData()
                    }

                    // ⭐ 수액공급 입력 필드
                    BasicTextField(
                        value = circulationFluid,
                        onValueChange = {
                            circulationFluid = it
                            saveData()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                            .background(
                                color = Color(0xFF3a3a3a),
                                shape = RoundedCornerShape(4.dp)
                            ),
                        textStyle = TextStyle(
                            color = if (isReadOnly) Color(0xFF666666) else Color.White,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center  // ⭐ 입력 텍스트 가운데
                        ),
                        singleLine = true,
                        readOnly = isReadOnly,
                        enabled = !isReadOnly,
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,  // ⭐ placeholder 가운데
                                modifier = Modifier.fillMaxSize()
                            ) {
                                if (circulationFluid.isEmpty()) {
                                    Text(
                                        text = "수액공급",
                                        color = Color(0xFF666666),
                                        fontSize = 12.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    SelectButton("약물투여", circulationDrug, Modifier.weight(1f), !isReadOnly) {
                        circulationDrug = it
                        saveData()
                    }
                }
            }

            // ==========================================
            // 고정
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "고정",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SelectButton("경추", immobilizationCervical, Modifier.weight(1f), !isReadOnly) {
                        immobilizationCervical = it
                        saveData()
                    }
                    SelectButton("척추", immobilizationSpinal, Modifier.weight(1f), !isReadOnly) {
                        immobilizationSpinal = it
                        saveData()
                    }
                    SelectButton("부목", immobilizationSplint, Modifier.weight(1f), !isReadOnly) {
                        immobilizationSplint = it
                        saveData()
                    }
                    SelectButton("머리", immobilizationHead, Modifier.weight(1f), !isReadOnly) {
                        immobilizationHead = it
                        saveData()
                    }
                }
            }

            // ==========================================
            // 상처 치치
            // ==========================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "상처 치치",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SelectButton("지혈", woundHemostasis, Modifier.weight(1f), !isReadOnly) {
                        woundHemostasis = it
                        saveData()
                    }
                    SelectButton("상처드레싱", woundDressing, Modifier.weight(1f), !isReadOnly) {
                        woundDressing = it
                        saveData()
                    }
                    SelectButton("분만", woundBandage, Modifier.weight(1f), !isReadOnly) {
                        woundBandage = it
                        saveData()
                    }
                    SelectButton("보온(온)", woundHandProtection, Modifier.weight(1f), !isReadOnly) {
                        woundHandProtection = it
                        saveData()
                    }
                    SelectButton("보온(냉)", woundFootProtection, Modifier.weight(1f), !isReadOnly) {
                        woundFootProtection = it
                        saveData()
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

@Composable
private fun SelectButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onToggle: (Boolean) -> Unit
) {
    Button(
        onClick = { onToggle(!isSelected) },
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
            maxLines = 2
        )
    }
}