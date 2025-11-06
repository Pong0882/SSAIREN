// EmergencyNav.kt
package com.example.ssairen_app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List  // ✅ 수정
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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
            .navigationBarsPadding()  // ✅ 시스템 네비게이션 바 회피
            .height(60.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(
            icon = Icons.Default.Home,
            label = "홈",
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        NavItem(
            icon = Icons.Default.DateRange,
            label = "구급활동일지",
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
        NavItem(
            icon = Icons.AutoMirrored.Filled.List,  // ✅ 수정됨
            label = "요약",
            isSelected = selectedTab == 2,
            onClick = { onTabSelected(2) }
        )
        NavItem(
            icon = Icons.Default.Edit,
            label = "메모",
            isSelected = selectedTab == 3,
            onClick = { onTabSelected(3) }
        )
        NavItem(
            icon = Icons.Default.LocalHospital,
            label = "병원이송",
            isSelected = selectedTab == 4,
            onClick = { onTabSelected(4) }
        )
    }
}

@Composable
private fun NavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(80.dp)
            .fillMaxHeight()
            .background(
                if (isSelected) Color(0xFF3b7cff) else Color.Transparent
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (isSelected) Color.White else Color(0xFF999999),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    color = if (isSelected) Color.White else Color(0xFF999999),
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}