package com.example.ssairen_app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun SignatureDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<Offset>) -> Unit
) {
    var paths by remember { mutableStateOf(listOf<List<Offset>>()) }
    var currentPath by remember { mutableStateOf(listOf<Offset>()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2a2a2a))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "서명하기",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(8.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPath = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        currentPath = currentPath + change.position
                                    },
                                    onDragEnd = {
                                        paths = paths + listOf(currentPath)
                                        currentPath = emptyList()
                                    }
                                )
                            }
                    ) {
                        paths.forEach { path ->
                            if (path.size > 1) {
                                val pathData = Path()
                                pathData.moveTo(path.first().x, path.first().y)
                                path.drop(1).forEach { offset ->
                                    pathData.lineTo(offset.x, offset.y)
                                }
                                drawPath(
                                    path = pathData,
                                    color = Color.Black,
                                    style = Stroke(width = 5f)
                                )
                            }
                        }

                        if (currentPath.size > 1) {
                            val pathData = Path()
                            pathData.moveTo(currentPath.first().x, currentPath.first().y)
                            currentPath.drop(1).forEach { offset ->
                                pathData.lineTo(offset.x, offset.y)
                            }
                            drawPath(
                                path = pathData,
                                color = Color.Black,
                                style = Stroke(width = 5f)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            paths = emptyList()
                            currentPath = emptyList()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFF4a4a4a))
                    ) {
                        Text("초기화")
                    }

                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4a4a4a),
                            contentColor = Color.White
                        )
                    ) {
                        Text("취소")
                    }

                    Button(
                        onClick = { onConfirm(paths.flatten()) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3b7cff),
                            contentColor = Color.White
                        )
                    ) {
                        Text("확인")
                    }
                }
            }
        }
    }
}

@Composable
fun SignatureArea(
    signature: List<Offset>,
    onSignatureClick: () -> Unit,
    enabled: Boolean = true,
    widthFraction: Float = 0.33f
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(widthFraction)
                .height(60.dp)
                .background(Color.Transparent)
                .clickable(enabled = enabled) { onSignatureClick() }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.BottomCenter)
            ) {
                drawLine(
                    color = Color(0xFF3a3a3a),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2f
                )
            }

            if (signature.isEmpty()) {
                Text(
                    text = "서명 또는 인",
                    color = Color(0xFF999999),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(bottom = 8.dp)
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 8.dp)
                ) {
                    if (signature.size > 1) {
                        val pathData = Path()
                        val minX = signature.minOfOrNull { it.x } ?: 0f
                        val maxX = signature.maxOfOrNull { it.x } ?: size.width
                        val minY = signature.minOfOrNull { it.y } ?: 0f
                        val maxY = signature.maxOfOrNull { it.y } ?: size.height

                        val scaleX = if (maxX - minX > 0) size.width / (maxX - minX) else 1f
                        val scaleY = if (maxY - minY > 0) size.height / (maxY - minY) else 1f
                        val scale = minOf(scaleX, scaleY) * 0.8f

                        val offsetX = (size.width - (maxX - minX) * scale) / 2f
                        val offsetY = (size.height - (maxY - minY) * scale) / 2f

                        val scaledPoints = signature.map { offset ->
                            Offset(
                                x = (offset.x - minX) * scale + offsetX,
                                y = (offset.y - minY) * scale + offsetY
                            )
                        }

                        pathData.moveTo(scaledPoints.first().x, scaledPoints.first().y)
                        scaledPoints.drop(1).forEach { offset ->
                            pathData.lineTo(offset.x, offset.y)
                        }

                        drawPath(
                            path = pathData,
                            color = Color.White,
                            style = Stroke(width = 3f)
                        )
                    }
                }
            }
        }
    }
}
