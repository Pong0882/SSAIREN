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
import com.example.ssairen_app.data.model.request.PatientInfoRequest
import com.example.ssairen_app.data.model.request.PatientTypeRequest
import com.example.ssairen_app.data.model.request.PatientEvaRequest
import com.example.ssairen_app.data.model.request.FirstAidRequest
import com.example.ssairen_app.data.model.request.DispatchRequest
import com.example.ssairen_app.data.model.response.DispatchResponse

class ReportRepository(
    private val authManager: AuthManager
) {
    private val api = RetrofitInstance.apiService

    companion object {
        private const val TAG = "ReportRepository"
    }

    // ==========================================
    // 새 일지 등록
    // ==========================================

    /**
     * 새 일지 등록
     * POST /api/emergency-reports/{dispatch_id}
     */
    suspend fun createReport(dispatchId: Int): Result<CreatedReportData> {
        return try {
            Log.d(TAG, "=== 새 일지 등록 시작 ===")
            Log.d(TAG, "📄 출동 ID: $dispatchId")

            val token = authManager.getAccessToken()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중...")

            val response = api.createReport(dispatchId, "Bearer $token")

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success && body.data != null) {
                    Log.d(TAG, "✅ 새 일지 등록 성공!")
                    Log.d(TAG, "출동보고서 ID: ${body.data.emergencyReportId}")
                    Log.d(TAG, "재난 번호: ${body.data.dispatchInfo.disasterNumber}")

                    Result.success(body.data)
                } else {
                    val errorMessage = "새 일지 등록에 실패했습니다"
                    Log.e(TAG, "❌ 새 일지 등록 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    400 -> "잘못된 출동 ID입니다"
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "일지 등록 권한이 없습니다"
                    404 -> "출동 정보를 찾을 수 없습니다"
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

    // ==========================================
    // 조회 메서드들 (기존 코드)
    // ==========================================

    /**
     * 환자정보 조회
     * GET /api/emergency-reports/{emergencyReportId}/sections/PATIENT_INFO
     */
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

    /**
     * 보고서 목록 조회
     * GET /api/emergency-reports/me
     */
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

    /**
     * 환자발생유형 조회
     * GET /api/emergency-reports/{emergencyReportId}/sections/INCIDENT_TYPE
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
     * 환자평가 조회
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

            val response = api.getPatientEva(emergencyReportId, "Bearer $token")

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success) {
                    Log.d(TAG, "✅ 환자평가 조회 성공!")
                    Log.d(TAG, "섹션 ID: ${body.data.id}")
                    Log.d(TAG, "출동보고서 ID: ${body.data.emergencyReportId}")
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
     * 구급출동 조회
     * GET /api/emergency-reports/{emergencyReportId}/sections/DISPATCH
     */
    suspend fun getDispatch(emergencyReportId: Int): Result<DispatchResponse> {
        return try {
            Log.d(TAG, "=== 구급출동 조회 시작 ===")
            Log.d(TAG, "📄 출동보고서 ID: $emergencyReportId")

            val token = authManager.getAccessToken()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중... (type: DISPATCH)")

            val response = api.getDispatch(emergencyReportId, "Bearer $token")

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success) {
                    Log.d(TAG, "✅ 구급출동 조회 성공!")
                    Log.d(TAG, "섹션 ID: ${body.data.id}")
                    Log.d(TAG, "출동보고서 ID: ${body.data.emergencyReportId}")
                    Log.d(TAG, "출동 유형: ${body.data.data.dispatch.dispatchType ?: "없음"}")

                    Result.success(body)
                } else {
                    val errorMessage = "구급출동 조회에 실패했습니다"
                    Log.e(TAG, "❌ 구급출동 조회 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "구급출동 조회 권한이 없습니다"
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
     * 응급처치 조회
     * GET /api/emergency-reports/{emergencyReportId}/sections/TREATMENT
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

    // ==========================================
    // 업데이트 메서드들 (새로 추가)
    // ==========================================

    /**
     * 환자정보 업데이트
     * PATCH /api/emergency-reports/{emergencyReportId}/sections/PATIENT_INFO
     */
    suspend fun updatePatientInfo(
        emergencyReportId: Int,
        request: PatientInfoRequest
    ): Result<PatientInfoResponse> {
        return try {
            Log.d(TAG, "=== 환자정보 업데이트 시작 ===")
            Log.d(TAG, "📄 출동보고서 ID: $emergencyReportId")
            Log.d(TAG, "📝 요청 데이터: 환자명=${request.data.patientInfo.patient?.name}, 성별=${request.data.patientInfo.patient?.gender}")

            val token = authManager.getAccessToken()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중... (PATCH PATIENT_INFO)")

            val response = api.updatePatientInfo(
                emergencyReportId = emergencyReportId,
                token = "Bearer $token",
                request = request
            )

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success) {
                    Log.d(TAG, "✅ 환자정보 업데이트 성공!")
                    Log.d(TAG, "섹션 ID: ${body.data.id}")
                    Log.d(TAG, "업데이트 시간: ${body.data.data.patientInfo.updatedAt}")

                    Result.success(body)
                } else {
                    val errorMessage = "환자정보 업데이트에 실패했습니다"
                    Log.e(TAG, "❌ 환자정보 업데이트 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    400 -> "잘못된 요청입니다. 입력값을 확인해주세요"
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "환자정보 수정 권한이 없습니다"
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
     * 환자발생유형 업데이트
     * PATCH /api/emergency-reports/{emergencyReportId}/sections/INCIDENT_TYPE
     */
    suspend fun updatePatientType(
        emergencyReportId: Int,
        request: PatientTypeRequest
    ): Result<PatientTypeResponse> {
        return try {
            Log.d(TAG, "=== 환자발생유형 업데이트 시작 ===")
            Log.d(TAG, "📄 출동보고서 ID: $emergencyReportId")
            Log.d(TAG, "📝 요청 데이터: 카테고리=${request.data.incidentType.category}")

            val token = authManager.getAccessToken()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중... (PATCH INCIDENT_TYPE)")

            val response = api.updatePatientType(
                emergencyReportId = emergencyReportId,
                token = "Bearer $token",
                request = request
            )

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success) {
                    Log.d(TAG, "✅ 환자발생유형 업데이트 성공!")
                    Log.d(TAG, "섹션 ID: ${body.data.id}")
                    Log.d(TAG, "업데이트 시간: ${body.data.data.incidentType.updatedAt}")

                    Result.success(body)
                } else {
                    val errorMessage = "환자발생유형 업데이트에 실패했습니다"
                    Log.e(TAG, "❌ 환자발생유형 업데이트 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    400 -> "잘못된 요청입니다. 입력값을 확인해주세요"
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "환자발생유형 수정 권한이 없습니다"
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
     * 환자평가 업데이트
     * PATCH /api/emergency-reports/{emergencyReportId}/sections/ASSESSMENT
     */
    suspend fun updatePatientEva(
        emergencyReportId: Int,
        request: PatientEvaRequest
    ): Result<PatientEvaResponse> {
        return try {
            Log.d(TAG, "=== 환자평가 업데이트 시작 ===")
            Log.d(TAG, "📄 출동보고서 ID: $emergencyReportId")
            Log.d(TAG, "📝 요청 데이터: 환자레벨=${request.data.assessment.patientLevel}")

            val token = authManager.getAccessToken()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중... (PATCH ASSESSMENT)")

            val response = api.updatePatientEva(
                emergencyReportId = emergencyReportId,
                token = "Bearer $token",
                request = request
            )

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success) {
                    Log.d(TAG, "✅ 환자평가 업데이트 성공!")
                    Log.d(TAG, "섹션 ID: ${body.data.id}")
                    Log.d(TAG, "업데이트 시간: ${body.data.data.assessment.updatedAt}")

                    Result.success(body)
                } else {
                    val errorMessage = "환자평가 업데이트에 실패했습니다"
                    Log.e(TAG, "❌ 환자평가 업데이트 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    400 -> "잘못된 요청입니다. 입력값을 확인해주세요"
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "환자평가 수정 권한이 없습니다"
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
     * 구급출동 업데이트
     * PATCH /api/emergency-reports/{emergencyReportId}/sections/DISPATCH
     */
    suspend fun updateDispatch(
        emergencyReportId: Int,
        request: DispatchRequest
    ): Result<DispatchResponse> {
        return try {
            Log.d(TAG, "=== 구급출동 업데이트 시작 ===")
            Log.d(TAG, "📄 출동보고서 ID: $emergencyReportId")
            Log.d(TAG, "📝 요청 데이터: 출동유형=${request.data.dispatch.dispatchType}")

            val token = authManager.getAccessToken()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중... (PATCH DISPATCH)")

            val response = api.updateDispatch(
                emergencyReportId = emergencyReportId,
                token = "Bearer $token",
                request = request
            )

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success) {
                    Log.d(TAG, "✅ 구급출동 업데이트 성공!")
                    Log.d(TAG, "섹션 ID: ${body.data.id}")
                    Log.d(TAG, "업데이트 시간: ${body.data.data.dispatch.updatedAt}")

                    Result.success(body)
                } else {
                    val errorMessage = "구급출동 업데이트에 실패했습니다"
                    Log.e(TAG, "❌ 구급출동 업데이트 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    400 -> "잘못된 요청입니다. 입력값을 확인해주세요"
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "구급출동 수정 권한이 없습니다"
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
     * 응급처치 업데이트
     * PATCH /api/emergency-reports/{emergencyReportId}/sections/TREATMENT
     */
    suspend fun updateFirstAid(
        emergencyReportId: Int,
        request: FirstAidRequest
    ): Result<FirstAidResponse> {
        return try {
            Log.d(TAG, "=== 응급처치 업데이트 시작 ===")
            Log.d(TAG, "📄 출동보고서 ID: $emergencyReportId")
            Log.d(TAG, "📝 요청 데이터: CPR=${request.data.treatment.cpr}")

            val token = authManager.getAccessToken()

            if (token == null) {
                Log.e(TAG, "❌ Access Token이 없습니다")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.d(TAG, "🔑 Access Token (앞 20자): ${token.take(20)}...")
            Log.d(TAG, "API 호출 중... (PATCH TREATMENT)")

            val response = api.updateFirstAid(
                emergencyReportId = emergencyReportId,
                token = "Bearer $token",
                request = request
            )

            Log.d(TAG, "응답 코드: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Log.d(TAG, "응답 바디 success: ${body.success}")

                if (body.success) {
                    Log.d(TAG, "✅ 응급처치 업데이트 성공!")
                    Log.d(TAG, "섹션 ID: ${body.data.id}")
                    Log.d(TAG, "업데이트 시간: ${body.data.data.treatment.updatedAt}")

                    Result.success(body)
                } else {
                    val errorMessage = "응급처치 업데이트에 실패했습니다"
                    Log.e(TAG, "❌ 응급처치 업데이트 실패: $errorMessage")
                    Result.failure(Exception(errorMessage))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ HTTP 오류 - 코드: ${response.code()}")
                Log.e(TAG, "에러 바디: $errorBody")

                val errorMsg = when (response.code()) {
                    400 -> "잘못된 요청입니다. 입력값을 확인해주세요"
                    401 -> "인증이 만료되었습니다. 다시 로그인해주세요"
                    403 -> "응급처치 수정 권한이 없습니다"
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