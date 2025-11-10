// ReportHome.kt (무한 스크롤 개선 버전 - 작성 상태 UI 제거)
package com.example.ssairen_app.ui.screens.report

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ssairen_app.ui.components.ClickableDarkCard
import com.example.ssairen_app.ui.context.rememberDispatchState
import com.example.ssairen_app.ui.navigation.ReportNavigationBar
import com.example.ssairen_app.viewmodel.ReportViewModel
import com.example.ssairen_app.viewmodel.CreateReportState
import com.example.ssairen_app.viewmodel.ReportListState

@Composable
fun ReportHome(
    onNavigateToActivityLog: (emergencyReportId: Int, isReadOnly: Boolean) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {},
    reportViewModel: ReportViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val dispatchState = rememberDispatchState()

    val createReportState by reportViewModel.createReportState.observeAsState(CreateReportState.Idle)
    val reportListState by reportViewModel.reportListState.observeAsState(ReportListState.Idle)
    val isLoadingMore by reportViewModel.isLoadingMore.observeAsState(false)
    val hasMoreData by reportViewModel.hasMoreData.observeAsState(true)

    LaunchedEffect(Unit) {
        reportViewModel.getReports()
    }

    // ✅ 임시로 주석처리 - API 대신 모달창에서 직접 이동
//    LaunchedEffect(createReportState) {
//        if (createReportState is CreateReportState.Success) {
//            val reportId = (createReportState as CreateReportState.Success).reportData.emergencyReportId
//            reportViewModel.getReports()
//            onNavigateToActivityLog(reportId, false)  // 새로 생성된 보고서는 수정 가능
//            reportViewModel.resetCreateState()
//        }
//    }
//
//    if (createReportState is CreateReportState.Error) {
//        val errorMessage = (createReportState as CreateReportState.Error).message
//        AlertDialog(
//            onDismissRequest = { reportViewModel.resetCreateState() },
//            title = { Text("일지 생성 실패", color = Color.White) },
//            text = { Text(errorMessage, color = Color.White) },
//            confirmButton = {
//                TextButton(onClick = { reportViewModel.resetCreateState() }) {
//                    Text("확인")
//                }
//            },
//            containerColor = Color(0xFF2a2a2a)
//        )
//    }
//
//    if (createReportState is CreateReportState.Loading) {
//        AlertDialog(
//            onDismissRequest = { },
//            title = { Text("일지 생성 중...", color = Color.White) },
//            text = {
//                Row(
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    CircularProgressIndicator()
//                }
//            },
//            confirmButton = { },
//            containerColor = Color(0xFF2a2a2a)
//        )
//    }

    if (dispatchState.showDispatchModal && dispatchState.activeDispatch != null) {
        val dispatch = dispatchState.activeDispatch!!
        DispatchDetail(
            dispatchData = DispatchDetailData(
                dispatchNumber = dispatch.id,
                status = "실전/1차",
                type = dispatch.type,
                area = "관할구역",
                location = dispatch.location,
                reporter = "신고자명",
                reporterPhone = "010-0000-0000",
                dispatchTime = dispatch.date,
                address = dispatch.location,
                cause = "사고 원인 정보"
            ),
            onDismiss = {
                dispatchState.closeDispatchModal()
            },
            onCreateNewReport = {
                dispatchState.closeDispatchModal()
                // ✅ 모달창의 emergencyReportId 사용 (23번으로 하드코딩됨)
                onNavigateToActivityLog(dispatch.emergencyReportId, false)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 50.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "보고서 메인화면",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF666666))
            ) {
                Text(
                    text = "로그아웃",
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ReportNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = {
                Log.d("ReportHome", "🔵 탭 클릭됨: $it")
                selectedTab = it
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                Log.d("ReportHome", "✅ ReportListContent 표시 중")
                ReportListContent(
                    reportListState = reportListState,
                    onRefresh = { reportViewModel.getReports() },
                    onLoadMore = { reportViewModel.loadMoreReports() },
                    onReportClick = { emergencyReportId ->
                        onNavigateToActivityLog(emergencyReportId, true)  // GET으로 불러온 보고서는 읽기 전용
                    },
                    isLoadingMore = isLoadingMore,
                    hasMoreData = hasMoreData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
            1 -> {
                Log.d("ReportHome", "⚠️ DispatchList 표시 중 (목 데이터)")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    DispatchList()
                }
            }
            2 -> {
                Log.d("ReportHome", "🔍 ReportSearchScreen 표시 중")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    ReportSearchScreen(
                        onNavigateToDetail = { report ->
                            println("Report detail: ${report.id}")
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// 내 보고서 콘텐츠
// ==========================================
@Composable
private fun ReportListContent(
    reportListState: ReportListState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onReportClick: (Int) -> Unit,
    isLoadingMore: Boolean,
    hasMoreData: Boolean,
    modifier: Modifier = Modifier
) {
    var selectedCardIndex by remember { mutableStateOf<Int?>(null) }
    val listState = rememberLazyListState()

    when (reportListState) {
        is ReportListState.Idle -> {
            Log.d("ReportHome", "⭕ ReportListState.Idle")
        }

        is ReportListState.Loading -> {
            Log.d("ReportHome", "⏳ ReportListState.Loading")
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        is ReportListState.Error -> {
            Log.d("ReportHome", "❌ ReportListState.Error: ${reportListState.message}")
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = reportListState.message,
                    color = Color(0xFFFF6B6B),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3b7cff)
                    )
                ) {
                    Text("다시 시도")
                }
            }
        }

        is ReportListState.Success -> {
            val reportsData = reportListState.reportListData

            // ✅ 모든 보고서 표시 (필터링 제거)
            val reports = reportsData.emergencyReports

            Log.d("ReportHome", "✅ ReportListState.Success - 보고서 개수: ${reports.size}")

            if (reports.isEmpty()) {
                Box(
                    modifier = modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "보고서가 없습니다",
                        color = Color(0xFF999999),
                        fontSize = 14.sp
                    )
                }
            } else {
                // ✅ 원래 코드: 무한 스크롤 감지
                /*
                LaunchedEffect(listState, reports.size) {
                    snapshotFlow {
                        val layoutInfo = listState.layoutInfo
                        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                        val lastVisibleIndex = lastVisibleItem?.index ?: -1
                        val totalItems = layoutInfo.totalItemsCount

                        Log.d("ReportHome", "📊 스크롤 상태")
                        Log.d("ReportHome", "   - 마지막 보이는 인덱스: $lastVisibleIndex")
                        Log.d("ReportHome", "   - 전체 아이템 수: $totalItems")
                        Log.d("ReportHome", "   - hasMoreData: $hasMoreData")
                        Log.d("ReportHome", "   - isLoadingMore: $isLoadingMore")

                        lastVisibleIndex to totalItems
                    }.collect { (lastVisibleIndex, totalItems) ->
                        // ✅ 마지막에서 3번째 아이템에 도달하면 로드
                        if (lastVisibleIndex >= totalItems - 3 && hasMoreData && !isLoadingMore) {
                            Log.d("ReportHome", "🔄 무한 스크롤 트리거!")
                            Log.d("ReportHome", "   - 트리거 인덱스: $lastVisibleIndex")
                            Log.d("ReportHome", "   - 전체 개수: $totalItems")
                            onLoadMore()
                        }
                    }
                }
                */

                LazyColumn(
                    state = listState,
                    modifier = modifier,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(reports, key = { _, report -> report.id }) { index, report ->
                        val dispatchInfo = report.dispatchInfo

                        val formattedDate = try {
                            dispatchInfo.date.substringBefore('T')
                        } catch (e: Exception) {
                            dispatchInfo.date
                        }

                        ReportCard(
                            reportData = ReportData(
                                reportNumber = dispatchInfo.disasterNumber,
                                patientNumber = report.id.toString().padStart(7, '0'),
                                status = dispatchInfo.disasterType,
                                progress = 0,  // ✅ 사용 안 하지만 호환성 유지
                                date = formattedDate,
                                location = dispatchInfo.fireStateInfo.name,
                                locationAddress = dispatchInfo.locationAddress
                            ),
                            isSelected = selectedCardIndex == index,
                            onClick = {
                                selectedCardIndex = if (selectedCardIndex == index) null else index
                                onReportClick(report.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

data class ReportData(
    val reportNumber: String,
    val patientNumber: String,
    val status: String,
    val progress: Int = 0,  // ✅ 더 이상 사용 안 하지만 호환성 유지
    val date: String,
    val location: String,
    val locationAddress: String = ""
)

// ✅ 작성 상태 UI 제거된 ReportCard
@Composable
private fun ReportCard(
    reportData: ReportData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ClickableDarkCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        isSelected = isSelected
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // ✅ 상단: 재난번호 | 상태 + 작성완료 뱃지
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${reportData.reportNumber} | ${reportData.status}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                // 작성완료 뱃지
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF28a745),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "작성완료",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✅ 하단: 보고서 정보
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "보고서 ID: ${reportData.patientNumber}",
                    color = Color.White,
                    fontSize = 12.sp
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = reportData.date,
                        color = Color(0xFF999999),
                        fontSize = 11.sp
                    )
                    Text(
                        text = reportData.location,
                        color = Color(0xFF999999),
                        fontSize = 11.sp
                    )
                    if (reportData.locationAddress.isNotEmpty()) {
                        Text(
                            text = reportData.locationAddress,
                            color = Color(0xFF999999),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}