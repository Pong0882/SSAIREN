# 로그인 통합 가이드

바디캠 기능을 기존 로그인 시스템과 통합하는 방법을 안내합니다.

## 📋 개요

바디캠 기능은 `TokenManager`를 통해 사용자 인증 정보를 관리합니다.
- 토큰은 SharedPreferences에 자동 저장됩니다
- 401 에러 발생 시 자동으로 재로그인을 시도합니다
- 사용자 이름은 녹화 파일의 폴더명에 사용됩니다

## 🔧 구조

```
data/
├── auth/
│   └── TokenManager.kt          # 토큰 및 사용자 정보 관리
├── api/
│   ├── RetrofitClient.kt        # Retrofit 클라이언트 (자동 토큰 갱신)
│   ├── LoginRequest.kt          # 로그인 요청 DTO
│   └── TokenResponse.kt         # 로그인 응답 DTO
```

## 📝 백엔드 DTO 구조

### LoginRequest
```kotlin
{
  "userType": "PARAMEDIC",  // "PARAMEDIC" or "HOSPITAL"
  "username": "20240007",   // 구급대원: 학번, 병원: 병원명
  "password": "Password123!"
}
```

### TokenResponse
```kotlin
{
  "accessToken": "eyJ...",     // JWT 액세스 토큰 (15분 유효)
  "refreshToken": "eyJ...",    // 리프레시 토큰 (7일 유효)
  "userType": "PARAMEDIC",
  "userId": 21,
  "username": "20240007",
  "name": "김민지",             // 구급대원 이름 (폴더명에 사용)
  "tokenType": "Bearer"
}
```

## 🚀 사용 방법

### 1. Application 또는 MainActivity에서 초기화

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // RetrofitClient 초기화 (필수!)
        RetrofitClient.init(this)

        // ... 나머지 코드
    }
}
```

### 2. 로그인 화면에서 로그인 처리

```kotlin
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("PARAMEDIC") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun performLogin() {
        scope.launch {
            try {
                // 1. 로그인 요청
                val loginRequest = LoginRequest(
                    userType = userType,
                    username = username,
                    password = password
                )

                val response = RetrofitClient.fileApiService.login(loginRequest)

                if (response.isSuccessful && response.body()?.success == true) {
                    val tokenData = response.body()?.data

                    if (tokenData != null) {
                        // 2. TokenManager에 로그인 정보 저장
                        val tokenManager = RetrofitClient.getTokenManager()
                        tokenManager.saveLoginInfo(
                            tokenResponse = tokenData,
                            loginUsername = username,
                            loginPassword = password,
                            loginUserType = userType
                        )

                        Log.d("LoginScreen", "Login successful: ${tokenData.name}")

                        // 3. 로그인 성공 - 메인 화면으로 이동
                        onLoginSuccess()
                    } else {
                        errorMessage = "로그인 실패: 토큰 정보 없음"
                    }
                } else {
                    errorMessage = "로그인 실패: ${response.code()}"
                }
            } catch (e: Exception) {
                Log.e("LoginScreen", "Login error", e)
                errorMessage = "로그인 오류: ${e.message}"
            }
        }
    }

    // UI 구현...
}
```

### 3. 로그인 확인 (앱 시작 시)

```kotlin
@Composable
fun App() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // RetrofitClient 초기화
        RetrofitClient.init(context)

        val tokenManager = RetrofitClient.getTokenManager()

        // 로그인 여부 확인
        if (tokenManager.isLoggedIn()) {
            // 이미 로그인됨 -> 메인 화면으로
            Log.d("App", "User already logged in: ${tokenManager.getUserName()}")
        } else {
            // 로그인 필요 -> 로그인 화면으로
            Log.d("App", "User not logged in, showing login screen")
        }
    }
}
```

### 4. 로그아웃

```kotlin
fun logout(context: Context) {
    val tokenManager = RetrofitClient.getTokenManager()
    tokenManager.logout()
    Log.d("Logout", "User logged out successfully")

    // 로그인 화면으로 이동
    // ...
}
```

## 🔄 자동 토큰 갱신

`RetrofitClient`의 `Authenticator`가 401 응답을 자동으로 처리합니다:

1. API 호출 중 401 에러 발생
2. `TokenManager`에서 저장된 로그인 정보 조회
3. 자동으로 재로그인 시도
4. 새 토큰으로 실패한 요청 재시도
5. 최대 2번까지 재시도

**사용자는 토큰 만료를 신경쓰지 않아도 됩니다!**

## 📁 파일 구조

녹화된 영상은 다음 구조로 저장됩니다:

```
날짜/시작시간_사용자명/시작시간_종료시간.mp4

예시:
2025-11-07/
└── 14:30:15_김민지/
    ├── 14:30:15_14:37:15.mp4
    ├── 14:37:15_14:44:15.mp4
    └── 14:44:15_14:50:30.mp4
```

사용자 이름은 `TokenResponse.name` 필드에서 가져옵니다.

## ⚠️ 주의사항

### 필수 초기화

```kotlin
// Application 또는 첫 Activity에서 반드시 호출!
RetrofitClient.init(context)
```

초기화하지 않으면 `IllegalStateException`이 발생합니다.

### 로그인 정보 저장

```kotlin
// 로그인 성공 후 반드시 저장!
tokenManager.saveLoginInfo(
    tokenResponse = tokenData,
    loginUsername = username,      // 재로그인용
    loginPassword = password,      // 재로그인용 (암호화 권장)
    loginUserType = userType
)
```

`loginUsername`, `loginPassword`는 자동 재로그인에 사용되므로 반드시 저장해야 합니다.

## 🧪 현재 테스트 코드 위치

현재 `BodyCamScreen.kt`의 `LaunchedEffect`에 테스트용 자동 로그인 코드가 있습니다:

```kotlin
// BodyCamScreen.kt:55-100
// TODO: 실제 로그인 화면에서 아래 로직 사용
```

**통합 시 이 부분을 제거하고 실제 로그인 화면에서 사용하세요.**

## 📞 문의

구조나 통합 방법에 대한 질문이 있으면 바디캠 기능 개발자에게 문의하세요.
