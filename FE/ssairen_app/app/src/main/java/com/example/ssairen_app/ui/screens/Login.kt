// Login.kt
package com.example.ssairen_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Login(
    onLoginSuccess: () -> Unit = {},
    onForgotPassword: () -> Unit = {}
) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // TODO: ViewModel 연결 (나중에)
    /*
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(context) as T
            }
        }
    )

    val loginState by viewModel.loginState.collectAsState()

    // 자동 로그인 체크
    LaunchedEffect(Unit) {
        if (viewModel.checkAutoLogin()) {
            onLoginSuccess()
        }
    }

    // 로그인 성공 시
    LaunchedEffect(loginState.isSuccess) {
        if (loginState.isSuccess) {
            onLoginSuccess()
        }
    }

    // 에러 메시지 표시
    LaunchedEffect(loginState.errorMessage) {
        loginState.errorMessage?.let { message ->
            println("❌ 로그인 에러: $message")
        }
    }
    */

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 로그인 타이틀
            Text(
                text = "로그인",
                color = Color(0xFF3b7cff),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 60.dp)
            )

            // ID 입력 필드
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "ID",
                        color = Color(0xFF666666),
                        fontSize = 14.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF2a2a2a),
                    unfocusedContainerColor = Color(0xFF2a2a2a),
                    focusedBorderColor = Color(0xFF3b7cff),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF3b7cff)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PW 입력 필드
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "PW",
                        color = Color(0xFF666666),
                        fontSize = 14.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF2a2a2a),
                    unfocusedContainerColor = Color(0xFF2a2a2a),
                    focusedBorderColor = Color(0xFF3b7cff),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF3b7cff)
                ),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 로그인하기 버튼
            Button(
                onClick = {
                    isLoading = true

                    // TODO: ViewModel 로그인 호출 (나중에)
                    // viewModel.login(userId, password)

                    // 임시: 바로 로그인 성공 처리
                    isLoading = false
                    onLoginSuccess()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3b7cff),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading && userId.isNotEmpty() && password.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "로그인하기",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 찾기
            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "비밀번호 찾기",
                    color = Color(0xFF999999),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}