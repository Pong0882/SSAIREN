// EmergencyNav.kt
package com.example.ssairen_app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmergencyNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF3a3a3a))
            .requiredHeight(80.dp),  // ⭐ height → requiredHeight로 변경 (강제 고정)
//            .imePadding()  // ⭐ 키보드 대응
//            .navigationBarsPadding(),  // ⭐ 시스템 네비게이션 바 패딩
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(
            icon = Icons.Default.Home,
            label = "홈",
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            width = 60.dp
        )
        NavItem(
            icon = Icons.Default.DateRange,
            label = "구급활동일지",
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            width = 95.dp
        )
        NavItem(
            icon = Icons.AutoMirrored.Filled.List,
            label = "요약",
            isSelected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            width = 60.dp
        )
        NavItem(
            icon = Icons.Default.Edit,
            label = "메모",
            isSelected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            width = 60.dp
        )
        NavItem(
            icon = Icons.Default.LocalHospital,
            label = "병원이송",
            isSelected = selectedTab == 4,
            onClick = { onTabSelected(4) },
            width = 75.dp
        )
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    width: androidx.compose.ui.unit.Dp = 80.dp
) {
    Column(
        modifier = Modifier
            .width(width)
            .height(80.dp)
            .background(
                if (isSelected) Color(0xFF3b7cff) else Color.Transparent
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Color.White else Color(0xFF999999),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color(0xFF999999),
                    fontSize = 9.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}