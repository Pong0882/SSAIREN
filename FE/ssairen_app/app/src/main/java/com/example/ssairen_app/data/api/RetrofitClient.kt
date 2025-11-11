package com.example.ssairen_app.data.api

import android.content.Context
import android.util.Log
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.model.request.LoginRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 클라이언트 싱글톤
 *
 * 사용법:
 * 1. Application 또는 Activity에서 RetrofitClient.init(context) 호출
 * 2. 로그인 후 AuthManager로 토큰 저장
 * 3. API 호출 시 자동으로 토큰이 헤더에 추가됨
 */
object RetrofitClient {

    private const val TAG = "RetrofitClient"

    // ✅ 배포 서버 (실제 사용)
    const val BASE_URL = "https://api.ssairen.site"

    // 로컬 백엔드 테스트용 (USB 연결 + ADB 포트 포워딩)
    // adb reverse tcp:9090 tcp:8080
    // const val BASE_URL = "http://localhost:9090"

    /**
     * AuthManager (로그인 정보 및 토큰 관리)
     */
    private lateinit var authManager: AuthManager

    /**
     * 초기화 (Application 또는 Activity에서 호출 필요)
     */
    fun init(context: Context) {
        if (!::authManager.isInitialized) {
            authManager = AuthManager(context)
            Log.d(TAG, "RetrofitClient initialized with AuthManager")
        }
    }

    /**
     * AuthManager 인스턴스 반환 (외부에서 로그인 정보 저장 시 사용)
     */
    fun getAuthManager(): AuthManager {
        if (!::authManager.isInitialized) {
            throw IllegalStateException("RetrofitClient not initialized. Call init(context) first.")
        }
        return authManager
    }

    /**
     * JWT 인증 인터셉터
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()

        // AuthManager에서 액세스 토큰 조회
        val token = if (::authManager.isInitialized) {
            authManager.getAccessToken()
        } else {
            null
        }

        if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }

    /**
     * 로깅 인터셉터 (디버그용)
     * 비디오 업로드 시 메모리 부족 방지를 위해 HEADERS 레벨 사용
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS  // BODY는 큰 파일 업로드 시 OOM 발생
    }

    /**
     * 토큰 자동 갱신 Authenticator
     * 401 응답을 받으면 자동으로 재로그인 시도
     */
    private val tokenAuthenticator = Authenticator { route: Route?, response: okhttp3.Response ->
        // AuthManager가 초기화되지 않았으면 재시도 불가
        if (!::authManager.isInitialized) {
            Log.e(TAG, "AuthManager not initialized, cannot refresh token")
            return@Authenticator null
        }

        // 이미 재시도한 요청인지 확인 (무한 루프 방지)
        val requestRetryCount = response.request.header("X-Retry-Count")?.toIntOrNull() ?: 0
        if (requestRetryCount >= 2) {
            Log.e(TAG, "Token refresh failed after 2 retries")
            return@Authenticator null
        }

        Log.d(TAG, "Received 401, attempting to refresh token...")

        // 저장된 로그인 자격 증명 조회
        val credentials = authManager.getLoginCredentials()
        if (credentials == null) {
            Log.e(TAG, "No login credentials found, cannot refresh token")
            return@Authenticator null
        }

        // 동기 방식으로 토큰 갱신 시도
        val newToken = runBlocking {
            try {
                // 로그인용 별도 Retrofit 인스턴스 생성 (Authenticator 없음)
                val loginRetrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val loginService = loginRetrofit.create(FileApiService::class.java)
                val loginRequest = LoginRequest(
                    userType = credentials.userType,
                    username = credentials.username,
                    password = credentials.password
                )
                val loginResponse = loginService.login(loginRequest)

                if (loginResponse.isSuccessful && loginResponse.body()?.success == true) {
                    val tokenData = loginResponse.body()?.data
                    if (tokenData != null) {
                        // AuthManager에 새 토큰 저장
                        authManager.saveAccessToken(tokenData.accessToken)
                        Log.d(TAG, "Token refreshed successfully")
                        tokenData.accessToken
                    } else {
                        Log.e(TAG, "Token refresh failed: no token data")
                        null
                    }
                } else {
                    Log.e(TAG, "Token refresh failed: ${loginResponse.code()}")
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Token refresh error", e)
                null
            }
        }

        // 새 토큰으로 요청 재시도
        if (newToken != null) {
            response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .header("X-Retry-Count", (requestRetryCount + 1).toString())
                .build()
        } else {
            null
        }
    }

    /**
     * OkHttp 클라이언트
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)  // 401 응답 시 자동 토큰 갱신
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Retrofit 인스턴스
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * FileApiService 인스턴스
     */
    val fileApiService: FileApiService by lazy {
        retrofit.create(FileApiService::class.java)
    }
}
