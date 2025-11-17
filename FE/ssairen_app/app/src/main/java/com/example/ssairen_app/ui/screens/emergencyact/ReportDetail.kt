package com.example.ssairen_app.ui.screens.emergencyact

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.ui.components.SignatureArea
import com.example.ssairen_app.ui.components.SignatureDialog
import com.example.ssairen_app.viewmodel.ActivityViewModel
import com.example.ssairen_app.viewmodel.DetailReportApiState

/**
 * 세부사항 섹션 메인 화면
 *
 * @param viewModel LogViewModel
 * @param data ActivityLogData
 * @param isReadOnly 읽기 전용 모드
 * @param activityViewModel ActivityViewModel (API 호출용)
 */
@Composable
fun ReportDetail(
    viewModel: com.example.ssairen_app.viewmodel.LogViewModel,
    data: com.example.ssairen_app.viewmodel.ActivityLogData,
    isReadOnly: Boolean = false,
    activityViewModel: ActivityViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val activityLogData by viewModel.activityLogData.collectAsState()
    val reportDetailData = activityLogData.reportDetail

    val detailReportState by activityViewModel.detailReportState.observeAsState()
    val currentReportId by activityViewModel.currentEmergencyReportId.observeAsState()

    var doctorAffiliation by remember { mutableStateOf(reportDetailData.doctorAffiliation) }
    var doctorName by remember { mutableStateOf(reportDetailData.doctorName) }
    var doctorSignature by remember { mutableStateOf(reportDetailData.doctorSignature) }

    var paramedic1Grade by remember { mutableStateOf(reportDetailData.paramedic1Grade) }
    var paramedic1Rank by remember { mutableStateOf(reportDetailData.paramedic1Rank) }
    var paramedic1Name by remember { mutableStateOf(reportDetailData.paramedic1Name) }
    var paramedic1Signature by remember { mutableStateOf(reportDetailData.paramedic1Signature) }

    var paramedic2Grade by remember { mutableStateOf(reportDetailData.paramedic2Grade) }
    var paramedic2Rank by remember { mutableStateOf(reportDetailData.paramedic2Rank) }
    var paramedic2Name by remember { mutableStateOf(reportDetailData.paramedic2Name) }
    var paramedic2Signature by remember { mutableStateOf(reportDetailData.paramedic2Signature) }

    var driverGrade by remember { mutableStateOf(reportDetailData.driverGrade) }
    var driverRank by remember { mutableStateOf(reportDetailData.driverRank) }
    var driverName by remember { mutableStateOf(reportDetailData.driverName) }
    var driverSignature by remember { mutableStateOf(reportDetailData.driverSignature) }

    var otherGrade by remember { mutableStateOf(reportDetailData.otherGrade) }
    var otherRank by remember { mutableStateOf(reportDetailData.otherRank) }
    var otherName by remember { mutableStateOf(reportDetailData.otherName) }
    var otherSignature by remember { mutableStateOf(reportDetailData.otherSignature) }

    var selectedObstacles by remember { mutableStateOf(reportDetailData.obstacles) }
    var obstacleOtherValue by remember { mutableStateOf(reportDetailData.obstacleOtherValue ?: "") }

    var showSignatureDialog by remember { mutableStateOf(false) }
    var signatureTarget by remember { mutableStateOf<SignatureTarget?>(null) }

    LaunchedEffect(currentReportId) {
        currentReportId?.let { reportId ->
            Log.d("ReportDetail", "📞 API 호출: getDetailReport($reportId)")
            activityViewModel.getDetailReport(reportId)
        }
    }

    LaunchedEffect(detailReportState) {
        Log.d("ReportDetail", "🟢 detailReportState 변경: $detailReportState")

        when (val state = detailReportState) {
            is DetailReportApiState.Success -> {
                Log.d("ReportDetail", "✅ API 성공 - 데이터 매핑 시작")
                val apiData = state.detailReportResponse.data?.data?.detailReport

                if (apiData != null) {
                    apiData.doctor?.let { doctor ->
                        doctorAffiliation = doctor.affiliation ?: ""
                        doctorName = doctor.name ?: ""
                    }

                    apiData.paramedic1?.let { p1 ->
                        paramedic1Name = p1.name ?: ""
                        paramedic1Grade = p1.grade ?: ""
                        paramedic1Rank = p1.rank ?: ""
                    }

                    apiData.paramedic2?.let { p2 ->
                        paramedic2Name = p2.name ?: ""
                        paramedic2Grade = p2.grade ?: ""
                        paramedic2Rank = p2.rank ?: ""
                    }

                    apiData.driver?.let { driver ->
                        driverName = driver.name ?: ""
                        driverGrade = driver.grade ?: ""
                        driverRank = driver.rank ?: ""
                    }

                    apiData.other?.let { other ->
                        otherName = other.name ?: ""
                        otherGrade = other.grade ?: ""
                        otherRank = other.rank ?: ""
                    }

                    val obstacles = mutableSetOf<String>()
                    var otherValue: String? = null

                    apiData.obstacles?.forEach { obstacle ->
                        obstacles.add(obstacle.type)
                        if (obstacle.isCustom && obstacle.value != null) {
                            otherValue = obstacle.value
                        }
                    }

                    selectedObstacles = obstacles
                    obstacleOtherValue = otherValue ?: ""

                    Log.d("ReportDetail", "✅ 데이터 매핑 완료")

                    // ✅ LogViewModel에 동기화 (덮어쓰기 버그 방지)
                    viewModel.updateReportDetail(
                        com.example.ssairen_app.viewmodel.ReportDetailData(
                            doctorAffiliation = doctorAffiliation,
                            doctorName = doctorName,
                            doctorSignature = doctorSignature,
                            paramedic1Grade = paramedic1Grade,
                            paramedic1Rank = paramedic1Rank,
                            paramedic1Name = paramedic1Name,
                            paramedic1Signature = paramedic1Signature,
                            paramedic2Grade = paramedic2Grade,
                            paramedic2Rank = paramedic2Rank,
                            paramedic2Name = paramedic2Name,
                            paramedic2Signature = paramedic2Signature,
                            driverGrade = driverGrade,
                            driverRank = driverRank,
                            driverName = driverName,
                            driverSignature = driverSignature,
                            otherGrade = otherGrade,
                            otherRank = otherRank,
                            otherName = otherName,
                            otherSignature = otherSignature,
                            obstacles = selectedObstacles,
                            obstacleOtherValue = if (selectedObstacles.contains("기타")) obstacleOtherValue else null
                        )
                    )
                    Log.d("ReportDetail", "💾 LogViewModel 동기화 완료")
                }
            }
            is DetailReportApiState.Error -> {
                Log.e("ReportDetail", "❌ API 오류: ${state.message}")
            }
            is DetailReportApiState.UpdateSuccess -> {
                Log.d("ReportDetail", "✅ 업데이트 성공")
            }
            is DetailReportApiState.UpdateError -> {
                Log.e("ReportDetail", "❌ 업데이트 오류: ${state.message}")
            }
            else -> {}
        }
    }

    LaunchedEffect(reportDetailData) {
        doctorAffiliation = reportDetailData.doctorAffiliation
        doctorName = reportDetailData.doctorName
        paramedic1Grade = reportDetailData.paramedic1Grade
        paramedic1Rank = reportDetailData.paramedic1Rank
        paramedic1Name = reportDetailData.paramedic1Name
        paramedic2Grade = reportDetailData.paramedic2Grade
        paramedic2Rank = reportDetailData.paramedic2Rank
        paramedic2Name = reportDetailData.paramedic2Name
        driverGrade = reportDetailData.driverGrade
        driverRank = reportDetailData.driverRank
        driverName = reportDetailData.driverName
        otherGrade = reportDetailData.otherGrade
        otherRank = reportDetailData.otherRank
        otherName = reportDetailData.otherName
        selectedObstacles = reportDetailData.obstacles
        obstacleOtherValue = reportDetailData.obstacleOtherValue ?: ""
    }

    LaunchedEffect(
        doctorAffiliation, doctorName, doctorSignature,
        paramedic1Grade, paramedic1Rank, paramedic1Name, paramedic1Signature,
        paramedic2Grade, paramedic2Rank, paramedic2Name, paramedic2Signature,
        driverGrade, driverRank, driverName, driverSignature,
        otherGrade, otherRank, otherName, otherSignature,
        selectedObstacles, obstacleOtherValue
    ) {
        viewModel.updateReportDetail(
            com.example.ssairen_app.viewmodel.ReportDetailData(
                doctorAffiliation = doctorAffiliation,
                doctorName = doctorName,
                doctorSignature = doctorSignature,
                paramedic1Grade = paramedic1Grade,
                paramedic1Rank = paramedic1Rank,
                paramedic1Name = paramedic1Name,
                paramedic1Signature = paramedic1Signature,
                paramedic2Grade = paramedic2Grade,
                paramedic2Rank = paramedic2Rank,
                paramedic2Name = paramedic2Name,
                paramedic2Signature = paramedic2Signature,
                driverGrade = driverGrade,
                driverRank = driverRank,
                driverName = driverName,
                driverSignature = driverSignature,
                otherGrade = otherGrade,
                otherRank = otherRank,
                otherName = otherName,
                otherSignature = otherSignature,
                obstacles = selectedObstacles,
                obstacleOtherValue = if (selectedObstacles.contains("기타")) obstacleOtherValue else null
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "출동인원",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            DoctorSection(
                affiliation = doctorAffiliation,
                name = doctorName,
                signature = doctorSignature,
                onAffiliationChange = { doctorAffiliation = it },
                onNameChange = { doctorName = it },
                onSignatureClick = {
                    signatureTarget = SignatureTarget.Doctor
                    showSignatureDialog = true
                },
                enabled = !isReadOnly
            )
        }

        item {
            ParamedicSection(
                title = "구급대원(1)",
                name = paramedic1Name,
                rank = paramedic1Rank,
                grade = paramedic1Grade,
                signature = paramedic1Signature,
                onNameChange = { paramedic1Name = it },
                onRankChange = { paramedic1Rank = it },
                onGradeChange = { paramedic1Grade = it },
                onSignatureClick = {
                    signatureTarget = SignatureTarget.Paramedic1
                    showSignatureDialog = true
                },
                enabled = !isReadOnly
            )
        }

        item {
            ParamedicSection(
                title = "구급대원(2)",
                name = paramedic2Name,
                rank = paramedic2Rank,
                grade = paramedic2Grade,
                signature = paramedic2Signature,
                onNameChange = { paramedic2Name = it },
                onRankChange = { paramedic2Rank = it },
                onGradeChange = { paramedic2Grade = it },
                onSignatureClick = {
                    signatureTarget = SignatureTarget.Paramedic2
                    showSignatureDialog = true
                },
                enabled = !isReadOnly
            )
        }

        item {
            ParamedicSection(
                title = "운전요원",
                name = driverName,
                rank = driverRank,
                grade = driverGrade,
                signature = driverSignature,
                onNameChange = { driverName = it },
                onRankChange = { driverRank = it },
                onGradeChange = { driverGrade = it },
                onSignatureClick = {
                    signatureTarget = SignatureTarget.Driver
                    showSignatureDialog = true
                },
                enabled = !isReadOnly
            )
        }

        item {
            ParamedicSection(
                title = "기타",
                name = otherName,
                rank = otherRank,
                grade = otherGrade,
                signature = otherSignature,
                onNameChange = { otherName = it },
                onRankChange = { otherRank = it },
                onGradeChange = { otherGrade = it },
                onSignatureClick = {
                    signatureTarget = SignatureTarget.Others
                    showSignatureDialog = true
                },
                enabled = !isReadOnly
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "장애요인",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                MultiSelectButtonGroup(
                    options = listOf("없음", "장거리 이송", "보호자 요구", "원거리 병원", "원거리 출동", "환자 과체중"),
                    selectedOptions = selectedObstacles,
                    onOptionsChanged = { selectedObstacles = it },
                    columns = 6,
                    enabled = !isReadOnly
                )

                MultiSelectButtonGroup(
                    options = listOf("폭우", "교통정체", "환자위치 불명확", "기관 협조 미흡", "언어폭력", "만취자"),
                    selectedOptions = selectedObstacles,
                    onOptionsChanged = { selectedObstacles = it },
                    columns = 6,
                    enabled = !isReadOnly
                )

                MultiSelectButtonGroup(
                    options = listOf("폭행", "폭설", "기타"),
                    selectedOptions = selectedObstacles,
                    onOptionsChanged = { selectedObstacles = it },
                    columns = 3,
                    enabled = !isReadOnly
                )

                if (selectedObstacles.contains("기타")) {
                    OutlinedTextField(
                        value = obstacleOtherValue,
                        onValueChange = { obstacleOtherValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        placeholder = { Text("기타 장애요인을 입력하세요", color = Color(0xFF999999)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color(0xFF666666),
                            focusedBorderColor = Color(0xFF3b7cff),
                            unfocusedBorderColor = Color(0xFF3a3a3a),
                            cursorColor = Color(0xFF3b7cff)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isReadOnly
                    )
                }
            }
        }
    }

    if (showSignatureDialog) {
        SignatureDialog(
            onDismiss = { showSignatureDialog = false },
            onConfirm = { signature ->
                when (signatureTarget) {
                    is SignatureTarget.Doctor -> doctorSignature = signature
                    is SignatureTarget.Paramedic1 -> paramedic1Signature = signature
                    is SignatureTarget.Paramedic2 -> paramedic2Signature = signature
                    is SignatureTarget.Driver -> driverSignature = signature
                    is SignatureTarget.Others -> otherSignature = signature
                    else -> {}
                }
                showSignatureDialog = false
            }
        )
    }
}

sealed class SignatureTarget {
    object Doctor : SignatureTarget()
    object Paramedic1 : SignatureTarget()
    object Paramedic2 : SignatureTarget()
    object Driver : SignatureTarget()
    object Others : SignatureTarget()
}

@Composable
private fun DoctorSection(
    affiliation: String,
    name: String,
    signature: List<Offset>,
    onAffiliationChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onSignatureClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "의사",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "소속", color = Color.White, fontSize = 14.sp)
                TextField(
                    value = affiliation,
                    onValueChange = onAffiliationChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color(0xFF666666),
                        focusedIndicatorColor = Color(0xFF3a3a3a),
                        unfocusedIndicatorColor = Color(0xFF3a3a3a),
                        cursorColor = Color(0xFF3b7cff)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    singleLine = true,
                    enabled = enabled
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "성명", color = Color.White, fontSize = 14.sp)
                TextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color(0xFF666666),
                        focusedIndicatorColor = Color(0xFF3a3a3a),
                        unfocusedIndicatorColor = Color(0xFF3a3a3a),
                        cursorColor = Color(0xFF3b7cff)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    singleLine = true,
                    enabled = enabled
                )
            }
        }

        SignatureArea(
            signature = signature,
            onSignatureClick = onSignatureClick,
            enabled = enabled,
            widthFraction = 0.33f
        )
    }
}

