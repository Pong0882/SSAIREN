package com.example.ssairen_app.viewmodel

import com.example.ssairen_app.data.model.response.LoginData

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()

    // 3. Success 상태가 LoginData 객체를 갖도록 수정
    data class Success(val loginData: LoginData) : LoginState()

    data class Error(val message: String) : LoginState()
}