// ReportRepository.kt
package com.example.ssairen_app.data.repository

import android.util.Log
import com.example.ssairen_app.data.api.RetrofitInstance
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.model.response.CreatedReportData
import com.example.ssairen_app.data.model.response.MyReportsData
import com.example.ssairen_app.data.model.response.ReportListResponse
import com.example.ssairen_app.data.model.response.PatientInfoResponse
import com.example.ssairen_app.data.model.response.PatientTypeResponse
import com.example.ssairen_app.data.model.response.PatientEvaResponse
import com.example.ssairen_app.data.model.response.FirstAidResponse

class ReportRepository(
    private val authManager: AuthManager
) {
    private val api = RetrofitInstance.apiService

    companion object {
        private const val TAG = "ReportRepository"
    }

    suspend fun getPatientInfo(emergencyReportId: Int): Result<PatientInfoResponse> {
        return try {
            Log.d(TAG, "=== 환자정보 조회 시작 ===")
            Log.d(TAG, "📄 출동보고서 ID: $emergencyReportId")

            val token = authManager.getAccessToken()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중... (type: PATIENT_INFO)")

            val response = api.getEmergencyReportSection(
                emergencyReportId = emergencyReportId,
                token = "Bearer $token"
            )

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success) {
                    Log.d(TAG, "✅ 환자정보 조회 성공!")
                    Log.d(TAG, "섹션 ID: ${body.data.id}")
                    Log.d(TAG, "출동보고서 ID: ${body.data.emergencyReportId}")
                    Log.d(TAG, "환자 이름: ${body.data.data.patientInfo.patient?.name ?: "없음"}")

                    Result.success(body)
                } else {
                    val errorMessage = "환자정보 조회에 실패했습니다"
                    Log.e(TAG, "❌ 환자정보 조회 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "환자정보 조회 권한이 없습니다"
                    404 -> "해당 보고서를 찾을 수 없습니다"
                    500 -> "서버 내부 오류가 발생했습니다"
                    else -> "서버 오류: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 예외 발생!", e)

            val errorMsg = when {
                e.message?.contains("Unable to resolve host") == true ->
                    "인터넷 연결을 확인해주세요"
                e.message?.contains("timeout") == true ->
                    "서버 응답 시간이 초과되었습니다"
                else ->
                    "네트워크 오류: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    suspend fun getReports(page: Int = 0, size: Int = 10): Result<MyReportsData> {
        return try {
            Log.d(TAG, "=== 보고서 목록 조회 시작 ===")
            Log.d(TAG, "📄 페이지: $page, 사이즈: $size")

            val token = authManager.getAccessToken()
            val userId = authManager.getSavedUserId()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "👤 현재 로그인된 사용자 ID: $userId")
            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중...")
            val response = api.getReports("Bearer $token", page, size)
            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body: ReportListResponse = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success && body.data != null) {
                    Log.d(TAG, "✅ 보고서 목록 조회 성공!")
                    Log.d(TAG, "보고서 개수: ${body.data.emergencyReports.size}")

                    body.data.emergencyReports.forEachIndexed { index, report ->
                        Log.d(TAG, "📄 보고서 [$index] ID: ${report.id}, 재난번호: ${report.dispatchInfo.disasterNumber}, 날짜: ${report.dispatchInfo.date}")
                    }

                    Result.success(body.data)
                } else {
                    val errorMessage = body.error?.message ?: "보고서 목록 조회에 실패했습니다"

                    Log.e(TAG, "❌ 보고서 목록 조회 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "보고서 조회 권한이 없습니다"
                    404 -> "서버를 찾을 수 없습니다"
                    500 -> "서버 내부 오류가 발생했습니다"
                    else -> "서버 오류: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 예외 발생!", e)

            val errorMsg = when {
                e.message?.contains("Unable to resolve host") == true ->
                    "인터넷 연결을 확인해주세요"
                e.message?.contains("timeout") == true ->
                    "서버 응답 시간이 초과되었습니다"
                else ->
                    "네트워크 오류: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    // ✅ 임시로 주석처리 - API 대신 모달창에서 직접 이동
    /**
     * 새 일지 등록
     */
//    suspend fun createReport(dispatchId: Int): Result<CreatedReportData> {
//        return try {
//            Log.d(TAG, "=== 새 일지 등록 시작 ===")
//            Log.d(TAG, "Dispatch ID: $dispatchId")
//
//            val token = authManager.getAccessToken()
//
//            if (token == null) {
//                Log.e(TAG, "❌ Access Token이 없습니다")
//                return Result.failure(Exception("로그인이 필요합니다"))
//            }
//
//            Log.d(TAG, "API 호출 중...")
//            val response = api.createReport(dispatchId, "Bearer $token")
//            Log.d(TAG, "응답 코드: ${response.code()}")
//
//            if (response.isSuccessful && response.body() != null) {
//                val body = response.body()!!
//                Log.d(TAG, "응답 바디 success: ${body.success}")
//
//                if (body.success && body.data != null) {
//                    Log.d(TAG, "✅ 일지 생성 성공!")
//                    Log.d(TAG, "일지 ID: ${body.data.emergencyReportId}")
//                    Log.d(TAG, "재난번호: ${body.data.dispatchInfo.disasterNumber}")
//
//                    Result.success(body.data)
//                } else {
//                    val errorMessage = body.error?.message
//                        ?: body.message
//                        ?: "일지 생성에 실패했습니다"
//
//                    Log.e(TAG, "❌ 일지 생성 실패: $errorMessage")
//                    Result.failure(Exception(errorMessage))
//                }
//            } else {
//                val errorBody = response.errorBody()?.string()
//                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
//                Log.e(TAG, "에러 바디: $errorBody")
//
//                val errorMsg = when (response.code()) {
//                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
//                    403 -> "일지 생성 권한이 없습니다"
//                    404 -> "서버를 찾을 수 없습니다"
//                    500 -> "서버 내부 오류가 발생했습니다"
//                    else -> "서버 오류: ${response.code()}"
//                }
//                Result.failure(Exception(errorMsg))
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "💥 예외 발생!", e)
//
//            val errorMsg = when {
//                e.message?.contains("Unable to resolve host") == true ->
//                    "인터넷 연결을 확인해주세요"
//                e.message?.contains("timeout") == true ->
//                    "서버 응답 시간이 초과되었습니다"
//                else ->
//                    "네트워크 오류: ${e.message}"
//            }
//            Result.failure(Exception(errorMsg))
//        }
//    }

    /**
     * 환자발생유형 섹션 조회
     * GET /api/emergency-reports/{id}/sections/INCIDENT_TYPE
     */
    suspend fun getPatientType(emergencyReportId: Int): Result<PatientTypeResponse> {
        return try {
            Log.d(TAG, "=== 환자발생유형 조회 시작 ===")
            Log.d(TAG, "📄 출동보고서 ID: $emergencyReportId")

            val token = authManager.getAccessToken()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중... (type: INCIDENT_TYPE)")

            val response = api.getPatientType(emergencyReportId, "Bearer $token")

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success) {
                    Log.d(TAG, "✅ 환자발생유형 조회 성공!")
                    Log.d(TAG, "섹션 ID: ${body.data.id}")
                    Log.d(TAG, "출동보고서 ID: ${body.data.emergencyReportId}")
                    Log.d(TAG, "카테고리: ${body.data.data.incidentType.category ?: "없음"}")

                    Result.success(body)
                } else {
                    val errorMessage = "환자발생유형 조회에 실패했습니다"
                    Log.e(TAG, "❌ 환자발생유형 조회 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "환자발생유형 조회 권한이 없습니다"
                    404 -> "해당 보고서를 찾을 수 없습니다"
                    500 -> "서버 내부 오류가 발생했습니다"
                    else -> "서버 오류: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 예외 발생!", e)

            val errorMsg = when {
                e.message?.contains("Unable to resolve host") == true ->
                    "인터넷 연결을 확인해주세요"
                e.message?.contains("timeout") == true ->
                    "서버 응답 시간이 초과되었습니다"
                else ->
                    "네트워크 오류: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * 환자평가 섹션 조회
     * GET /api/emergency-reports/{id}/sections/ASSESSMENT
     */
    suspend fun getPatientEva(emergencyReportId: Int): Result<PatientEvaResponse> {
        return try {
            Log.d(TAG, "=== 환자평가 조회 시작 ===")
            Log.d(TAG, "📄 출동보고서 ID: $emergencyReportId")

            val token = authManager.getAccessToken()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중... (type: ASSESSMENT)")

            // ✅ Bearer 토큰 추가
            val response = api.getPatientEva(emergencyReportId, "Bearer $token")

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success) {
                    Log.d(TAG, "✅ 환자평가 조회 성공!")
                    Log.d(TAG, "섹션 ID: ${body.data.id}")
                    Log.d(TAG, "출동보고서 ID: ${body.data.emergencyReportId}")
                    // ✅ 수정: patientAssessment → assessment
                    Log.d(TAG, "환자 레벨: ${body.data.data.assessment.patientLevel ?: "없음"}")

                    Result.success(body)
                } else {
                    val errorMessage = "환자평가 조회에 실패했습니다"
                    Log.e(TAG, "❌ 환자평가 조회 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "환자평가 조회 권한이 없습니다"
                    404 -> "해당 보고서를 찾을 수 없습니다"
                    500 -> "서버 내부 오류가 발생했습니다"
                    else -> "서버 오류: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 예외 발생!", e)

            val errorMsg = when {
                e.message?.contains("Unable to resolve host") == true ->
                    "인터넷 연결을 확인해주세요"
                e.message?.contains("timeout") == true ->
                    "서버 응답 시간이 초과되었습니다"
                else ->
                    "네트워크 오류: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }

    /**
     * 응급처치 섹션 조회
     * GET /api/emergency-reports/{id}/sections/TREATMENT
     */
    suspend fun getFirstAid(emergencyReportId: Int): Result<FirstAidResponse> {
        return try {
            Log.d(TAG, "=== 응급처치 조회 시작 ===")
            Log.d(TAG, "📄 출동보고서 ID: $emergencyReportId")

            val token = authManager.getAccessToken()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중... (type: TREATMENT)")

            val response = api.getFirstAid(emergencyReportId, "Bearer $token")

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success) {
                    Log.d(TAG, "✅ 응급처치 조회 성공!")
                    Log.d(TAG, "섹션 ID: ${body.data.id}")
                    Log.d(TAG, "출동보고서 ID: ${body.data.emergencyReportId}")
                    // ✅ 수정: emergencyTreatment → treatment, cpr은 String 타입
                    Log.d(TAG, "CPR: ${body.data.data.treatment.cpr ?: "없음"}")
                    Log.d(TAG, "기도 관리 방법: ${body.data.data.treatment.airwayManagement?.methods?.joinToString(", ") ?: "없음"}")

                    Result.success(body)
                } else {
                    val errorMessage = "응급처치 조회에 실패했습니다"
                    Log.e(TAG, "❌ 응급처치 조회 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "응급처치 조회 권한이 없습니다"
                    404 -> "해당 보고서를 찾을 수 없습니다"
                    500 -> "서버 내부 오류가 발생했습니다"
                    else -> "서버 오류: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 예외 발생!", e)

            val errorMsg = when {
                e.message?.contains("Unable to resolve host") == true ->
                    "인터넷 연결을 확인해주세요"
                e.message?.contains("timeout") == true ->
                    "서버 응답 시간이 초과되었습니다"
                else ->
                    "네트워크 오류: ${e.message}"
            }
            Result.failure(Exception(errorMsg))
        }
    }
}