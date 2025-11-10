// data/local/AuthManager.kt
package com.example.ssairen_app.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class AuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "AuthManager"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIME = "login_time"
    }

    // ✅ 로그인 정보 저장 (Access Token + Refresh Token)
    fun saveLoginInfo(userId: String, accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            apply()
        }
        Log.d(TAG, "✅ 로그인 정보 저장 완료 - User: $userId")
    }

    // ✅ 토큰만 저장 (토큰 갱신 시 사용)
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
        Log.d(TAG, "✅ 토큰 갱신 완료")
    }

    // ✅ Access Token 가져오기
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    // ✅ Refresh Token 가져오기
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
            remove(KEY_REFRESH_TOKEN)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
        Log.d(TAG, "🗑️ 로그아웃 - 모든 인증 정보 삭제 완료")
    }
}