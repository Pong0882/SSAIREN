//Card.kt
package com.example.ssairen_app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ==========================================
// 다크 모드 카드 컴포넌트
// ==========================================
@Composable
fun DarkCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFF3a3a3a), // 기본 회색 테두리
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2a2a2a)
        ),
        border = BorderStroke(borderWidth, borderColor),
        content = {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    )
}

// ==========================================
// ✅ 클릭 가능한 다크 모드 카드 (클릭 시 파란색 테두리)
// ==========================================
@Composable
fun ClickableDarkCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    cornerRadius: Dp = 10.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2a2a2a)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) Color(0xFF3b7cff) else Color(0xFF3a3a3a)
        ),
        content = {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    )
}