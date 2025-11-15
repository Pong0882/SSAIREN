//ReportSearch.kt
package com.example.ssairen_app.ui.screens.report

import android.app.Application
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// 검색 결과 데이터 클래스
// ==========================================
data class ReportSearchResult(
    val id: String,                     // disasterNumber
    // TODO: 추후 API에서 제공 예정
    val patient: String? = null,        // 응답 데이터에 없음
    val status: Int = 100,              // 응답 데이터에 없음, 임시로 100 (작성완료)
    // TODO: 추후 API에서 제공 예정
    val statusText: String? = null,     // 응답 데이터에 없음
    val date: String,                   // dispatchInfo.date 파싱 (yyyy-MM-dd)
    val time: String,                   // dispatchInfo.date 파싱 (HH:mm)
    val location: String,               // dispatchInfo.locationAddress
    // TODO: 추후 API에서 제공 예정
    val reporterName: String? = null,   // 응답 데이터에 없음
    val teamName: String,               // fireStateInfo.name
    val emergencyReportId: Int          // emergencyReports[].id (상세 화면 이동용)
)


// ==========================================
// 메인 화면
// ==========================================
@Composable
fun ReportSearchScreen(
    onNavigateToDetail: (ReportSearchResult) -> Unit = {}
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context.applicationContext as Application) }

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<ReportSearchResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 화면 진입 시 API 호출
    LaunchedEffect(Unit) {
        Log.d("ReportSearchScreen", "🔍 API 호출 시작")
        isLoading = true
        errorMessage = null

        try {
            val token = authManager.getAccessToken()
            if (token == null) {
                errorMessage = "로그인이 필요합니다"
                isLoading = false
                return@LaunchedEffect
            }

            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.apiService.getFireStateReports("Bearer $token")
            }

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("ReportSearchScreen", "✅ API 응답 성공: ${body.success}")

                if (body.success && body.data != null) {
                    val allReports = mutableListOf<ReportSearchResult>()

                    body.data.forEach { fireStateData ->
                        val teamName = fireStateData.fireStateInfo.name

                        fireStateData.emergencyReports.forEach { report ->
                            val parsedDateTime = parseDateTimeForSearch(report.dispatchInfo.date)
                            allReports.add(
                                ReportSearchResult(
                                    id = report.dispatchInfo.disasterNumber,
                                    patient = null,
                                    status = 100,
                                    statusText = null,
                                    date = parsedDateTime.first,
                                    time = parsedDateTime.second,
                                    location = report.dispatchInfo.locationAddress,
                                    reporterName = null,
                                    teamName = teamName,
                                    emergencyReportId = report.id
                                )
                            )
                        }
                    }

                    searchResults = allReports
                    Log.d("ReportSearchScreen", "📊 변환된 보고서 ${allReports.size}개")
                } else {
                    errorMessage = "보고서 데이터를 불러올 수 없습니다"
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("ReportSearchScreen", "❌ API 에러: ${response.code()}, $errorBody")
                errorMessage = "서버 오류: ${response.code()}"
            }
        } catch (e: Exception) {
            Log.e("ReportSearchScreen", "❌ API 호출 실패", e)
            errorMessage = "네트워크 오류: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // 검색 기능은 추후 구현 예정
    val handleSearch: () -> Unit = {
        // TODO: 검색 기능 구현
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
    ) {
        // 검색 영역
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onSearch = handleSearch
        )

        // 필터 버튼들
        FilterButtons()

        // 검색 결과
        when {
            isLoading -> {
                LoadingView()
            }
            errorMessage != null -> {
                ErrorView(message = errorMessage!!)
            }
            searchResults.isNotEmpty() -> {
                SearchResults(
                    results = searchResults,
                    onCardClick = onNavigateToDetail
                )
            }
            else -> {
                EmptyView(message = "관내 보고서가 없습니다")
            }
        }
    }
}

