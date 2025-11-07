// Login.kt
package com.example.ssairen_app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ssairen_app.viewmodel.AuthViewModel
import com.example.ssairen_app.viewmodel.LoginState

@Composable
fun Login(
    onLoginSuccess: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()  // ⭐ ViewModel 추가
) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // ⭐ ViewModel 상태 관찰
    val loginState by viewModel.loginState.observeAsState(LoginState.Idle)
    val isLoggedIn by viewModel.isLoggedIn.observeAsState(false)

    // ⭐ 자동 로그인 체크
    LaunchedEffect(Unit) {
        if (isLoggedIn) {
            onLoginSuccess()
        }
    }

    // ⭐ 로그인 성공 시 화면 이동
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess()
        }
    }

    // ⭐ 로딩 상태
    val isLoading = loginState is LoginState.Loading

    // ⭐ 에러 메시지 표시
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Error) {
            errorMessage = (loginState as LoginState.Error).message
            showError = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .widthIn(max = 400.dp),
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

            // ID 입력 필드 (학번)
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                placeholder = {
                    Text(
                        text = "학번",
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

            Spacer(modifier = Modifier.height(8.dp))

            // ⭐ 에러 메시지 표시
            if (showError) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 로그인하기 버튼 (항상 보이도록 수정)
            Button(
                onClick = {
                    if (userId.isNotEmpty() && password.isNotEmpty()) {
                        showError = false
                        viewModel.login(userId, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (userId.isNotEmpty() && password.isNotEmpty()) {
                        Color(0xFF2F67FF)
                    } else {
                        Color(0xFF2F67FF).copy(alpha = 0.5f)
                    },
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFF2F67FF).copy(alpha = 0.5f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !isLoading
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

            // 비밀번호 찾기 (오른쪽 정렬)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(
                    onClick = onForgotPassword
                ) {
                    Text(
                        text = "비밀번호 찾기",
                        color = Color(0xFF999999),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}