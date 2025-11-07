// AuthViewModel.kt
package com.example.ssairen_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(AuthManager(application))

    // 로그인 상태
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    // 로그인 여부
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    init {
        checkLoginStatus()
    }

    // DB에서 로그인 상태 확인
    fun checkLoginStatus() {
        _isLoggedIn.value = repository.isLoggedIn()
    }

    // 로그인
    fun login(studentNumber: String, password: String) {
        // 입력값 검증
        if (studentNumber.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("학번과 비밀번호를 입력해주세요")
            return
        }

        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val result = repository.login(studentNumber, password)
            result.onSuccess { paramedic ->
                _loginState.value = LoginState.Success(paramedic)
                _isLoggedIn.value = true
            }.onFailure { error ->
                _loginState.value = LoginState.Error(error.message ?: "로그인 실패")
                _isLoggedIn.value = false
            }
        }
    }

    // 로그아웃
    fun logout() {
        repository.logout()
        _isLoggedIn.value = false
        _loginState.value = LoginState.Idle
    }
}

// ⭐ sealed class 삭제 (별도 파일로 분리했으므로)