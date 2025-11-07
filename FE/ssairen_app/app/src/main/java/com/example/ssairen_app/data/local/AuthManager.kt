// data/local/AuthManager.kt
package com.example.ssairen_app.data.local

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"  // ⭐ 사용자 이름 추가
        private const val KEY_ACCESS_TOKEN = "access_token"  // ⭐ 이름 변경
        private const val KEY_REFRESH_TOKEN = "refresh_token"  // ⭐ 추가
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIME = "login_time"
        private const val KEY_LOGIN_USERNAME = "login_username"  // ⭐ 재로그인용
        private const val KEY_LOGIN_PASSWORD = "login_password"  // ⭐ 재로그인용
        private const val KEY_LOGIN_USER_TYPE = "login_user_type"  // ⭐ 재로그인용
    }

    // ⭐ 로그인 정보 저장 (토큰 + 사용자 정보 + 재로그인 자격증명)
    fun saveLoginInfo(
        userId: String,
        userName: String,
        accessToken: String,
        refreshToken: String,
        loginUsername: String,
        loginPassword: String,
        loginUserType: String
    ) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putString(KEY_LOGIN_USERNAME, loginUsername)
            putString(KEY_LOGIN_PASSWORD, loginPassword)
            putString(KEY_LOGIN_USER_TYPE, loginUserType)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            apply()
        }
    }

    // ⭐ Access Token 가져오기
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    // ⭐ Refresh Token 가져오기
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    // ⭐ 기존 호환성을 위해 남겨둠 (deprecated)
    @Deprecated("Use getAccessToken() instead", ReplaceWith("getAccessToken()"))
    fun getToken(): String? {
        return getAccessToken()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getAccessToken() != null
    }

    fun isAutoLoginEnabled(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getSavedUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    // ⭐ 사용자 이름 가져오기 (비디오 폴더명에 사용)
    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "사용자") ?: "사용자"
    }

    // ⭐ 로그인 자격 증명 가져오기 (자동 재로그인용)
    fun getLoginCredentials(): LoginCredentials? {
        val username = prefs.getString(KEY_LOGIN_USERNAME, null)
        val password = prefs.getString(KEY_LOGIN_PASSWORD, null)
        val userType = prefs.getString(KEY_LOGIN_USER_TYPE, null)

        return if (username != null && password != null && userType != null) {
            LoginCredentials(username, password, userType)
        } else {
            null
        }
    }

    // ⭐ Access Token만 업데이트 (토큰 갱신 시 사용)
    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun logout() {
        prefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_LOGIN_USERNAME)
            remove(KEY_LOGIN_PASSWORD)
            remove(KEY_LOGIN_USER_TYPE)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }

    // ⭐ 로그인 자격 증명 데이터 클래스
    data class LoginCredentials(
        val username: String,
        val password: String,
        val userType: String
    )
}