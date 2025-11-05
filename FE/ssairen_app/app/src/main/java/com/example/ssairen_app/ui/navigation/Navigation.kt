package com.example.ssairen_app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ssairen_app.ui.components.ReportButton
import com.example.ssairen_app.ui.components.ActivityButton

// ==========================================
// 보고서 네비게이션 바
// ==========================================
@Composable
fun ReportNavigationBar(
    modifier: Modifier = Modifier,  // ✅ Modifier를 첫 번째로 이동
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ReportButton(
            onClick = { onTabSelected(0) },
            isSelected = selectedTab == 0,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "내 보고서")
        }

        ReportButton(
            onClick = { onTabSelected(1) },
            isSelected = selectedTab == 1,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "출동지령 내역")
        }

        ReportButton(
            onClick = { onTabSelected(2) },
            isSelected = selectedTab == 2,
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "관내 보고서 검색")
        }
    }
}

// ==========================================
// 구급활동일지 네비게이션 바
// ==========================================
@Composable
fun ActivityLogNavigationBar(
    modifier: Modifier = Modifier,  // ✅ Modifier를 첫 번째로 이동
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 첫 번째 줄
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityButton(
                onClick = { onTabSelected(0) },
                isSelected = selectedTab == 0,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "환자정보")
            }
            ActivityButton(
                onClick = { onTabSelected(1) },
                isSelected = selectedTab == 1,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "신고접수")
            }
            ActivityButton(
                onClick = { onTabSelected(2) },
                isSelected = selectedTab == 2,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "출동정보")
            }
            ActivityButton(
                onClick = { onTabSelected(3) },
                isSelected = selectedTab == 3,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "현장활동")
            }
        }

        // 두 번째 줄
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActivityButton(
                onClick = { onTabSelected(4) },
                isSelected = selectedTab == 4,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "이송정보")
            }
            ActivityButton(
                onClick = { onTabSelected(5) },
                isSelected = selectedTab == 5,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "병원정보")
            }
            ActivityButton(
                onClick = { onTabSelected(6) },
                isSelected = selectedTab == 6,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "귀소정보")
            }
            ActivityButton(
                onClick = { onTabSelected(7) },
                isSelected = selectedTab == 7,
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "특이사항")
            }
        }
    }
}