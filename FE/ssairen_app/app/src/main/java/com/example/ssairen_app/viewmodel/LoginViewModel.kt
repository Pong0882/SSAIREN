// ui/viewmodel/LoginViewModel.kt
package com.example.ssairen_app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ssairen_app.data.local.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
)

class LoginViewModel(private val context: Context) : ViewModel() {

    private val authManager = AuthManager(context)

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun checkAutoLogin(): Boolean {
        return authManager.isAutoLoginEnabled()
    }

    fun login(userId: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState(isLoading = true)

            try {
                // TODO: API 호출
                kotlinx.coroutines.delay(500)

                val validUsers = mapOf(
                    "admin" to "1234",
                    "user1" to "pass1"
                )

                if (validUsers[userId] == password) {
                    authManager.saveLoginInfo(userId)
                    _loginState.value = LoginState(isSuccess = true)
                } else {
                    _loginState.value = LoginState(
                        errorMessage = "아이디 또는 비밀번호가 일치하지 않습니다."
                    )
                }

            } catch (e: Exception) {
                _loginState.value = LoginState(
                    errorMessage = "네트워크 오류: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        authManager.logout()
    }
}