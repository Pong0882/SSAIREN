// FirstAid.kt (개선 버전)
package com.example.ssairen_app.ui.screens.emergencyact

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ssairen_app.ui.components.MainButton
import com.example.ssairen_app.viewmodel.ActivityLogData
import com.example.ssairen_app.viewmodel.ActivityViewModel
import com.example.ssairen_app.viewmodel.FirstAidData
import com.example.ssairen_app.viewmodel.FirstAidApiState
import com.example.ssairen_app.viewmodel.LogViewModel
import com.example.ssairen_app.viewmodel.SttDataState

@Composable
fun FirstAid(
    viewModel: LogViewModel,
    data: ActivityLogData,
    isReadOnly: Boolean = false,
    activityViewModel: ActivityViewModel = viewModel()
) {
    // ✅ API 상태 관찰
    val firstAidState by activityViewModel.firstAidState.observeAsState(FirstAidApiState.Idle)
    val currentReportId by activityViewModel.currentEmergencyReportId.observeAsState()
    val sttDataState by activityViewModel.sttDataState.observeAsState(SttDataState.Idle)

    // ✅ API에서 데이터를 로드했는지 추적
    var isApiDataLoaded by remember { mutableStateOf(false) }

    // ✅ 응급 처치 상태 (mutableStateOf를 직접 사용하여 초기화 문제 해결)
    var airwayJawThrust by remember { mutableStateOf(false) }
    var airwayHeadTilt by remember { mutableStateOf(false) }
    var airwayNPA by remember { mutableStateOf(false) }
    var airwayOPA by remember { mutableStateOf(false) }
    var airwayIntubation by remember { mutableStateOf(false) }
    var airwaySupraglottic by remember { mutableStateOf(false) }

    var oxygenMask by remember { mutableStateOf(false) }
    var oxygenNasal by remember { mutableStateOf(false) }
    var oxygenBVM by remember { mutableStateOf(false) }
    var oxygenVentilator by remember { mutableStateOf(false) }
    var oxygenSuction by remember { mutableStateOf(false) }

    var cprPerformed by remember { mutableStateOf(false) }
    var cprManual by remember { mutableStateOf(false) }
    var cprDNR by remember { mutableStateOf(false) }
    var cprTermination by remember { mutableStateOf(false) }

    var aedShock by remember { mutableStateOf(false) }
    var aedMonitoring by remember { mutableStateOf(false) }
    var aedApplicationOnly by remember { mutableStateOf(false) }

    var treatmentOxygenSaturation by remember { mutableStateOf(false) }
    var treatmentShockPrevention by remember { mutableStateOf(false) }
    var treatmentInjection by remember { mutableStateOf(false) }

    var immobilizationSpinal by remember { mutableStateOf(false) }
    var immobilizationCSpine by remember { mutableStateOf(false) }
    var immobilizationSplint by remember { mutableStateOf(false) }
    var immobilizationOther by remember { mutableStateOf(false) }

    var woundDressing by remember { mutableStateOf(false) }
    var woundBandage by remember { mutableStateOf(false) }
    var woundHemostasis by remember { mutableStateOf(false) }
    var woundParalysis by remember { mutableStateOf(false) }

    // ✅ 1. 초기 로컬 데이터 로드 (API 데이터 로드 전에만)
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

            treatmentOxygenSaturation = data.firstAid.treatmentOxygenSaturation
            treatmentShockPrevention = data.firstAid.treatmentShockPrevention
            treatmentInjection = data.firstAid.treatmentInjection

            immobilizationSpinal = data.firstAid.immobilizationSpinal
            immobilizationCSpine = data.firstAid.immobilizationCSpine
            immobilizationSplint = data.firstAid.immobilizationSplint
            immobilizationOther = data.firstAid.immobilizationOther

            woundDressing = data.firstAid.woundDressing
            woundBandage = data.firstAid.woundBandage
            woundHemostasis = data.firstAid.woundHemostasis
            woundParalysis = data.firstAid.woundParalysis
        }
    }

    // ✅ 2. API 호출 (currentReportId가 설정되면 자동 실행)
    LaunchedEffect(currentReportId) {
        currentReportId?.let { reportId ->
            android.util.Log.d("FirstAid", "📞 API 호출: getFirstAid($reportId)")
            activityViewModel.getFirstAid(reportId)
        }
    }

    // ✅ 3. API 응답 처리
    LaunchedEffect(firstAidState) {
        when (val state = firstAidState) {
            is FirstAidApiState.Success -> {
                val treatment = state.firstAidResponse.data.data.treatment

                android.util.Log.d("FirstAid", "✅ API 데이터 로드 완료")

                // 기도 확보 매핑
                treatment.airwayManagement?.methods?.let { methods ->
                    airwayJawThrust = methods.any { it.contains("Jaw Thrust") || it.contains("하악거상") || it.contains("기도유지") }
                    airwayHeadTilt = methods.any { it.contains("Head Tilt") || it.contains("두부후굴") }
                    airwayNPA = methods.any { it.contains("NPA") || it.contains("비인두") }
                    airwayOPA = methods.any { it.contains("OPA") || it.contains("구인두") }
                    airwayIntubation = methods.any { it.contains("기도삽관") || it.contains("전문기도") }
                    airwaySupraglottic = methods.any { it.contains("성문상") }
                }

                // 산소 투여 매핑
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

                // CPR 매핑
                treatment.cpr?.let { cprStatus ->
                    cprPerformed = cprStatus.contains("실시")
                    cprManual = cprStatus.contains("개방") || cprStatus.contains("1회") || cprStatus.contains("다회")
                    cprDNR = cprStatus.contains("DNR")
                    cprTermination = cprStatus.contains("중단")
                }

                // AED 매핑
                treatment.aed?.let { aed ->
                    aedShock = false
                    aedMonitoring = false

                    when (aed.type) {
                        "shock" -> aedShock = true
                        "monitoring" -> aedMonitoring = true
                    }
                }

                // 상처 처치 매핑
                treatment.woundCare?.let { woundCare ->
                    woundDressing = woundCare.contains("드레싱") || woundCare.contains("소독")
                    woundBandage = woundCare.contains("붕대")
                    woundHemostasis = woundCare.contains("압박") || woundCare.contains("지혈")
                }

                // 고정 매핑
                treatment.fixed?.let { fixed ->
                    immobilizationSpinal = fixed.contains("척추")
                    immobilizationCSpine = fixed.contains("목") || fixed.contains("경추")
                    immobilizationSplint = fixed.contains("부목")
                }

                // 약물 투여 매핑
                treatment.drug?.let { drug ->
                    treatmentInjection = drug.isNotEmpty()
                }

                // ✅ API 데이터 로드 완료 표시
                isApiDataLoaded = true

                android.util.Log.d("FirstAid", "📋 매핑 완료 - CPR: $cprPerformed, AED Shock: $aedShock")

                // ✅ LogViewModel에 동기화 (덮어쓰기 버그 방지)
                saveData()
                android.util.Log.d("FirstAid", "💾 LogViewModel 동기화 완료")
            }
            is FirstAidApiState.Error -> {
                android.util.Log.e("FirstAid", "❌ API 오류: ${state.message}")
            }
            else -> { /* Loading or Idle */ }
        }
    }

    // ✅ 4. 자동 저장 함수 (LogViewModel에 저장)
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
        android.util.Log.d("FirstAid", "💾 로컬 저장 완료")
    }

    // ✅ 로딩 중일 때 표시
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
                            TreatmentButton("Shock", aedShock, modifier = Modifier.weight(1f)) {
                                aedShock = it
                                saveData()
                            }
                            TreatmentButton("Monitoring", aedMonitoring, modifier = Modifier.weight(1f)) {
                                aedMonitoring = it
                                saveData()
                            }
                            TreatmentButton("기타 사용", aedApplicationOnly, modifier = Modifier.weight(1f)) {
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

        // 읽기 전용 모드일 때 터치 차단
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