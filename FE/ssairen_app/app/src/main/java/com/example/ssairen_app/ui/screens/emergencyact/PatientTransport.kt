package com.example.ssairen_app.ui.screens.emergencyact

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.viewmodel.ActivityViewModel
import com.example.ssairen_app.viewmodel.TransportApiState
import com.example.ssairen_app.viewmodel.PatientTransportData

/**
 * 환자이송 섹션 메인 화면
 *
 * @param viewModel LogViewModel
 * @param data ActivityLogData
 * @param isReadOnly 읽기 전용 모드
 * @param activityViewModel ActivityViewModel (API 호출용)
 */

@Composable
fun PatientTransport(
    viewModel: com.example.ssairen_app.viewmodel.LogViewModel,
    data: com.example.ssairen_app.viewmodel.ActivityLogData,
    isReadOnly: Boolean = false,
    activityViewModel: ActivityViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val activityLogData by viewModel.activityLogData.collectAsState()
    val transportData = activityLogData.patientTransport

    // API 상태 관찰
    val transportState by activityViewModel.transportState.observeAsState()
    val currentReportId by activityViewModel.currentEmergencyReportId.observeAsState()

    // 로컬 UI 상태 - ViewModel에서 가져온 값으로 초기화
    var firstHospitalName by remember { mutableStateOf(transportData.firstHospitalName) }
    var selectedFirstRegion by remember { mutableStateOf(transportData.firstRegionType) }
    var firstArrivalTime by remember { mutableStateOf(transportData.firstArrivalTime) }
    var firstDistance by remember { mutableStateOf(if (transportData.firstDistanceKm > 0)
        transportData.firstDistanceKm.toString() else "") }
    var selectedFirstMedicalSelector by remember { mutableStateOf(transportData.firstSelectedBy) }
    var selectedFirstBedShortageReasons by remember {
        mutableStateOf(transportData.firstBedShortageReasons) }
    var selectedFirstOtherReasons by remember { mutableStateOf(transportData.firstOtherReasons) }
    var selectedFirstPatientReceiver by remember { mutableStateOf(transportData.firstReceiver) }

    var secondHospitalName by remember { mutableStateOf(transportData.secondHospitalName) }
    var selectedSecondRegion by remember { mutableStateOf(transportData.secondRegionType) }
    var secondArrivalTime by remember { mutableStateOf(transportData.secondArrivalTime) }
    var secondDistance by remember { mutableStateOf(if (transportData.secondDistanceKm > 0)
        transportData.secondDistanceKm.toString() else "") }
    var selectedSecondMedicalSelector by remember { mutableStateOf(transportData.secondSelectedBy) }
    var selectedSecondBedShortageReasons by remember {
        mutableStateOf(transportData.secondBedShortageReasons) }
    var selectedSecondOtherReasons by remember { mutableStateOf(transportData.secondOtherReasons) }
    var selectedSecondPatientReceiver by remember { mutableStateOf(transportData.secondReceiver) }

    var firstReceiverSignature by remember { mutableStateOf(transportData.firstReceiverSignature) }
    var secondReceiverSignature by remember { mutableStateOf(transportData.secondReceiverSignature) }

    var showSignatureDialog by remember { mutableStateOf(false) }
    var signatureTarget by remember { mutableStateOf<TransportSignatureTarget?>(null) }

    // API 호출 (currentReportId가 설정되면 자동 실행)
    LaunchedEffect(currentReportId) {
        currentReportId?.let { reportId ->
            Log.d("PatientTransport", "📞 API 호출: getTransport($reportId)")
            activityViewModel.getTransport(reportId)
        }
    }

    // API 응답 처리
    LaunchedEffect(transportState) {
        Log.d("PatientTransport", "🟢 transportState 변경: $transportState")

        when (val state = transportState) {
            is TransportApiState.Success -> {
                Log.d("PatientTransport", "✅ API 성공 - 데이터 매핑 시작")
                val apiData = state.transportResponse.data?.data?.patientTransport

                if (apiData != null) {
                    // 1차 이송 데이터 매핑
                    apiData.firstTransport?.let { first ->
                        firstHospitalName = first.hospitalName ?: ""
                        firstArrivalTime = first.arrivalTime ?: ""
                        firstDistance = first.distanceKm?.toString() ?: ""
                        selectedFirstMedicalSelector = first.selectedBy ?: ""
                        selectedFirstPatientReceiver = first.receiver ?: ""

                        // retransportReason 파싱
                        val bedShortage = mutableSetOf<String>()
                        val otherReasons = mutableSetOf<String>()

                        first.retransportReason?.forEach { reason ->
                            when (reason.type) {
                                "병상부족" -> {
                                    reason.name?.forEach { bedShortage.add(it) }
                                }
                                else -> {
                                    otherReasons.add(reason.type)
                                }
                            }
                        }

                        selectedFirstBedShortageReasons = bedShortage
                        selectedFirstOtherReasons = otherReasons
                    }

                    // 2차 이송 데이터 매핑
                    apiData.secondTransport?.let { second ->
                        secondHospitalName = second.hospitalName ?: ""
                        secondArrivalTime = second.arrivalTime ?: ""
                        secondDistance = second.distanceKm?.toString() ?: ""
                        selectedSecondMedicalSelector = second.selectedBy ?: ""
                        selectedSecondPatientReceiver = second.receiver ?: ""

                        // retransportReason 파싱
                        val bedShortage = mutableSetOf<String>()
                        val otherReasons = mutableSetOf<String>()

                        second.retransportReason?.forEach { reason ->
                            when (reason.type) {
                                "병상부족" -> {
                                    reason.name?.forEach { bedShortage.add(it) }
                                }
                                else -> {
                                    otherReasons.add(reason.type)
                                }
                            }
                        }

                        selectedSecondBedShortageReasons = bedShortage
                        selectedSecondOtherReasons = otherReasons
                    }

                    Log.d("PatientTransport", "✅ 데이터 매핑 완료")

                    // ✅ LogViewModel에 동기화 (덮어쓰기 버그 방지)
                    viewModel.updatePatientTransport(
                        PatientTransportData(
                            firstHospitalName = firstHospitalName,
                            firstRegionType = selectedFirstRegion,
                            firstArrivalTime = firstArrivalTime,
                            firstDistanceKm = firstDistance.toDoubleOrNull() ?: 0.0,
                            firstSelectedBy = selectedFirstMedicalSelector,
                            firstBedShortageReasons = selectedFirstBedShortageReasons,
                            firstOtherReasons = selectedFirstOtherReasons,
                            firstReceiver = selectedFirstPatientReceiver,
                            firstReceiverSignature = firstReceiverSignature,
                            secondHospitalName = secondHospitalName,
                            secondRegionType = selectedSecondRegion,
                            secondArrivalTime = secondArrivalTime,
                            secondDistanceKm = secondDistance.toDoubleOrNull() ?: 0.0,
                            secondSelectedBy = selectedSecondMedicalSelector,
                            secondBedShortageReasons = selectedSecondBedShortageReasons,
                            secondOtherReasons = selectedSecondOtherReasons,
                            secondReceiver = selectedSecondPatientReceiver,
                            secondReceiverSignature = secondReceiverSignature
                        )
                    )
                    Log.d("PatientTransport", "💾 LogViewModel 동기화 완료")
                }
            }
            is TransportApiState.Error -> {
                Log.e("PatientTransport", "❌ API 오류: ${state.message}")
            }
            is TransportApiState.Loading -> {
                Log.d("PatientTransport", "⏳ 로딩 중...")
            }
            else -> {
                Log.d("PatientTransport", "⚪ Idle 상태")
            }
        }
    }

    // ViewModel 데이터가 변경되면 UI 상태 업데이트
    LaunchedEffect(transportData) {
        firstHospitalName = transportData.firstHospitalName
        selectedFirstRegion = transportData.firstRegionType
        firstArrivalTime = transportData.firstArrivalTime
        firstDistance = if (transportData.firstDistanceKm > 0) transportData.firstDistanceKm.toString()
        else ""
        selectedFirstMedicalSelector = transportData.firstSelectedBy
        selectedFirstBedShortageReasons = transportData.firstBedShortageReasons
        selectedFirstOtherReasons = transportData.firstOtherReasons
        selectedFirstPatientReceiver = transportData.firstReceiver
        firstReceiverSignature = transportData.firstReceiverSignature

        secondHospitalName = transportData.secondHospitalName
        selectedSecondRegion = transportData.secondRegionType
        secondArrivalTime = transportData.secondArrivalTime
        secondDistance = if (transportData.secondDistanceKm > 0)
            transportData.secondDistanceKm.toString() else ""
        selectedSecondMedicalSelector = transportData.secondSelectedBy
        selectedSecondBedShortageReasons = transportData.secondBedShortageReasons
        selectedSecondOtherReasons = transportData.secondOtherReasons
        selectedSecondPatientReceiver = transportData.secondReceiver
        secondReceiverSignature = transportData.secondReceiverSignature
    }

    // 값이 변경될 때마다 ViewModel 업데이트 (읽기 전용이 아닐 때만)
    LaunchedEffect(
        firstHospitalName, firstArrivalTime, firstDistance, selectedFirstMedicalSelector,
        selectedFirstBedShortageReasons, selectedFirstOtherReasons, selectedFirstPatientReceiver,
        firstReceiverSignature,
        secondHospitalName, secondArrivalTime, secondDistance, selectedSecondMedicalSelector,
        selectedSecondBedShortageReasons, selectedSecondOtherReasons, selectedSecondPatientReceiver,
        secondReceiverSignature
    ) {
        if (!isReadOnly) {
            viewModel.updatePatientTransport(
                PatientTransportData(
                    firstHospitalName = firstHospitalName,
                    firstRegionType = selectedFirstRegion,
                    firstArrivalTime = firstArrivalTime,
                    firstDistanceKm = firstDistance.toDoubleOrNull() ?: 0.0,
                    firstSelectedBy = selectedFirstMedicalSelector,
                    firstBedShortageReasons = selectedFirstBedShortageReasons,
                    firstOtherReasons = selectedFirstOtherReasons,
                    firstReceiver = selectedFirstPatientReceiver,
                    firstReceiverSignature = firstReceiverSignature,
                    secondHospitalName = secondHospitalName,
                    secondRegionType = selectedSecondRegion,
                    secondArrivalTime = secondArrivalTime,
                    secondDistanceKm = secondDistance.toDoubleOrNull() ?: 0.0,
                    secondSelectedBy = selectedSecondMedicalSelector,
                    secondBedShortageReasons = selectedSecondBedShortageReasons,
                    secondOtherReasons = selectedSecondOtherReasons,
                    secondReceiver = selectedSecondPatientReceiver,
                    secondReceiverSignature = secondReceiverSignature
                )
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1차/2차 이송(연계) 기관명 (가로 배치)
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
                        value = firstHospitalName,
                        onValueChange = { firstHospitalName = it },
                        enabled = !isReadOnly,
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
                        value = secondHospitalName,
                        onValueChange = { secondHospitalName = it },
                        enabled = !isReadOnly,
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
        }

        // 도착시간 + 거리(km) (가로 배치)
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
                                enabled = !isReadOnly,
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
                                enabled = !isReadOnly,
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
                                enabled = !isReadOnly,
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
                                enabled = !isReadOnly,
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
                }
            }
        }

        // 의료기관 선정자 등 (가로 배치)
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
                        options = listOf("구급대", "119상황실", "구급상황센터", "환자보호자",
                            "병원수용곤란등", "기타"),
                        selectedOption = selectedFirstMedicalSelector,
                        onOptionSelected = { selectedFirstMedicalSelector = it },
                        enabled = !isReadOnly,
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
                        options = listOf("구급대", "119상황실", "구급상황센터", "환자보호자",
                            "병원수용곤란등", "기타"),
                        selectedOption = selectedSecondMedicalSelector,
                        onOptionSelected = { selectedSecondMedicalSelector = it },
                        enabled = !isReadOnly,
                        columns = 3
                    )
                }
            }
        }

        // 재이송 사유 - 병상부족 (가로 배치)
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

                    CompactMultiSelectButtonGroup(
                        options = listOf("응급실", "수술실", "입원실", "중환자실"),
                        selectedOptions = selectedFirstBedShortageReasons,
                        onOptionsChanged = { selectedFirstBedShortageReasons = it },
                        enabled = !isReadOnly,
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

                    CompactMultiSelectButtonGroup(
                        options = listOf("응급실", "수술실", "입원실", "중환자실"),
                        selectedOptions = selectedSecondBedShortageReasons,
                        onOptionsChanged = { selectedSecondBedShortageReasons = it },
                        enabled = !isReadOnly,
                        columns = 4
                    )
                }
            }
        }

        // 재이송 사유 - 이외 (가로 배치)
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

                    CompactMultiSelectButtonGroup(
                        options = listOf("전문의부재", "환자/보호자변심", "의료장비고장", "1차응급처치",
                            "주취자등", "기타"),
                        selectedOptions = selectedFirstOtherReasons,
                        onOptionsChanged = { selectedFirstOtherReasons = it },
                        enabled = !isReadOnly,
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

                    CompactMultiSelectButtonGroup(
                        options = listOf("전문의부재", "환자/보호자변심", "의료장비고장", "1차응급처치",
                            "주취자등", "기타"),
                        selectedOptions = selectedSecondOtherReasons,
                        onOptionsChanged = { selectedSecondOtherReasons = it },
                        enabled = !isReadOnly,
                        columns = 3
                    )
                }
            }
        }

        // 환자 인수자 (가로 배치)
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
                        enabled = !isReadOnly,
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
                        enabled = !isReadOnly,
                        columns = 4
                    )
                }
            }
        }

        // 서명 영역 (가로 배치)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1차 인수자 서명
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransportSignatureArea(
                        signature = firstReceiverSignature,
                        onSignatureClick = {
                            signatureTarget = TransportSignatureTarget.FirstReceiver
                            showSignatureDialog = true
                        },
                        enabled = !isReadOnly
                    )
                }

                // 2차 인수자 서명
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransportSignatureArea(
                        signature = secondReceiverSignature,
                        onSignatureClick = {
                            signatureTarget = TransportSignatureTarget.SecondReceiver
                            showSignatureDialog = true
                        },
                        enabled = !isReadOnly
                    )
                }
            }
        }
    }

    // 서명 다이얼로그
    if (showSignatureDialog) {
        SignatureDialog(
            onDismiss = { showSignatureDialog = false },
            onConfirm = { signature ->
                when (signatureTarget) {
                    is TransportSignatureTarget.FirstReceiver -> firstReceiverSignature = signature
                    is TransportSignatureTarget.SecondReceiver -> secondReceiverSignature = signature
                    else -> {}
                }
                showSignatureDialog = false
            }
        )
    }
}