// ==========================================
// 검색바
// ==========================================
@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            placeholder = {
                Text(
                    text = "신고번호, 작성자, 구급대, 위치로 검색",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF2a2a2a),
                unfocusedContainerColor = Color(0xFF2a2a2a),
                focusedBorderColor = Color(0xFF3a3a3a),
                unfocusedBorderColor = Color(0xFF3a3a3a)
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )

        Button(
            onClick = onSearch,
            modifier = Modifier
                .height(44.dp)
                .widthIn(min = 60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF3b7cff)
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp)
        ) {
            Text(
                text = "검색",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

// ==========================================
// 필터 버튼들
// ==========================================
@Composable
private fun FilterButtons() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterButton(text = "오늘")
        FilterButton(text = "최근 7일")
        FilterButton(text = "최근 30일")
        FilterButton(text = "기간 설정")
    }
}

@Composable
private fun FilterButton(text: String) {
    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color(0xFF3a3a3a),
                shape = RoundedCornerShape(6.dp)
            )
            .background(
                color = Color(0xFF2a2a2a),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { /* 필터 로직 */ }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF999999)
        )
    }
}

// ==========================================
// 검색 결과 리스트
// ==========================================
@Composable
private fun SearchResults(
    results: List<ReportSearchResult>,
    onCardClick: (ReportSearchResult) -> Unit
) {
    Column {
        // 결과 헤더
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "총 ${results.size}건의 보고서",
                fontSize = 13.sp,
                color = Color(0xFF999999)
            )
        }

        // 결과 리스트
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(results) { item ->
                ReportCard(
                    item = item,
                    onClick = { onCardClick(item) }
                )
            }
        }
    }
}

// ==========================================
// 보고서 카드
// ==========================================
@Composable
private fun ReportCard(
    item: ReportSearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2a2a2a)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3a3a3a))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 카드 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 왼쪽: ID와 환자 정보
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.id,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    item.patient?.let { patient ->
                        Text(
                            text = patient,
                            fontSize = 13.sp,
                            color = Color(0xFF999999)
                        )
                    }
                }

                // 오른쪽: 상태 뱃지
                item.statusText?.let { statusText ->
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (item.status == 100) Color(0xFF28a745) else Color(0xFF4a4a4a),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // 진행률 바 (100% 미만일 때만 표시)
            if (item.status < 100) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            color = Color(0xFF3a3a3a),
                            shape = RoundedCornerShape(3.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(item.status / 100f)
                            .height(6.dp)
                            .background(
                                color = Color(0xFF3b7cff),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 카드 푸터
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 왼쪽: 신고번호와 작성자 정보
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "신고번호 ${item.id}",
                        fontSize = 12.sp,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    val writerText = if (item.reporterName != null) {
                        "작성자: ${item.reporterName} (${item.teamName})"
                    } else {
                        "소방서: ${item.teamName}"
                    }
                    Text(
                        text = writerText,
                        fontSize = 11.sp,
                        color = Color(0xFF999999)
                    )
                }

                // 오른쪽: 날짜와 위치
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${item.date} ${item.time}",
                        fontSize = 11.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = item.location,
                        fontSize = 11.sp,
                        color = Color(0xFF999999)
                    )
                }
            }
        }
    }
}

// ==========================================
// 로딩 뷰
// ==========================================
@Composable
private fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = Color(0xFF3b7cff)
            )
            Text(
                text = "검색 중...",
                fontSize = 16.sp,
                color = Color(0xFF999999)
            )
        }
    }
}

// ==========================================
// 빈 화면
// ==========================================
@Composable
private fun EmptyView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 16.sp,
            color = Color(0xFF999999)
        )
    }
}

// ==========================================
// 에러 화면
// ==========================================
@Composable
private fun ErrorView(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            fontSize = 16.sp,
            color = Color(0xFFFF6B6B)
        )
    }
}

// ==========================================
// 날짜 파싱 함수 (yyyy-MM-dd, HH:mm 분리)
// ==========================================
private fun parseDateTimeForSearch(dateString: String): Pair<String, String> {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault())
        val date = inputFormat.parse(dateString)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        Pair(dateFormat.format(date!!), timeFormat.format(date))
    } catch (e: Exception) {
        Log.e("ReportSearchScreen", "날짜 파싱 실패: $dateString", e)
        Pair(dateString, "")
    }
}