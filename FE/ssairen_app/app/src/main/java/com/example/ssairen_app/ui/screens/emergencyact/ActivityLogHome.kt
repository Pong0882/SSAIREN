// ActivityLogHome(네비게이션이 2개가 있는 진입점 홈).kt
package com.example.ssairen_app.ui.screens.emergencyact

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.ui.navigation.ActivityLogNavigationBar
import com.example.ssairen_app.ui.navigation.EmergencyNav
import com.example.ssairen_app.viewmodel.LogViewModel
import com.example.ssairen_app.viewmodel.ActivityViewModel
import com.example.ssairen_app.viewmodel.SaveState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Check
import com.example.ssairen_app.viewmodel.ReportViewModel
import com.example.ssairen_app.viewmodel.CompleteReportState
import androidx.compose.runtime.livedata.observeAsState


@Composable
fun ActivityLogHome(
    emergencyReportId: Int,
    initialTab: Int = 0,
    isReadOnly: Boolean = false,
    onNavigateBack: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToReportHome: () -> Unit = {},
    onNavigateToSummation: (Int) -> Unit = {},  // ✅ 수정 1: (Int) 추가
    onNavigateToMemo: () -> Unit = {},
    onNavigateToHospitalSearch: () -> Unit = {},
    viewModel: LogViewModel = viewModel(),
    activityViewModel: ActivityViewModel = viewModel(),
    reportViewModel: ReportViewModel = viewModel()
) {
    var selectedLogTab by remember { mutableIntStateOf(initialTab) }
    var selectedBottomTab by remember { mutableIntStateOf(1) }

    val activityLogData by viewModel.activityLogData.collectAsState()
    val lastSavedTime by viewModel.lastSavedTime.collectAsState()
    val saveState by viewModel.saveState.collectAsState()

    // ✅ Snackbar 상태
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ 작성완료 상태 관찰
    val completeReportState by reportViewModel.completeReportState.observeAsState(CompleteReportState.Idle)

    LaunchedEffect(completeReportState) {
        when (completeReportState) {
            is CompleteReportState.Success -> {
                Log.d("ActivityLogHome", "✅ 작성 완료 성공")

                // 1. Snackbar 표시
                snackbarHostState.showSnackbar(
                    message = "보고서 작성이 완료되었습니다",
                    duration = SnackbarDuration.Short
                )

                // 2. 상태 초기화
                reportViewModel.resetCompleteState()

                // 3. ReportHome으로 이동 (ReportHome에서 자동으로 새로고침됨)
                Log.d("ActivityLogHome", "🏠 ReportHome으로 이동")
                onNavigateToReportHome()
            }
            is CompleteReportState.Error -> {
                val message = (completeReportState as CompleteReportState.Error).message
                Log.e("ActivityLogHome", "❌ 작성 완료 실패: $message")
                snackbarHostState.showSnackbar(
                    message = "작성 완료 실패: $message",
                    duration = SnackbarDuration.Long
                )
                reportViewModel.resetCompleteState()
            }
            else -> {}
        }
    }

    // ✅ emergencyReportId를 두 ViewModel에 모두 설정
    LaunchedEffect(emergencyReportId) {
        if (emergencyReportId > 0) {
            Log.d("ActivityLogHome", "📝 emergencyReportId 설정: $emergencyReportId")

            // ActivityViewModel에 설정
            activityViewModel.setEmergencyReportId(emergencyReportId)
            com.example.ssairen_app.viewmodel.ActivityViewModel.setGlobalReportId(emergencyReportId)

            // ✅ LogViewModel에도 설정
            viewModel.setEmergencyReportId(emergencyReportId)
        } else {
            Log.w("ActivityLogHome", "⚠️ 유효하지 않은 emergencyReportId: $emergencyReportId")
        }
    }

    // ✅ SaveState 관찰 및 UI 피드백
    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                val message = (saveState as SaveState.Success).message
                Log.d("ActivityLogHome", "✅ 저장 성공: $message")
                snackbarHostState.showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Short
                )
            }
            is SaveState.Error -> {
                val message = (saveState as SaveState.Error).message
                Log.e("ActivityLogHome", "❌ 저장 실패: $message")
                snackbarHostState.showSnackbar(
                    message = "저장 실패: $message",
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }

    // ✅ 탭 변경 전에 현재 데이터를 백엔드에 저장하는 함수
    fun saveCurrentTabToBackend() {
        if (isReadOnly) {
            Log.d("ActivityLogHome", "🔒 읽기 전용 모드 - 저장 차단")
            return
        }
        Log.d("ActivityLogHome", "💾 탭 변경 감지 - 백엔드 저장 시작")
        Log.d("ActivityLogHome", "   - 현재 탭: $selectedLogTab")

        viewModel.saveToBackend(selectedLogTab)

        Log.d("ActivityLogHome", "✅ 백엔드 저장 요청 완료")
    }

    Box(modifier = Modifier.fillMaxSize()) {  // ⭐ Scaffold → Box
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1a1a1a))
                .statusBarsPadding()
            // .padding(paddingValues) 삭제  // ⭐ 이 줄 삭제
        ) {
            // 1. 상단 타이틀 + 뒤로가기
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "구급활동일지",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (lastSavedTime.isNotEmpty()) {
                                Text(
                                    text = "마지막 저장: $lastSavedTime",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                            // ✅ 저장 중 표시
                            if (saveState is SaveState.Saving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF3b7cff)
                                )
                                Text(
                                    text = "저장 중...",
                                    color = Color(0xFF3b7cff),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onNavigateToReportHome) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "보고서 홈",
                            tint = Color.White
                        )
                    }
                    if (!isReadOnly) {
                        IconButton(
                            onClick = {
                                Log.d("ActivityLogHome", "📝 작성완료 버튼 클릭")

                                // 코루틴으로 순차 실행
                                CoroutineScope(Dispatchers.Main).launch {
                                    // 1. 현재 탭 저장
                                    viewModel.saveToBackend(selectedLogTab)

                                    // 2. 저장 완료 대기 (최대 2초)
                                    var waitCount = 0
                                    while (saveState is SaveState.Saving && waitCount < 20) {
                                        kotlinx.coroutines.delay(100)
                                        waitCount++
                                    }

                                    // 3. 작성 완료 API 호출
                                    Log.d("ActivityLogHome", "💾 저장 완료, 작성완료 API 호출")
                                    reportViewModel.completeReport(emergencyReportId)
                                }
                            },
                            enabled = completeReportState !is CompleteReportState.Loading
                        ) {
                            if (completeReportState is CompleteReportState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF28a745)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "작성완료",
                                    tint = Color(0xFF28a745),  // 초록색
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 8개 탭 네비게이션
            ActivityLogNavigationBar(
                selectedTab = selectedLogTab,
                onTabSelected = { newTab ->
                    if (selectedLogTab != newTab) {
                        // 1️⃣ PATCH: 이전 탭 데이터 저장
                        saveCurrentTabToBackend()

                        // 2️⃣ 탭 전환
                        selectedLogTab = newTab

                        // 3️⃣ GET: 새 탭 데이터 불러오기
                        Log.d("ActivityLogHome", "🔍 GET 요청 시작 - emergencyReportId: $emergencyReportId, 탭: $newTab")
                        when (newTab) {
                            0 -> {
                                Log.d("ActivityLogHome", "📞 환자정보 조회 호출")
                                activityViewModel.getPatientInfo(emergencyReportId)
                            }
                            1 -> {
                                Log.d("ActivityLogHome", "📞 구급출동 조회 호출")
                                activityViewModel.getDispatch(emergencyReportId)
                            }
                            2 -> {
                                Log.d("ActivityLogHome", "📞 환자발생유형 조회 호출")
                                activityViewModel.getPatientType(emergencyReportId)
                            }
                            3 -> {
                                Log.d("ActivityLogHome", "📞 환자평가 조회 호출")
                                activityViewModel.getPatientEva(emergencyReportId)
                            }
                            4 -> {
                                Log.d("ActivityLogHome", "📞 응급처치 조회 호출")
                                activityViewModel.getFirstAid(emergencyReportId)
                            }
                            5 -> {
                                Log.d("ActivityLogHome", "📞 의료지도 조회 호출")
                                activityViewModel.getMedicalGuidance(emergencyReportId)
                            }
                            6 -> {
                                Log.d("ActivityLogHome", "📞 환자이송 조회 호출")
                                activityViewModel.getTransport(emergencyReportId)
                            }
                            7 -> {
                                Log.d("ActivityLogHome", "📞 세부사항 조회 호출")
                                activityViewModel.getDetailReport(emergencyReportId)
                            }
                        }

                        Log.d("ActivityLogHome", "📑 상단 탭 변경 완료: $selectedLogTab → $newTab")
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. 내용 영역
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedLogTab) {
                    0 -> PatientInfo(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    1 -> DispatchSection(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    2 -> PatientType(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    3 -> PatientEva(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    4 -> FirstAid(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    5 -> MedicalGuidance(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    6 -> PatientTransport(
                        viewModel = viewModel,
                        data = activityLogData,
                        isReadOnly = isReadOnly
                    )
                    7 -> {
                        ReportDetail(
                            viewModel = viewModel,
                            data = activityLogData,
                            isReadOnly = isReadOnly
                        )

                        DisposableEffect(Unit) {
                            onDispose {
                                CoroutineScope(Dispatchers.IO).launch {
                                    Log.d("ActivityLogHome", "🔄 세부사항 탭 벗어남 - 백그라운드 저장")
                                    viewModel.saveDetailReportSection(activityViewModel)
                                }
                            }
                        }
                    }
                }
            }

            // 4. 하단 네비게이션
            EmergencyNav(
                selectedTab = selectedBottomTab,
                onTabSelected = { newTab ->
                    if (selectedBottomTab != newTab) {
                        // 1️⃣ PATCH: 현재 상단 탭 데이터 저장
                        saveCurrentTabToBackend()

                        // 2️⃣ 하단 탭 전환
                        selectedBottomTab = newTab

                        Log.d("ActivityLogHome", "📑 하단 탭 변경: $selectedBottomTab → $newTab")

                        when (newTab) {
                            0 -> {
                                // 홈으로 이동 (GET 불필요)
                                onNavigateToHome()
                            }
                            1 -> {
                                // 구급활동일지로 복귀
                                // 3️⃣ GET: 현재 상단 탭 데이터 다시 불러오기
                                when (selectedLogTab) {
                                    0 -> activityViewModel.getPatientInfo(emergencyReportId)
                                    1 -> activityViewModel.getDispatch(emergencyReportId)
                                    2 -> activityViewModel.getPatientType(emergencyReportId)
                                    3 -> activityViewModel.getPatientEva(emergencyReportId)
                                    4 -> activityViewModel.getFirstAid(emergencyReportId)
                                    5 -> activityViewModel.getMedicalGuidance(emergencyReportId)
                                    6 -> activityViewModel.getTransport(emergencyReportId)
                                    7 -> activityViewModel.getDetailReport(emergencyReportId)
                                }
                            }
                            2 -> {
                                // 요약으로 이동 (emergencyReportId 전달)
                                onNavigateToSummation(emergencyReportId)  // ✅ 수정 2: ID 전달
                            }
                            3 -> {
                                // 메모로 이동
                                onNavigateToMemo()
                            }
                            4 -> {
                                // 병원이송으로 이동
                                onNavigateToHospitalSearch()
                            }
                        }
                    }
                }
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF2a2a2a),
                contentColor = Color.White
            )
        }
    }
}