package com.example.ssairen_app.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ssairen_app.data.websocket.HospitalResponseMessage

/**
 * 병원 수용 응답 알림 모달
 *
 * @param response 병원 응답 메시지
 * @param onConfirm 확인 버튼 클릭 시 콜백
 */
@Composable
fun HospitalResponseModal(
    response: HospitalResponseMessage,
    onConfirm: () -> Unit
) {
    LaunchedEffect(Unit) {
        Log.d("HospitalResponseModal", "╔════════════════════════════════════════╗")
        Log.d("HospitalResponseModal", "║   MODAL COMPOSABLE RENDERED           ║")
        Log.d("HospitalResponseModal", "╚════════════════════════════════════════╝")
        Log.d("HospitalResponseModal", "Response Details:")
        Log.d("HospitalResponseModal", "  - Hospital: ${response.hospitalName}")
        Log.d("HospitalResponseModal", "  - Status: ${response.status}")
        Log.d("HospitalResponseModal", "  - Type: ${response.type}")
        Log.d("HospitalResponseModal", "========================================")
    }

    Dialog(onDismissRequest = onConfirm) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2A2A2A), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 아이콘
                Text(
                    text = if (response.status == "ACCEPTED") "✅" else "❌",
                    fontSize = 48.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 제목
                Text(
                    text = if (response.status == "ACCEPTED") "병원 수용 가능" else "병원 수용 거부",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 병원 이름
                Text(
                    text = response.hospitalName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 상태 메시지
                Text(
                    text = if (response.status == "ACCEPTED")
                        "해당 병원으로 이송 가능합니다"
                    else
                        "다른 병원을 선택해주세요",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 확인 버튼
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3b7cff)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "확인",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
