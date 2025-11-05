//ReportHome.kt
package com.example.ssairen_app.ui.screens.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ssairen_app.ui.components.ClickableDarkCard
import com.example.ssairen_app.ui.context.rememberDispatchState

@Composable
fun ReportHome() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val dispatchState = rememberDispatchState()

    // 출동 모달 표시
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
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 50.dp, bottom = 16.dp)
    ) {
        // 상단 타이틀
        Text(
            text = "보고서 메인화면",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 네비게이션 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TabButton(
                text = "내 보고서",
                isSelected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "출동지령 내역",
                isSelected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "관내 보고서 검색",
                isSelected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 탭에 따라 다른 콘텐츠 표시
        when (selectedTab) {
            0 -> ReportListContent()
            1 -> DispatchList()
            2 -> ReportSearchScreen(
                onNavigateToDetail = { report ->
                    println("Report detail: ${report.id}")
                }
            )
        }
    }
}

// ==========================================
// 내 보고서 콘텐츠
// ==========================================
@Composable
private fun ReportListContent() {
    var selectedCardIndex by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        itemsIndexed(listOf(
            ReportData("CB000000000842", "0000000", "정상", 65, "2024-04-05", "감서 소방서 (구급대 차 번호)"),
            ReportData("CB000000000843", "0000001", "정상", 65, "2024-04-05", "감서 소방서 (구급대 차 번호)"),
            ReportData("CB000000000844", "0000002", "정상", 65, "2024-04-05", "감서 소방서 (구급대 차 번호)"),
            ReportData("CB000000000845", "0000003", "정상", 65, "2024-04-05", "감서 소방서 (구급대 차 번호)")
        )) { index, report ->
            ReportCard(
                reportData = report,
                isSelected = selectedCardIndex == index,
                onClick = {
                    selectedCardIndex = if (selectedCardIndex == index) null else index
                }
            )
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF3b7cff) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color(0xFF999999)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color(0xFF3b7cff) else Color(0xFF4a4a4a)
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

data class ReportData(
    val reportNumber: String,
    val patientNumber: String,
    val status: String,
    val progress: Int,
    val date: String,
    val location: String
)

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${reportData.reportNumber} 구급출동 | ${reportData.status}",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Surface(
                    color = Color(0xFF4a4a4a),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${reportData.progress}% 작성중",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { reportData.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF3b7cff),
                trackColor = Color(0xFF3a3a3a),
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "신정운 ${reportData.patientNumber}",
                    color = Color.White,
                    fontSize = 12.sp
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${reportData.date} 지정시간",
                        color = Color(0xFF999999),
                        fontSize = 11.sp
                    )
                    Text(
                        text = reportData.location,
                        color = Color(0xFF999999),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}