// 콤팩트 단일 선택 버튼 그룹 (작은 버튼용)
@Composable
private fun CompactSingleSelectButtonGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
                        enabled = enabled,
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

// 콤팩트 다중 선택 버튼 그룹 (작은 버튼용)
@Composable
private fun CompactMultiSelectButtonGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionsChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
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
                        enabled = enabled,
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

// 콤팩트 선택 버튼 (작은 버튼용)
@Composable
private fun CompactSelectButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF3b7cff) else Color(0xFF3a3a3a),
            contentColor = Color.White,
            disabledContainerColor = if (isSelected) Color(0xFF2a5ab8) else Color(0xFF2a2a2a),
            disabledContentColor = Color(0xFF666666)
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

// 서명 Target sealed class
sealed class TransportSignatureTarget {
    object FirstReceiver : TransportSignatureTarget()
    object SecondReceiver : TransportSignatureTarget()
}

// 서명 영역 컴포넌트
@Composable
private fun TransportSignatureArea(
    signature: List<Offset>,
    onSignatureClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(60.dp)
                .background(Color.Transparent)
                .clickable(enabled = enabled) { onSignatureClick() }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.BottomCenter)
            ) {
                drawLine(
                    color = Color(0xFF3a3a3a),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
            }

            if (signature.isEmpty()) {
                Text(
                    text = "서명 또는 인",
                    color = Color(0xFF999999),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(bottom = 8.dp)
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp)
                ) {
                    if (signature.size > 1) {
                        val pathData = Path()
                        val minX = signature.minOfOrNull { it.x } ?: 0f
                        val maxX = signature.maxOfOrNull { it.x } ?: size.width
                        val minY = signature.minOfOrNull { it.y } ?: 0f
                        val maxY = signature.maxOfOrNull { it.y } ?: size.height

                        val scaleX = if (maxX - minX > 0) size.width / (maxX - minX) else 1f
                        val scaleY = if (maxY - minY > 0) size.height / (maxY - minY) else 1f
                        val scale = minOf(scaleX, scaleY) * 0.8f

                        val offsetX = (size.width - (maxX - minX) * scale) / 2f
                        val offsetY = (size.height - (maxY - minY) * scale) / 2f

                        val scaledPoints = signature.map { offset ->
                            Offset(
                                x = (offset.x - minX) * scale + offsetX,
                                y = (offset.y - minY) * scale + offsetY
                            )
                        }

                        pathData.moveTo(scaledPoints.first().x, scaledPoints.first().y)
                        scaledPoints.drop(1).forEach { offset ->
                            pathData.lineTo(offset.x, offset.y)
                        }

                        drawPath(
                            path = pathData,
                            color = Color.White,
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignatureDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<Offset>) -> Unit
) {
    var paths by remember { mutableStateOf(listOf<List<Offset>>()) }
    var currentPath by remember { mutableStateOf(listOf<Offset>()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2a2a2a))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "서명하기",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(8.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPath = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        currentPath = currentPath + change.position
                                    },
                                    onDragEnd = {
                                        paths = paths + listOf(currentPath)
                                        currentPath = emptyList()
                                    }
                                )
                            }
                    ) {
                        paths.forEach { path ->
                            if (path.size > 1) {
                                val pathData = Path()
                                pathData.moveTo(path.first().x, path.first().y)
                                path.drop(1).forEach { offset ->
                                    pathData.lineTo(offset.x, offset.y)
                                }
                                drawPath(
                                    path = pathData,
                                    color = Color.Black,
                                    style = Stroke(width = 5f)
                                )
                            }
                        }

                        if (currentPath.size > 1) {
                            val pathData = Path()
                            pathData.moveTo(currentPath.first().x, currentPath.first().y)
                            currentPath.drop(1).forEach { offset ->
                                pathData.lineTo(offset.x, offset.y)
                            }
                            drawPath(
                                path = pathData,
                                color = Color.Black,
                                style = Stroke(width = 5f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            paths = emptyList()
                            currentPath = emptyList()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFF4a4a4a))
                    ) {
                        Text("초기화")
                    }

                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4a4a4a),
                            contentColor = Color.White
                        )
                    ) {
                        Text("취소")
                    }

                    Button(
                        onClick = { onConfirm(paths.flatten()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3b7cff),
                            contentColor = Color.White
                        )
                    ) {
                        Text("확인")
                    }
                }
            }
        }
    }
}