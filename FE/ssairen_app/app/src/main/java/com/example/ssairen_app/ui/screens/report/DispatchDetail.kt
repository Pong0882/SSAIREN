//DispatchDetail.kt
package com.example.ssairen_app.ui.screens.report

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// ==========================================
// 출동지령 상세 정보 데이터 클래스
// ==========================================
data class DispatchDetailData(
    val dispatchNumber: String,      // 재난번호
    val status: String,              // 실전/1차
    val type: String,                // 화재/고층건물
    val area: String,                // 관할
    val location: String,            // 오창안전센터
    val reporter: String,            // 신고자
    val reporterPhone: String,       // 신고자 전화번호
    val dispatchTime: String,        // 출동지령일시
    val address: String,             // 주소
    val cause: String                // 사고 원인
)

// ==========================================
// 출동지령 상세 정보 화면 (모달 스타일)
// ==========================================
@Composable
fun DispatchDetail(
    dispatchData: DispatchDetailData,
    onDismiss: () -> Unit,
    onCreateNewReport: () -> Unit = {}
) {
    Log.d("DispatchDetail", "========================================")
    Log.d("DispatchDetail", "🎨 DispatchDetail 렌더링")
    Log.d("DispatchDetail", "dispatchNumber: ${dispatchData.dispatchNumber}")
    Log.d("DispatchDetail", "onCreateNewReport: $onCreateNewReport")
    Log.d("DispatchDetail", "========================================")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF2a2a2a)
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // 빨간색 헤더
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFff3b30))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "출동지령 상세 정보",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // 스크롤 가능한 콘텐츠
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 재난번호 & 구분/출동차수
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoColumn(
                            label = "재난번호",
                            value = dispatchData.dispatchNumber,
                            modifier = Modifier.weight(1f)
                        )
                        InfoColumn(
                            label = "구분/출동차수",
                            value = dispatchData.status,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 층별/분류 & 관할
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoColumn(
                            label = "층별 / 분류",
                            value = dispatchData.type,
                            modifier = Modifier.weight(1f)
                        )
                        InfoColumn(
                            label = "관할",
                            value = dispatchData.area,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 신고자 & 출동지령일시
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoColumn(
                            label = "신고자",
                            value = "${dispatchData.reporter}(${dispatchData.reporterPhone})",
                            modifier = Modifier.weight(1f)
                        )
                        InfoColumn(
                            label = "출동지령일시",
                            value = dispatchData.dispatchTime,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 주소
                    InfoColumn(
                        label = "주소",
                        value = dispatchData.address,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 사고 원인
                    InfoColumn(
                        label = "사고 원인",
                        value = dispatchData.cause,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 새 일지 등록 버튼
                Button(
                    onClick = {
                        Log.d("DispatchDetail", "╔════════════════════════════════════════╗")
                        Log.d("DispatchDetail", "║   🔵 버튼 클릭됨!                     ║")
                        Log.d("DispatchDetail", "╚════════════════════════════════════════╝")

                        try {
                            Log.d("DispatchDetail", "📡 onCreateNewReport 콜백 호출 시작...")
                            onCreateNewReport()
                            Log.d("DispatchDetail", "✅ onCreateNewReport 콜백 호출 완료!")
                        } catch (e: Exception) {
                            Log.e("DispatchDetail", "❌ 콜백 실행 중 에러: ${e.message}", e)
                        }

                        Log.d("DispatchDetail", "========================================")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFff3b30)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "새 일지 등록",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ==========================================
// 정보 컬럼 (라벨 + 값)
// ==========================================
@Composable
private fun InfoColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF999999),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            lineHeight = 20.sp
        )
    }
}