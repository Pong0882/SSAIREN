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

// ==========================================
// 출동지령 내역 데이터 클래스
// ==========================================
data class DispatchData(
    val dispatchNumber: String,     // 재난번호
    val status: String,              // 상태
    val date: String,                // 날짜
    val time: String,                // 시간
    val location: String,            // 위치
    val distance: String             // 거리
)

// ==========================================
// 출동지령 내역 콘텐츠 (네비게이션 바 없이 내용만)
// ==========================================
@Composable
fun DispatchList() {
    var selectedCardIndex by remember { mutableStateOf<Int?>(null) }
    var showDetail by remember { mutableStateOf(false) }
    var selectedDispatch by remember { mutableStateOf<DispatchData?>(null) }

    if (showDetail && selectedDispatch != null) {
        // 상세 화면 표시
        DispatchDetail(
            dispatchData = DispatchDetailData(
                dispatchNumber = selectedDispatch!!.dispatchNumber,
                status = "실전 / 1차",
                type = "화재 / 고층건물(3층이상, 아파트)",
                area = "오창안전센터",
                location = "오창안전센터",
                reporter = "김우리 (010-0000-0000)",
                reporterPhone = "010-0000-0000",
                dispatchTime = selectedDispatch!!.date + " " + selectedDispatch!!.time,
                address = "충청북도 청주시 청원군 오창읍 양청길 오창캠퍼스 융합기술원",
                cause = "4층 창고에서 화재 발생"
            ),
            onDismiss = { showDetail = false }
        )
    } else {
        // 리스트 화면 표시
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            itemsIndexed(listOf(
                DispatchData(
                    "CB000000000662",
                    "정상",
                    "2023-11-13",
                    "09:16",
                    "위치 : 충청북도 청주시 청원군 오창읍 양청길 오창캠퍼스 융합기술원",
                    "거리 50km"
                ),
                DispatchData(
                    "CB000000000843",
                    "정상",
                    "2024-04-05",
                    "지정시간",
                    "위치 : 서울특별시 양천구 대림대로 2/2",
                    "거리 50km"
                ),
                DispatchData(
                    "CB000000000844",
                    "정상",
                    "2024-04-05",
                    "지정시간",
                    "위치 : 서울특별시 양천구 대림대로 2/2",
                    "거리 50km"
                ),
                DispatchData(
                    "CB000000000845",
                    "정상",
                    "2024-04-05",
                    "지정시간",
                    "위치 : 서울특별시 양천구 대림대로 2/2",
                    "거리 50km"
                )
            )) { index, dispatch ->
                DispatchCard(
                    dispatchData = dispatch,
                    isSelected = selectedCardIndex == index,
                    onClick = {
                        selectedCardIndex = if (selectedCardIndex == index) null else index
                        selectedDispatch = dispatch
                        showDetail = true  // 상세 화면으로 전환
                    }
                )
            }
        }
    }
}

// ==========================================
// 출동지령 카드
// ==========================================
@Composable
private fun DispatchCard(
    dispatchData: DispatchData,
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
            // 상단: 재난번호 & 상태 & 날짜/시간
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 왼쪽: 재난번호 & 상태
                Column {
                    Text(
                        text = "${dispatchData.dispatchNumber} 구급출동 | ${dispatchData.status}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 오른쪽: 날짜 & 시간
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${dispatchData.date} ${dispatchData.time}",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 하단: 위치 & 거리 정보
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = dispatchData.location,
                    color = Color(0xFF999999),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = dispatchData.distance,
                    color = Color(0xFF999999),
                    fontSize = 12.sp
                )
            }
        }
    }
}