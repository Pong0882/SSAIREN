// data/local/AuthManager.kt
package com.example.ssairen_app.data.local

import android.content.Context
import android.content.SharedPreferences

class AuthManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIME = "login_time"
    }

    fun saveLoginInfo(userId: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LOGIN_TIME, System.currentTimeMillis())
            apply()
        }
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
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
}