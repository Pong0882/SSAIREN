package com.example.ssairen_app.ui.screens.emergencyact

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.ui.components.ClickableDarkCard
import com.example.ssairen_app.viewmodel.HospitalSearchViewModel
import com.example.ssairen_app.viewmodel.HospitalAiRecommendationState
import com.example.ssairen_app.viewmodel.ActivityViewModel
import kotlinx.coroutines.delay

// ==========================================
// 병원 상태 enum (UI 표시용)
// ==========================================
enum class HospitalStatus(val displayName: String, val color: Color) {
    PENDING("요청중", Color(0xFF999999)),
    ACCEPTED("수용 가능", Color(0xFF34c759)),
    REJECTED("거절", Color(0xFFff3b30)),
    CALLREQUEST("전화 요망", Color(0xFFffcc00))
}

// ==========================================
// 병원 검색 메인 화면
// ==========================================
@Composable
fun HospitalSearch(
    modifier: Modifier = Modifier,
    hospitalSearchViewModel: HospitalSearchViewModel = viewModel(),
    activityViewModel: ActivityViewModel = viewModel()
) {
    var selectedHospitalId by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: 검색 완료, 1: 선정된 병원

    val aiRecommendationState by hospitalSearchViewModel.aiRecommendationState.collectAsState()
    val hospitals by hospitalSearchViewModel.hospitals.collectAsState()
    val emergencyReportId = activityViewModel.currentEmergencyReportId.observeAsState()

    // 화면 진입 시 AI 병원 추천 자동 호출
    LaunchedEffect(Unit) {
        val reportId = emergencyReportId.value
        android.util.Log.d("HospitalSearch", "========================================")
        android.util.Log.d("HospitalSearch", "LaunchedEffect 실행됨")
        android.util.Log.d("HospitalSearch", "emergencyReportId.value: $reportId")
        android.util.Log.d("HospitalSearch", "========================================")

        // ⚠️ 임시: emergencyReportId가 없으면 테스트용 ID 사용
        val testReportId = reportId ?: 1

        if (testReportId != 0) {
            // 하드코딩된 위도/경도 (예시값)
            val latitude = 37.5062528
            val longitude = 127.0317056
            val radius = 10

            android.util.Log.d("HospitalSearch", "✅ AI 병원 추천 API 호출 시작!")
            android.util.Log.d("HospitalSearch", "   - reportId: $testReportId")
            android.util.Log.d("HospitalSearch", "   - 위치: ($latitude, $longitude)")

            hospitalSearchViewModel.requestAiHospitalRecommendation(
                emergencyReportId = testReportId.toLong(),
                latitude = latitude,
                longitude = longitude,
                radius = radius
            )
        } else {
            android.util.Log.e("HospitalSearch", "❌ emergencyReportId가 0입니다!")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
    ) {
        when (aiRecommendationState) {
            is HospitalAiRecommendationState.Idle -> {
                // 초기 상태
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "병원 검색 준비 중...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            is HospitalAiRecommendationState.Loading -> {
                // 로딩 중 (AI 추론 중)
                SearchingScreen(
                    hospitals = hospitals,
                    selectedHospitalId = selectedHospitalId,
                    onHospitalClick = { selectedHospitalId = if (selectedHospitalId == it) null else it }
                )
            }

            is HospitalAiRecommendationState.Success -> {
                // AI 추천 성공 - 검색 완료 화면
                SearchCompletedScreen(
                    hospitals = hospitals,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    selectedHospitalId = selectedHospitalId,
                    onHospitalClick = { selectedHospitalId = if (selectedHospitalId == it) null else it }
                )
            }

            is HospitalAiRecommendationState.Error -> {
                // 에러 발생
                val errorMessage = (aiRecommendationState as HospitalAiRecommendationState.Error).message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "병원 검색 실패",
                            color = Color(0xFFff3b30),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = errorMessage,
                            color = Color(0xFF999999),
                            fontSize = 14.sp
                        )
                        Button(
                            onClick = {
                                val reportId = emergencyReportId.value
                                if (reportId != null && reportId != 0) {
                                    hospitalSearchViewModel.requestAiHospitalRecommendation(
                                        emergencyReportId = reportId.toLong(),
                                        latitude = 37.5062528,
                                        longitude = 127.0317056,
                                        radius = 10
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3b7cff)
                            )
                        ) {
                            Text("다시 시도")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 검색 중 화면
// ==========================================
@Composable
private fun SearchingScreen(
    hospitals: List<com.example.ssairen_app.data.model.response.HospitalSelectionInfo>,
    selectedHospitalId: Int?,
    onHospitalClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 상단 제목 + 로딩 스피너
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "검색된 병원",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFF3b7cff),
                strokeWidth = 2.dp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 병원 리스트
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(hospitals, key = { it.hospitalSelectionId }) { hospital ->
                HospitalCard(
                    hospital = hospital,
                    isSelected = selectedHospitalId == hospital.hospitalSelectionId,
                    onClick = { onHospitalClick(hospital.hospitalSelectionId) }
                )
            }
        }
    }
}

// ==========================================
// 검색 완료 화면
// ==========================================
@Composable
private fun SearchCompletedScreen(
    hospitals: List<com.example.ssairen_app.data.model.response.HospitalSelectionInfo>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    selectedHospitalId: Int?,
    onHospitalClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 탭 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabButton(
                text = "검색 완료",
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) }
            )
            TabButton(
                text = "선정된 병원",
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 병원 리스트 (탭에 따라 필터링)
        val filteredHospitals = if (selectedTab == 0) {
            hospitals // 검색 완료: 모든 병원
        } else {
            hospitals.filter { it.status == "ACCEPTED" } // 선정된 병원: 수용 가능한 병원만
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredHospitals, key = { it.hospitalSelectionId }) { hospital ->
                HospitalCard(
                    hospital = hospital,
                    isSelected = selectedHospitalId == hospital.hospitalSelectionId,
                    onClick = { onHospitalClick(hospital.hospitalSelectionId) }
                )
            }
        }
    }
}

// ==========================================
// 탭 버튼
// ==========================================
@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF3b7cff) else Color.Transparent,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        border = if (!isSelected) BorderStroke(1.dp, Color(0xFF4a4a4a)) else null,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// ==========================================
// 병원 카드
// ==========================================
@Composable
private fun HospitalCard(
    hospital: com.example.ssairen_app.data.model.response.HospitalSelectionInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // 상태에 따른 색상 및 텍스트 매핑
    val statusInfo = when (hospital.status) {
        "PENDING" -> HospitalStatus.PENDING
        "ACCEPTED" -> HospitalStatus.ACCEPTED
        "REJECTED" -> HospitalStatus.REJECTED
        "CALLREQUEST" -> HospitalStatus.CALLREQUEST
        else -> HospitalStatus.PENDING
    }

    ClickableDarkCard(
        onClick = onClick,
        isSelected = isSelected,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 병원 정보
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = hospital.hospitalName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "ID: ${hospital.hospitalSelectionId}",
                    color = Color(0xFF999999),
                    fontSize = 12.sp
                )
                Text(
                    text = "생성시간: ${hospital.createdAt}",
                    color = Color(0xFF999999),
                    fontSize = 12.sp
                )
            }

            // 상태 표시
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = statusInfo.color.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, statusInfo.color)
            ) {
                Text(
                    text = statusInfo.displayName,
                    color = statusInfo.color,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
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
    heightDp = 800,
    widthDp = 400
)
@Composable
private fun HospitalSearchPreview() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1a1a1a)
    ) {
        HospitalSearch()
    }
}
