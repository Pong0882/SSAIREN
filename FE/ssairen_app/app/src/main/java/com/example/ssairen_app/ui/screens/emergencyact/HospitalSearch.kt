package com.example.ssairen_app.ui.screens.emergencyact

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ssairen_app.ui.components.ClickableDarkCard
import kotlinx.coroutines.delay

// ==========================================
// 병원 데이터 클래스
// ==========================================
data class HospitalData(
    val id: Int,
    val name: String,
    val distance: String,
    val phone: String,
    val status: HospitalStatus
)

enum class HospitalStatus(val displayName: String, val color: Color) {
    REQUESTING("요청중", Color(0xFF999999)),
    ACCEPTED("수용 가능", Color(0xFF34c759)),
    REJECTED("거절", Color(0xFFff3b30)),
    CALL_REQUIRED("전화 요망", Color(0xFFffcc00))
}

// ==========================================
// 병원 검색 메인 화면
// ==========================================
@Composable
fun HospitalSearch(
    modifier: Modifier = Modifier
) {
    var isSearching by remember { mutableStateOf(true) }
    var hospitals by remember { mutableStateOf<List<HospitalData>>(emptyList()) }
    var selectedHospitalId by remember { mutableStateOf<Int?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: 검색 완료, 1: 선정된 병원

    // 병원 추가 로직 (랜덤 간격으로 추가)
    LaunchedEffect(Unit) {
        val dummyHospitals = listOf(
            HospitalData(1, "OO 병원", "1.4 km", "010-5555-5555", HospitalStatus.REQUESTING),
            HospitalData(2, "OO 병원", "3 km", "010-5555-5555", HospitalStatus.REQUESTING),
            HospitalData(3, "OO 병원", "2.6 km", "010-5555-5555", HospitalStatus.REQUESTING),
            HospitalData(4, "OO 병원", "5 km", "010-5555-5555", HospitalStatus.REQUESTING),
            HospitalData(5, "OO 병원", "7 km", "010-5555-5555", HospitalStatus.REQUESTING),
        )

        // 0.5초~1.5초 랜덤 간격으로 병원 추가
        dummyHospitals.forEach { hospital ->
            delay((500..1500).random().toLong())
            hospitals = hospitals + hospital
        }

        // 모든 병원 추가 완료 후 2초 대기
        delay(2000)
        isSearching = false

        // 검색 완료 후 상태 업데이트 (랜덤으로 수용/거절/전화요망)
        hospitals = hospitals.map { hospital ->
            val statusOptions = listOf(
                HospitalStatus.ACCEPTED,
                HospitalStatus.REJECTED,
                HospitalStatus.CALL_REQUIRED
            )
            hospital.copy(status = statusOptions.random())
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
    ) {
        if (isSearching) {
            // 검색 중 화면
            SearchingScreen(hospitals = hospitals, selectedHospitalId = selectedHospitalId, onHospitalClick = { selectedHospitalId = if (selectedHospitalId == it) null else it })
        } else {
            // 검색 완료 화면
            SearchCompletedScreen(
                hospitals = hospitals,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                selectedHospitalId = selectedHospitalId,
                onHospitalClick = { selectedHospitalId = if (selectedHospitalId == it) null else it }
            )
        }
    }
}

// ==========================================
// 검색 중 화면
// ==========================================
@Composable
private fun SearchingScreen(
    hospitals: List<HospitalData>,
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
            items(hospitals, key = { it.id }) { hospital ->
                HospitalCard(
                    hospital = hospital,
                    isSelected = selectedHospitalId == hospital.id,
                    onClick = { onHospitalClick(hospital.id) }
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
    hospitals: List<HospitalData>,
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
            hospitals.filter { it.status == HospitalStatus.ACCEPTED } // 선정된 병원: 수용 가능한 병원만
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredHospitals, key = { it.id }) { hospital ->
                HospitalCard(
                    hospital = hospital,
                    isSelected = selectedHospitalId == hospital.id,
                    onClick = { onHospitalClick(hospital.id) }
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
    hospital: HospitalData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
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
                    text = hospital.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "거리 ${hospital.distance}",
                    color = Color(0xFF999999),
                    fontSize = 14.sp
                )
                Text(
                    text = "전화번호 ${hospital.phone}",
                    color = Color(0xFF999999),
                    fontSize = 14.sp
                )
            }

            // 상태 표시
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = hospital.status.color.copy(alpha = 0.2f),
                border = BorderStroke(1.dp, hospital.status.color)
            ) {
                Text(
                    text = hospital.status.displayName,
                    color = hospital.status.color,
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
