package com.example.ssairen_app.ui.screens.emergencyact

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.viewmodel.LogViewModel


// ==========================================
// 출동 인원 데이터 클래스
// ==========================================
data class CrewMemberData(
    val name: String = "",
    val rank: String = "",
    val qualification: String = "",
    val signature: List<Offset> = emptyList()
)

// ==========================================
// 세부상황표 메인 화면
// ==========================================
@Composable
fun ReportDetail(
    modifier: Modifier = Modifier,
    logViewModel: LogViewModel = viewModel()
) {
    val activityLogData by logViewModel.activityLogData.collectAsState()

    // 자가 선택 이송 서명 상태
    var selfTransportSignature by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var showSelfTransportSignatureDialog by remember { mutableStateOf(false) }


    // 미이송 상태
    var nonTransportReason1 by remember { mutableStateOf("") }
    var nonTransportReason2 by remember { mutableStateOf("") }
    var nonTransportReason3 by remember { mutableStateOf("") }


    // 기타 활동 상태
    var otherActivity by remember { mutableStateOf("") }

    // 장애요인 상태
    var obstacle1 by remember { mutableStateOf("") }
    var obstacle2 by remember { mutableStateOf("") }
    var obstacle3 by remember { mutableStateOf("") }


    // 출동인원 상태
    var paramedics by remember { mutableStateOf(listOf(CrewMemberData())) }
    var driver by remember { mutableStateOf(CrewMemberData()) }
    var others by remember { mutableStateOf(CrewMemberData()) }

    // 서명 모달 상태
    var showSignatureDialog by remember { mutableStateOf(false) }
    var signatureTarget by remember { mutableStateOf<SignatureTarget?>(null) }


    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ==== 출동인원 ====/
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "출동인원",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = {
                        paramedics = paramedics + CrewMemberData()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF3b7cff), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "구급대원 추가",
                        tint = Color.White
                    )
                }
            }
        }

        // ==== 구급대원 목록 ====
        itemsIndexed(paramedics) { index, paramedic ->
            CrewMemberSection(
                title = "구급대원",
                crewMember = paramedic,
                onNameChange = { name ->
                    paramedics = paramedics.toMutableList().apply { this[index] = this[index].copy(name = name) }
                },
                onRankChange = { rank ->
                    paramedics = paramedics.toMutableList().apply { this[index] = this[index].copy(rank = rank) }
                },
                onQualificationChange = { qual ->
                    paramedics = paramedics.toMutableList().apply { this[index] = this[index].copy(qualification = qual) }
                },
                onSignatureClick = {
                    signatureTarget = SignatureTarget.Paramedic(index)
                    showSignatureDialog = true
                }
            )
        }

        // ==== 운전요원 ====
        item {
            CrewMemberSection(
                title = "운전요원",
                crewMember = driver,
                onNameChange = { name -> driver = driver.copy(name = name) },
                onRankChange = { rank -> driver = driver.copy(rank = rank) },
                onQualificationChange = { qual -> driver = driver.copy(qualification = qual) },
                onSignatureClick = {
                    signatureTarget = SignatureTarget.Driver
                    showSignatureDialog = true
                }
            )
        }

        // ==== 기타 ====
        item {
            CrewMemberSection(
                title = "기타",
                crewMember = others,
                onNameChange = { name -> others = others.copy(name = name) },
                onRankChange = { rank -> others = others.copy(rank = rank) },
                onQualificationChange = { qual -> others = others.copy(qualification = qual) },
                onSignatureClick = {
                    signatureTarget = SignatureTarget.Others
                    showSignatureDialog = true
                }
            )
        }

        // ==== 자가 선택 이송 섹션 ====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "자가 선택 이송 서명",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "본 구급대는 환자의 추가 손상 및 악화(사망 등) 방지를 위해 응급처치에 적합하고\n최단시간 이내에 이송이 가능한 ○○○병원으로 이송을 권유 하였으나, ○○○씨가 원하는\n병원으로 이송함에 따라 발생하는 민사, 형사상 책임은 지지 않습니다.\n위 내용을 고지합니다.",
                    color = Color(0xFF999999),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF2a2a2a), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.33f)
                            .height(60.dp)
                            .background(Color.Transparent)
                            .clickable { showSelfTransportSignatureDialog = true }
                    ) {
                        // 하단 테두리
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

                        if (selfTransportSignature.isEmpty()) {
                            // 서명이 없을 때 텍스트 표시
                            Text(
                                text = "서명 또는 인",
                                color = Color(0xFF999999),
                                fontSize = 14.sp,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(bottom = 8.dp)
                            )
                        } else {
                            // 서명이 있을 때 Canvas로 서명 표시
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 8.dp)
                            ) {
                                if (selfTransportSignature.size > 1) {
                                    val pathData = Path()

                                    // 서명 좌표의 범위를 구해서 스케일링
                                    val minX = selfTransportSignature.minOfOrNull { it.x } ?: 0f
                                    val maxX = selfTransportSignature.maxOfOrNull { it.x } ?: size.width
                                    val minY = selfTransportSignature.minOfOrNull { it.y } ?: 0f
                                    val maxY = selfTransportSignature.maxOfOrNull { it.y } ?: size.height

                                    val scaleX = if (maxX - minX > 0) size.width / (maxX - minX) else 1f
                                    val scaleY = if (maxY - minY > 0) size.height / (maxY - minY) else 1f
                                    val scale = minOf(scaleX, scaleY) * 0.8f

                                    val offsetX = (size.width - (maxX - minX) * scale) / 2f
                                    val offsetY = (size.height - (maxY - minY) * scale) / 2f

                                    val scaledPoints = selfTransportSignature.map { offset ->
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
        }

        // ==== 미이송 ====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "미이송",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                MultiSelectButtonGroup(
                    options = listOf("취소", "환자 없음", "현장처치", "이송거부", "이송 거절"),
                    selectedOption = nonTransportReason1,
                    onOptionSelected = { nonTransportReason1 = it },
                    columns = 5
                )

                MultiSelectButtonGroup(
                    options = listOf("경찰인계", "이송 불필요", "사망", "병원차", "경찰차"),
                    selectedOption = nonTransportReason2,
                    onOptionSelected = { nonTransportReason2 = it },
                    columns = 5
                )

                MultiSelectButtonGroup(
                    options = listOf("자가용", "택시", "헬기", "기타"),
                    selectedOption = nonTransportReason3,
                    onOptionSelected = { nonTransportReason3 = it },
                    columns = 4
                )
            }
        }

        // ==== 기타 활동 ====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "기타 활동",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                MultiSelectButtonGroup(
                    options = listOf("소방활동", "일련번호", "재난번호"),
                    selectedOption = otherActivity,
                    onOptionSelected = { otherActivity = it },
                    columns = 3
                )
            }
        }

        // ==== 장애요인 ====
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "장애요인",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                MultiSelectButtonGroup(
                    options = listOf("장거리 이송", "보호자 요구", "원거리 병원", "원거리 출동", "환자 과체중"),
                    selectedOption = obstacle1,
                    onOptionSelected = { obstacle1 = it },
                    columns = 5
                )

                MultiSelectButtonGroup(
                    options = listOf("폭우", "없음", "교통정체", "환자위치 불명확", "기관 협조 미흡"),
                    selectedOption = obstacle2,
                    onOptionSelected = { obstacle2 = it },
                    columns = 5
                )

                MultiSelectButtonGroup(
                    options = listOf("언어폭력", "만취자", "폭행", "폭설", "기타"),
                    selectedOption = obstacle3,
                    onOptionSelected = { obstacle3 = it },
                    columns = 5
                )
            }
        }
    }

    // 서명 모달 (출동인원)
    if (showSignatureDialog) {
        SignatureDialog(
            onDismiss = { showSignatureDialog = false },
            onConfirm = { signature ->
                when (val target = signatureTarget) {
                    is SignatureTarget.Paramedic -> {
                        paramedics = paramedics.toMutableList().apply {
                            this[target.index] = this[target.index].copy(signature = signature)
                        }
                    }
                    is SignatureTarget.Driver -> {
                        driver = driver.copy(signature = signature)
                    }
                    is SignatureTarget.Others -> {
                        others = others.copy(signature = signature)
                    }
                    else -> {}
                }
                showSignatureDialog = false
            }
        )
    }

    // 서명 모달 (자가 이송)
    if (showSelfTransportSignatureDialog) {
        SignatureDialog(
            onDismiss = { showSelfTransportSignatureDialog = false },
            onConfirm = { signature ->
                selfTransportSignature = signature
                showSelfTransportSignatureDialog = false
            }
        )
    }
}


