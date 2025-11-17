package com.example.ssairen_app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ==========================================
// 1. 보고서 네비게이션 버튼
// ==========================================
@Composable
fun ReportButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF3b7cff) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color(0xFF999999)
        ),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF3b7cff) else Color(0xFF4a4a4a)),
        shape = RoundedCornerShape(6.dp), // ✅ 20dp → 6dp로 수정
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        content = content
    )
}

// ==========================================
// 2. 구급활동일지 네비게이션 버튼
// ==========================================
@Composable
fun ActivityButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF3b7cff) else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color(0xFF999999)
        ),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF3b7cff) else Color(0xFF4a4a4a)),
        shape = RoundedCornerShape(6.dp), // ✅ 8dp → 6dp로 수정
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        content = content
    )
}

// ==========================================
// 3. 메인 버튼
// ==========================================
@Composable
fun MainButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    backgroundColor: Color = Color(0xFF3b7cff),
    contentColor: Color = Color.White,
    cornerRadius: Dp = 8.dp,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor,
            disabledContainerColor = Color(0xFF4a4a4a),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(cornerRadius),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        enabled = enabled,
        content = content
    )
}