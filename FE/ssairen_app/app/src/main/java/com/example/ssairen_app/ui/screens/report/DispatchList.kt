package com.example.ssairen_app.ui.screens.report

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ssairen_app.ui.components.ClickableDarkCard
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.api.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
// 출동지령 내역 데이터 클래스
// ==========================================
data class DispatchData(
    val dispatchNumber: String,     // 재난번호
    val disasterType: String,        // 재난유형 (구급, 화재 등)
    val date: String,                // 날짜 (yyyy-MM-dd)
    val time: String,                // 시간 (HH:mm)
    val location: String             // 위치 (locationAddress)
)

// ==========================================
// 출동지령 내역 콘텐츠 (네비게이션 바 없이 내용만)
// ==========================================
@Composable
fun DispatchList() {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context.applicationContext as Application) }
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var dispatches by remember { mutableStateOf<List<DispatchData>>(emptyList()) }
    var allApiDispatches by remember { mutableStateOf<List<com.example.ssairen_app.data.model.response.Dispatch>>(emptyList()) }
    var selectedCardIndex by remember { mutableStateOf<Int?>(null) }
    var showDetail by remember { mutableStateOf(false) }
    var selectedDispatchDetail by remember { mutableStateOf<DispatchDetailData?>(null) }

    LaunchedEffect(Unit) {
        Log.d("DispatchList", "API 호출 시작")
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
                RetrofitInstance.apiService.getDispatchList("Bearer $token", 100)
            }

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d("DispatchList", "API 응답 성공: ${body.success}")

                if (body.success && body.data != null) {
                    allApiDispatches = body.data.dispatches
                    Log.d("DispatchList", "출동지령 ${allApiDispatches.size}개 조회됨")

                    dispatches = allApiDispatches.map { dispatch ->
                        val parsedDate = parseDate(dispatch.date)
                        DispatchData(
                            dispatchNumber = dispatch.disasterNumber,
                            disasterType = dispatch.disasterType,
                            date = parsedDate.first,
                            time = parsedDate.second,
                            location = dispatch.locationAddress
                        )
                    }
                    Log.d("DispatchList", "변환된 DispatchData ${dispatches.size}개")
                } else {
                    errorMessage = "출동지령 데이터를 불러올 수 없습니다"
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("DispatchList", "API 에러: ${response.code()}, $errorBody")
                errorMessage = "서버 오류: ${response.code()}"
            }
        } catch (e: Exception) {
            Log.e("DispatchList", "API 호출 실패", e)
            errorMessage = "네트워크 오류: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    if (showDetail && selectedDispatchDetail != null) {
        DispatchDetail(
            dispatchData = selectedDispatchDetail!!,
            onDismiss = { showDetail = false }
        )
    } else {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
            errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFFF6B6B),
                        fontSize = 14.sp
                    )
                }
            }
            dispatches.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "출동지령 내역이 없습니다",
                        color = Color(0xFF999999),
                        fontSize = 14.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(dispatches, key = { _, dispatch -> dispatch.dispatchNumber }) { index, dispatch ->
                        DispatchCard(
                            dispatchData = dispatch,
                            isSelected = selectedCardIndex == index,
                            onClick = {
                                selectedCardIndex = if (selectedCardIndex == index) null else index

                                val apiDispatch = allApiDispatches.find {
                                    it.disasterNumber == dispatch.dispatchNumber
                                }

                                if (apiDispatch != null) {
                                    val parsedDateTime = parseDateTime(apiDispatch.date)
                                    selectedDispatchDetail = DispatchDetailData(
                                        dispatchNumber = apiDispatch.disasterNumber,
                                        dispatchLevel = apiDispatch.dispatchLevel,
                                        dispatchOrder = "${apiDispatch.dispatchOrder}차",
                                        disasterType = apiDispatch.disasterType,
                                        disasterSubtype = apiDispatch.disasterSubtype,
                                        dispatchStation = apiDispatch.dispatchStation,
                                        reporter = apiDispatch.reporterName,
                                        reporterPhone = apiDispatch.reporterPhone,
                                        dispatchTime = parsedDateTime,
                                        address = apiDispatch.locationAddress,
                                        cause = apiDispatch.incidentDescription
                                    )
                                    showDetail = true
                                } else {
                                    Log.e("DispatchList", "해당 출동지령을 찾을 수 없습니다: ${dispatch.dispatchNumber}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// 날짜 파싱 함수 (yyyy-MM-dd, HH:mm 분리)
private fun parseDate(dateString: String): Pair<String, String> {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        Pair(dateFormat.format(date!!), timeFormat.format(date))
    } catch (e: Exception) {
        Log.e("DispatchList", "날짜 파싱 실패: $dateString", e)
        Pair(dateString, "")
    }
}

// 날짜시간 파싱 함수 (yyyy-MM-dd HH:mm 형태)
private fun parseDateTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)

        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        outputFormat.format(date!!)
    } catch (e: Exception) {
        Log.e("DispatchList", "날짜시간 파싱 실패: $dateString", e)
        dateString
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
            // 상단: 재난번호 & 재난유형 & 날짜/시간
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 왼쪽: 재난번호 & 재난유형
                Column {
                    Text(
                        text = "${dispatchData.dispatchNumber} | ${dispatchData.disasterType}",
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

            // 하단: 위치 정보
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "위치: ${dispatchData.location}",
                    color = Color(0xFF999999),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}