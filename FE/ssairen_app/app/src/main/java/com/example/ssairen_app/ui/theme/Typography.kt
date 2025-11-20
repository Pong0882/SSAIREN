package com.example.ssairen_app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ssairen_app.R

// 삼성원 폰트 패밀리 정의
val SamsungOneFont = FontFamily(
    Font(R.font.samsungone_light, FontWeight.Light),
    Font(R.font.samsungone_regular, FontWeight.Normal),
    Font(R.font.samsungone_medium, FontWeight.Medium),
    Font(R.font.samsungone_bold, FontWeight.Bold),
    Font(R.font.samsungone_extrabold, FontWeight.ExtraBold)
)

// Typography 설정
val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = SamsungOneFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SamsungOneFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = SamsungOneFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SamsungOneFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SamsungOneFont,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
    ),
    titleSmall = TextStyle(
        fontFamily = SamsungOneFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle(
        fontFamily = SamsungOneFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SamsungOneFont,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SamsungOneFont,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp
    )
)