@Composable
private fun ParamedicSection(
    title: String,
    name: String,
    rank: String,
    grade: String,
    signature: List<Offset>,
    onNameChange: (String) -> Unit,
    onRankChange: (String) -> Unit,
    onGradeChange: (String) -> Unit,
    onSignatureClick: () -> Unit,
    enabled: Boolean = true
) {
    var rankExpanded by remember { mutableStateOf(false) }

    val ranks = listOf(
        "소방사시보", "소방사", "소방교", "소방장", "소방위", "소방경",
        "소방령", "소방정", "소방준감", "소방감", "소방정감", "소방총감"
    )

    val qualifications = listOf("1급", "2급", "간호사", "구급교육", "기타")

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "성명", color = Color.White, fontSize = 14.sp)
                TextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color(0xFF666666),
                        focusedIndicatorColor = Color(0xFF3a3a3a),
                        unfocusedIndicatorColor = Color(0xFF3a3a3a),
                        cursorColor = Color(0xFF3b7cff)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    singleLine = true,
                    enabled = enabled
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "계급(p)", color = Color.White, fontSize = 14.sp)

                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = rankExpanded,
                    onExpandedChange = { if (enabled) rankExpanded = it }
                ) {
                    TextField(
                        value = rank.ifEmpty { "선택" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .menuAnchor(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color(0xFF666666),
                            focusedIndicatorColor = Color(0xFF3a3a3a),
                            unfocusedIndicatorColor = Color(0xFF3a3a3a)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "계급 선택",
                                tint = if (enabled) Color.White else Color(0xFF666666)
                            )
                        },
                        enabled = enabled
                    )

                    ExposedDropdownMenu(
                        expanded = rankExpanded,
                        onDismissRequest = { rankExpanded = false },
                        modifier = Modifier.background(Color(0xFF2a2a2a))
                    ) {
                        ranks.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(text = r, color = Color.White, fontSize = 14.sp) },
                                onClick = {
                                    onRankChange(r)
                                    rankExpanded = false
                                },
                                colors = MenuDefaults.itemColors(textColor = Color.White)
                            )
                        }
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            qualifications.forEach { qual ->
                Button(
                    onClick = { if (enabled) onGradeChange(qual) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (grade == qual) Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF2a2a2a),
                        disabledContentColor = Color(0xFF666666)
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    border = if (grade == qual) null else BorderStroke(1.dp, Color(0xFF4a4a4a)),
                    enabled = enabled
                ) {
                    Text(
                        text = qual,
                        fontSize = 12.sp,
                        fontWeight = if (grade == qual) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }

        SignatureArea(
            signature = signature,
            onSignatureClick = onSignatureClick,
            enabled = enabled,
            widthFraction = 0.33f
        )
    }
}


@Composable
private fun MultiSelectButtonGroup(
    options: List<String>,
    selectedOptions: Set<String>,
    onOptionsChanged: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 5,
    enabled: Boolean = true
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
                    Button(
                        onClick = {
                            if (enabled) {
                                val newSelection = if (selectedOptions.contains(option)) {
                                    selectedOptions - option
                                } else {
                                    selectedOptions + option
                                }
                                onOptionsChanged(newSelection)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedOptions.contains(option)) Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF2a2a2a),
                            disabledContentColor = Color(0xFF666666)
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = if (selectedOptions.contains(option)) null else BorderStroke(1.dp, Color(0xFF4a4a4a)),
                        enabled = enabled
                    ) {
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            fontWeight = if (selectedOptions.contains(option)) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
                repeat(columns - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

