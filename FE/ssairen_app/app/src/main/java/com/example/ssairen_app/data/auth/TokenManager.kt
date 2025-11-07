package com.example.ssairen_app.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.ssairen_app.data.api.TokenResponse

/**
 * 토큰 및 사용자 정보 관리 클래스
 * SharedPreferences를 사용하여 로그인 정보를 저장/관리합니다.
 */
class TokenManager private constructor(context: Context) {

    companion object {
        private const val TAG = "TokenManager"
        private const val PREF_NAME = "ssairen_auth"

        // SharedPreferences Keys
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_TYPE = "user_type"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_NAME = "name"
        private const val KEY_LOGIN_USERNAME = "login_username"
        private const val KEY_LOGIN_PASSWORD = "login_password"
        private const val KEY_LOGIN_USER_TYPE = "login_user_type"

        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * 로그인 정보 저장 (토큰 응답 + 로그인 자격 증명)
     */
    fun saveLoginInfo(
        tokenResponse: TokenResponse,
        loginUsername: String,
        loginPassword: String,
        loginUserType: String
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, tokenResponse.accessToken)
            putString(KEY_REFRESH_TOKEN, tokenResponse.refreshToken)
            putString(KEY_USER_TYPE, tokenResponse.userType)
            putInt(KEY_USER_ID, tokenResponse.userId)
            putString(KEY_USERNAME, tokenResponse.username)
            putString(KEY_NAME, tokenResponse.name ?: tokenResponse.username)
            putString(KEY_LOGIN_USERNAME, loginUsername)
            putString(KEY_LOGIN_PASSWORD, loginPassword)
            putString(KEY_LOGIN_USER_TYPE, loginUserType)
            apply()
        }
        Log.d(TAG, "Login info saved: user=${tokenResponse.name ?: tokenResponse.username}")
    }

    /**
     * 액세스 토큰 저장 (토큰 갱신 시 사용)
     */
    fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply()
        Log.d(TAG, "Access token updated")
    }

    /**
     * 액세스 토큰 조회
     */
    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * 리프레시 토큰 조회
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * 사용자 이름 조회 (폴더명에 사용)
     */
    fun getUserName(): String {
        return sharedPreferences.getString(KEY_NAME, "사용자") ?: "사용자"
    }

    /**
     * 로그인 자격 증명 조회 (자동 재로그인용)
     */
    fun getLoginCredentials(): LoginCredentials? {
        val username = sharedPreferences.getString(KEY_LOGIN_USERNAME, null)
        val password = sharedPreferences.getString(KEY_LOGIN_PASSWORD, null)
        val userType = sharedPreferences.getString(KEY_LOGIN_USER_TYPE, null)

        return if (username != null && password != null && userType != null) {
            LoginCredentials(username, password, userType)
        } else {
            null
        }
    }

    /**
     * 로그인 여부 확인
     */
    fun isLoggedIn(): Boolean {
        return getAccessToken() != null
    }

    /**
     * 로그아웃 (모든 정보 삭제)
     */
    fun logout() {
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "Logged out, all data cleared")
    }

    /**
     * 로그인 자격 증명 데이터 클래스
     */
    data class LoginCredentials(
        val username: String,
        val password: String,
        val userType: String
    )
}