// ==========================================
// 서명 대상 식별자
// ==========================================
sealed class SignatureTarget {
    data class Paramedic(val index: Int) : SignatureTarget()
    object Driver : SignatureTarget()
    object Others : SignatureTarget()
}

// ==========================================
// 단일 선택 버튼 그룹
// ==========================================
@Composable
private fun MultiSelectButtonGroup(
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
                    Button(
                        onClick = { onOptionSelected(option) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedOption == option) Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = if (selectedOption == option) null else BorderStroke(1.dp, Color(0xFF4a4a4a))
                    ) {
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            fontWeight = if (selectedOption == option) FontWeight.Medium else FontWeight.Normal,
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

// ==========================================
// 출동 인원 섹션
// ==========================================
@Composable
private fun CrewMemberSection(
    title: String,
    crewMember: CrewMemberData,
    onNameChange: (String) -> Unit,
    onRankChange: (String) -> Unit,
    onQualificationChange: (String) -> Unit,
    onSignatureClick: () -> Unit
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

        // 성명 + 계급(p)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 성명
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "성명",
                    color = Color.White,
                    fontSize = 14.sp
                )
                TextField(
                    value = crewMember.name,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedIndicatorColor = Color(0xFF3a3a3a),
                        unfocusedIndicatorColor = Color(0xFF3a3a3a),
                        cursorColor = Color(0xFF3b7cff)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                    singleLine = true
                )
            }

            // 계급(p) - Dropdown
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "계급(p)",
                    color = Color.White,
                    fontSize = 14.sp
                )

                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = rankExpanded,
                    onExpandedChange = { rankExpanded = it }
                ) {
                    TextField(
                        value = crewMember.rank.ifEmpty { "선택" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .menuAnchor(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color(0xFF3a3a3a),
                            unfocusedIndicatorColor = Color(0xFF3a3a3a)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "계급 선택",
                                tint = Color.White
                            )
                        }
                    )

                    ExposedDropdownMenu(
                        expanded = rankExpanded,
                        onDismissRequest = { rankExpanded = false },
                        modifier = Modifier.background(Color(0xFF2a2a2a))
                    ) {
                        ranks.forEach { rank ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = rank,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                },
                                onClick = {
                                    onRankChange(rank)
                                    rankExpanded = false
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }

        // 자격증 버튼
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            qualifications.forEach { qual ->
                Button(
                    onClick = { onQualificationChange(qual) },
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (crewMember.qualification == qual) Color(0xFF3b7cff) else Color(0xFF3a3a3a),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    border = if (crewMember.qualification == qual) null else BorderStroke(1.dp, Color(0xFF4a4a4a))
                ) {
                    Text(
                        text = qual,
                        fontSize = 12.sp,
                        fontWeight = if (crewMember.qualification == qual) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }
        }

        // 서명 또는 인
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.33f)
                    .height(60.dp)
                    .background(Color.Transparent)
                    .clickable { onSignatureClick() }
            ) {
                // 하단 테두리
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

                if (crewMember.signature.isEmpty()) {
                    // 서명이 없을 때 텍스트 표시
                    Text(
                        text = "서명 또는 인",
                        color = Color(0xFF999999),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(bottom = 8.dp)
                    )
                } else {
                    // 서명이 있을 때 Canvas로 서명 표시
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 8.dp)
                    ) {
                        if (crewMember.signature.size > 1) {
                            val pathData = Path()

                            // 서명 좌표의 범위를 구해서 스케일링
                            val minX = crewMember.signature.minOfOrNull { it.x } ?: 0f
                            val maxX = crewMember.signature.maxOfOrNull { it.x } ?: size.width
                            val minY = crewMember.signature.minOfOrNull { it.y } ?: 0f
                            val maxY = crewMember.signature.maxOfOrNull { it.y } ?: size.height

                            val scaleX = if (maxX - minX > 0) size.width / (maxX - minX) else 1f
                            val scaleY = if (maxY - minY > 0) size.height / (maxY - minY) else 1f
                            val scale = minOf(scaleX, scaleY) * 0.8f

                            val offsetX = (size.width - (maxX - minX) * scale) / 2f
                            val offsetY = (size.height - (maxY - minY) * scale) / 2f

                            val scaledPoints = crewMember.signature.map { offset ->
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
}

// ==========================================
// 서명 다이얼로그
// ==========================================
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
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2a2a2a)
            )
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

                // 서명 영역
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

                        // 현재 그리는 중인 경로
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

                // 버튼
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
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
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
private fun ReportDetailPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1a1a1a)
    ) {
        ReportDetail()
    }
}
