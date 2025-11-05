//ReportSearch.kt
package com.example.ssairen_app.ui.screens.report

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// 검색 결과 데이터 클래스
// ==========================================
data class ReportSearchResult(
    val id: String,
    val patient: String,
    val status: Int,
    val statusText: String,
    val date: String,
    val time: String,
    val location: String,
    val reporterName: String,
    val teamName: String
)

// ==========================================
// 더미 데이터
// ==========================================
private val SEARCH_RESULTS = listOf(
    ReportSearchResult(
        id = "CB00000000846",
        patient = "구급환자 | 정보",
        status = 100,
        statusText = "작성완료",
        date = "2024-04-05",
        time = "14:30",
        location = "강남 도로변 (구급대 11 일반)",
        reporterName = "김구급",
        teamName = "구급대 11"
    ),
    ReportSearchResult(
        id = "CB00000000847",
        patient = "구급환자 | 정보",
        status = 100,
        statusText = "작성완료",
        date = "2024-04-04",
        time = "10:15",
        location = "서초구 서초동 123 (구급대 12 일반)",
        reporterName = "이응급",
        teamName = "구급대 12"
    ),
    ReportSearchResult(
        id = "CB00000000848",
        patient = "구급환자 | 정보",
        status = 100,
        statusText = "작성완료",
        date = "2024-04-03",
        time = "16:45",
        location = "송파구 잠실동 456 (구급대 13 일반)",
        reporterName = "박구조",
        teamName = "구급대 13"
    )
)

// ==========================================
// 메인 화면
// ==========================================
@Composable
fun ReportSearchScreen(
    onNavigateToDetail: (ReportSearchResult) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(SEARCH_RESULTS) }
    var isSearching by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val handleSearch: () -> Unit = remember {
        {
            scope.launch {
                isSearching = true

                searchResults = if (searchQuery.trim().isEmpty()) {
                    SEARCH_RESULTS
                } else {
                    SEARCH_RESULTS.filter { item ->
                        item.id.lowercase().contains(searchQuery.lowercase()) ||
                                item.reporterName.contains(searchQuery) ||
                                item.teamName.contains(searchQuery) ||
                                item.location.contains(searchQuery)
                    }
                }

                delay(300)
                isSearching = false
            }
        }
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
            isSearching -> {
                LoadingView()
            }
            searchResults.isNotEmpty() -> {
                SearchResults(
                    results = searchResults,
                    onCardClick = onNavigateToDetail
                )
            }
            else -> {
                EmptyView(
                    message = if (searchQuery.isNotEmpty()) "검색 결과가 없습니다" else "검색어를 입력해주세요"
                )
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
                    Text(
                        text = item.patient,
                        fontSize = 13.sp,
                        color = Color(0xFF999999)
                    )
                }

                // 오른쪽: 상태 뱃지
                Box(
                    modifier = Modifier
                        .background(
                            color = if (item.status == 100) Color(0xFF28a745) else Color(0xFF4a4a4a),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.statusText,
                        fontSize = 12.sp,
                        color = Color.White
                    )
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
                    Text(
                        text = "작성자: ${item.reporterName} (${item.teamName})",
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