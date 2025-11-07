// data/local/AuthManager.kt
package com.example.ssairen_app.data.local

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACCESS_TOKEN = "access_token"  // ⭐ 이름 변경
        private const val KEY_REFRESH_TOKEN = "refresh_token"  // ⭐ 추가
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIME = "login_time"
    }

    // ⭐ Refresh Token도 함께 저장
    fun saveLoginInfo(userId: String, accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
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

    fun logout() {
        prefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)  // ⭐ 추가
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
}