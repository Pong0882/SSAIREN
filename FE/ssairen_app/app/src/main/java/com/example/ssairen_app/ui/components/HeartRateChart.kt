// HeartRateChart.kt
package com.example.ssairen_app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Paint

@Composable
fun HeartRateChart(
    heartRateHistory: List<Int>,
    modifier: Modifier = Modifier
) {
    // 최근 10개 데이터만 표시
    val displayData = if (heartRateHistory.size > 10) {
        heartRateHistory.takeLast(10)
    } else {
        heartRateHistory
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 상단 정보
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "심박수 추이",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (displayData.isNotEmpty()) {
                Text(
                    text = "${displayData.last()} BPM",
                    color = Color(0xFF00d9ff),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 그래프
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp)
        ) {
            val width = size.width
            val height = size.height

            // Y축 범위 설정 (30~130 BPM) - 110 기준선을 위로 올려서 간격 확보
            val minValue = 30f
            val maxValue = 130f
            val valueRange = maxValue - minValue

            // 그래프 영역 설정 (왼쪽에 숫자 공간 확보)
            val labelWidth = 40.dp.toPx()  // 숫자 공간
            val graphStartX = labelWidth
            val graphWidth = width - labelWidth

            // 데이터가 2개 이상일 때만 그래프 그리기
            if (displayData.size >= 2) {
                val path = Path()
                val pointSpacing = graphWidth / (displayData.size - 1).coerceAtLeast(1)

                // 첫 번째 점
                val firstY = height - ((displayData[0] - minValue) / valueRange * height)
                path.moveTo(graphStartX, firstY.coerceIn(0f, height))

                // 나머지 점들 연결
                displayData.forEachIndexed { index, hr ->
                    val x = graphStartX + (index * pointSpacing)
                    val y = height - ((hr - minValue) / valueRange * height)
                    path.lineTo(x, y.coerceIn(0f, height))
                }

                // 심박수 파형 느낌으로 선 그리기 (더 굵고 밝게)
                drawPath(
                    path = path,
                    color = Color(0xFF00ff88),  // 밝은 청록색
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // 글로우 효과 (그림자처럼)
                drawPath(
                    path = path,
                    color = Color(0xFF00ff88).copy(alpha = 0.3f),
                    style = Stroke(
                        width = 5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 기준선 그리기 (50, 110) - 항상 표시
            val lowThresholdY = height - ((50f - minValue) / valueRange * height)
            val highThresholdY = height - ((110f - minValue) / valueRange * height)

            // 텍스트 페인트 설정
            val textPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#FF5252")
                textSize = 28f
                isAntiAlias = true
                alpha = 128  // 50% 투명도
                textAlign = Paint.Align.LEFT
            }

            // 50 BPM - 왼쪽 끝에 텍스트, 선은 차트 끝까지
            drawContext.canvas.nativeCanvas.drawText(
                "50",
                2f,
                lowThresholdY + 10f,
                textPaint
            )
            drawLine(
                color = Color(0xFFFF5252).copy(alpha = 0.3f),
                start = Offset(22.dp.toPx(), lowThresholdY),
                end = Offset(width, lowThresholdY),
                strokeWidth = 2.dp.toPx()
            )

            // 110 BPM - 왼쪽 끝에 텍스트, 선은 차트 끝까지
            drawContext.canvas.nativeCanvas.drawText(
                "110",
                2f,
                highThresholdY + 10f,
                textPaint
            )
            drawLine(
                color = Color(0xFFFF5252).copy(alpha = 0.3f),
                start = Offset(26.dp.toPx(), highThresholdY),
                end = Offset(width, highThresholdY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}
