package com.example.ssairen_app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.repository.ReportRepository
import com.example.ssairen_app.data.model.response.HospitalAiRecommendationResponse
import com.example.ssairen_app.data.model.response.HospitalSelectionInfo
import com.example.ssairen_app.util.PatientInfoMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 병원 검색 ViewModel
 * AI 기반 병원 추천 및 상태 관리
 */
class HospitalSearchViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "HospitalSearchViewModel"

        // ✅ Singleton 인스턴스 (전역 접근용)
        @Volatile
        private var INSTANCE: HospitalSearchViewModel? = null

        fun getInstance(application: Application): HospitalSearchViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HospitalSearchViewModel(application).also { INSTANCE = it }
            }
        }
    }

    private val repository = ReportRepository(AuthManager(application))

    // AI 추천 상태
    private val _aiRecommendationState = MutableStateFlow<HospitalAiRecommendationState>(
        HospitalAiRecommendationState.Idle
    )
    val aiRecommendationState: StateFlow<HospitalAiRecommendationState> = _aiRecommendationState.asStateFlow()

    // 병원 리스트 (실시간 업데이트용)
    private val _hospitals = MutableStateFlow<List<HospitalSelectionInfo>>(emptyList())
    val hospitals: StateFlow<List<HospitalSelectionInfo>> = _hospitals.asStateFlow()

    /**
     * 환자 정보 생성 API 호출
     * 병원 이송 화면 진입 시 구급일지 데이터를 기반으로 환자 정보 생성
     *
     * @param emergencyReportId 구급일지 ID
     * @return 성공 여부
     */
    suspend fun createPatientInfoForHospital(emergencyReportId: Int): Boolean {
        return try {
            Log.d(TAG, "🏥 환자 정보 생성 시작 (병원 이송용)")
            Log.d(TAG, "   - emergencyReportId: $emergencyReportId")

            // 1. 구급일지의 모든 섹션 데이터 조회
            Log.d(TAG, "📋 섹션 데이터 조회 시작...")
            val patientInfoResult = repository.getPatientInfo(emergencyReportId)
            val patientEvaResult = repository.getPatientEva(emergencyReportId)
            val patientTypeResult = repository.getPatientType(emergencyReportId)
            val dispatchResult = repository.getDispatch(emergencyReportId)

            // 조회 결과 로깅
            Log.d(TAG, "📊 섹션 데이터 조회 결과:")
            Log.d(TAG, "   - patientInfo 성공: ${patientInfoResult.isSuccess}")
            Log.d(TAG, "   - patientEva 성공: ${patientEvaResult.isSuccess}")
            Log.d(TAG, "   - patientType 성공: ${patientTypeResult.isSuccess}")
            Log.d(TAG, "   - dispatch 성공: ${dispatchResult.isSuccess}")

            // 상세 데이터 로깅
            patientInfoResult.getOrNull()?.let { info ->
                Log.d(TAG, "   - patient.age: ${info.data.data.patientInfo.patient?.ageYears}")
                Log.d(TAG, "   - patient.gender: ${info.data.data.patientInfo.patient?.gender}")
            }
            patientEvaResult.getOrNull()?.let { eva ->
                Log.d(TAG, "   - vitalSigns.hr: ${eva.data.data.assessment.vitalSigns?.first?.pulse}")
                Log.d(TAG, "   - vitalSigns.rr: ${eva.data.data.assessment.vitalSigns?.first?.respiration}")
                Log.d(TAG, "   - vitalSigns.spo2: ${eva.data.data.assessment.vitalSigns?.first?.spo2}")
            }

            // 2. 조회된 데이터를 환자 정보 생성 요청으로 맵핑
            val request = PatientInfoMapper.mapToCreatePatientInfoRequest(
                emergencyReportId = emergencyReportId,
                patientInfo = patientInfoResult.getOrNull(),
                patientEva = patientEvaResult.getOrNull(),
                patientType = patientTypeResult.getOrNull(),
                dispatch = dispatchResult.getOrNull()
            )

            Log.d(TAG, "📝 환자 정보 생성 요청 데이터 맵핑 완료")
            Log.d(TAG, "   - gender: ${request.gender}")
            Log.d(TAG, "   - age: ${request.age}")
            Log.d(TAG, "   - hr: ${request.hr}")
            Log.d(TAG, "   - rr: ${request.rr}")
            Log.d(TAG, "   - spo2: ${request.spo2}")
            Log.d(TAG, "   - chiefComplaint: ${request.chiefComplaint}")

            // 3. 환자 정보 생성 API 호출
            val result = repository.createPatientInfo(request)

            result.onSuccess { response ->
                Log.d(TAG, "✅ 환자 정보 생성 성공")
                Log.d(TAG, "   - message: ${response.message}")
            }.onFailure { error ->
                Log.e(TAG, "❌ 환자 정보 생성 실패: ${error.message}")
            }

            result.isSuccess
        } catch (e: Exception) {
            Log.e(TAG, "💥 환자 정보 생성 예외 발생", e)
            false
        }
    }

    /**
     * AI 기반 병원 추천 요청
     *
     * @param emergencyReportId 구급일지 ID
     * @param latitude 현재 위도
     * @param longitude 현재 경도
     * @param radius 검색 반경 (km)
     */
    fun requestAiHospitalRecommendation(
        emergencyReportId: Long,
        latitude: Double,
        longitude: Double,
        radius: Int = 10
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "🏥 AI 병원 추천 요청 시작")
                Log.d(TAG, "   - emergencyReportId: $emergencyReportId")
                Log.d(TAG, "   - 위치: ($latitude, $longitude)")
                Log.d(TAG, "   - 반경: ${radius}km")

                _aiRecommendationState.value = HospitalAiRecommendationState.Loading

                val result = repository.getAiHospitalRecommendation(
                    emergencyReportId = emergencyReportId,
                    latitude = latitude,
                    longitude = longitude,
                    radius = radius
                )

                result.onSuccess { response ->
                    Log.d(TAG, "✅ AI 병원 추천 성공")
                    Log.d(TAG, "   - 추천 병원 수: ${response.data?.recommendedHospitals?.size}")

                    if (response.data != null) {
                        _aiRecommendationState.value = HospitalAiRecommendationState.Success(response)
                        _hospitals.value = response.data.hospitalSelections
                    } else {
                        _aiRecommendationState.value = HospitalAiRecommendationState.Error("응답 데이터가 없습니다")
                    }

                }.onFailure { error ->
                    Log.e(TAG, "❌ AI 병원 추천 실패: ${error.message}")
                    _aiRecommendationState.value = HospitalAiRecommendationState.Error(
                        error.message ?: "알 수 없는 오류가 발생했습니다"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "💥 AI 병원 추천 예외 발생", e)
                _aiRecommendationState.value = HospitalAiRecommendationState.Error(
                    e.message ?: "알 수 없는 오류가 발생했습니다"
                )
            }
        }
    }

    /**
     * 병원 상태 업데이트 (WebSocket에서 호출)
     *
     * @param hospitalSelectionId 병원 선택 ID
     * @param newStatus 새로운 상태 (ACCEPTED, REJECTED, CALLREQUEST 등)
     */
    fun updateHospitalStatus(hospitalSelectionId: Int, newStatus: String) {
        Log.d(TAG, "🔄 병원 상태 업데이트")
        Log.d(TAG, "   - hospitalSelectionId: $hospitalSelectionId")
        Log.d(TAG, "   - newStatus: $newStatus")

        val currentTime = System.currentTimeMillis()
        val currentList = _hospitals.value

        val updatedList = currentList.map { hospital ->
            if (hospital.hospitalSelectionId == hospitalSelectionId) {
                hospital.copy(
                    status = newStatus,
                    responseTime = currentTime  // 응답 받은 시간 기록
                )
            } else {
                hospital
            }
        }

        // 정렬 적용
        val sortedList = sortHospitals(updatedList)

        Log.d(TAG, "📊 정렬 결과:")
        sortedList.forEachIndexed { index, hospital ->
            Log.d(TAG, "  [$index] ${hospital.hospitalName} - ${hospital.status} (responseTime: ${hospital.responseTime})")
        }

        _hospitals.value = sortedList
        Log.d(TAG, "✅ 병원 상태 업데이트 및 정렬 완료")
        Log.d(TAG, "   현재 hospitals.value 크기: ${_hospitals.value.size}")
    }

    /**
     * 병원 리스트 정렬
     * 우선순위: 수용가능(오래된순) > 전화요망(오래된순) > 거절(오래된순) > 요청중(기존순)
     */
    private fun sortHospitals(hospitals: List<HospitalSelectionInfo>): List<HospitalSelectionInfo> {
        return hospitals.sortedWith(compareBy(
            { hospital ->
                // 1차 정렬: 상태별 우선순위
                when (hospital.status) {
                    "ACCEPTED" -> 0                      // 수용 가능 (최우선)
                    "CALLREQUEST", "CALL_REQUEST" -> 1   // 전화 요망 (두 가지 형식 지원)
                    "REJECTED" -> 2                      // 거절
                    "PENDING" -> 3                       // 요청중 (최하위)
                    else -> {
                        Log.w(TAG, "⚠️ 알 수 없는 상태값: '${hospital.status}' (병원: ${hospital.hospitalName})")
                        4  // 알 수 없는 상태는 맨 뒤로
                    }
                }
            },
            { hospital ->
                // 2차 정렬: responseTime
                // PENDING이 아닌 경우 응답 시간순 (오래된 것이 위)
                // PENDING인 경우 원래 순서 유지 (responseTime이 null이므로 자연스럽게 뒤로)
                hospital.responseTime ?: Long.MAX_VALUE
            }
        ))
    }

    /**
     * 상태 초기화
     */
    fun resetState() {
        _aiRecommendationState.value = HospitalAiRecommendationState.Idle
        _hospitals.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 HospitalSearchViewModel 정리됨")
    }
}

/**
 * AI 병원 추천 상태 sealed class
 */
sealed class HospitalAiRecommendationState {
    object Idle : HospitalAiRecommendationState()
    object Loading : HospitalAiRecommendationState()
    data class Success(val response: HospitalAiRecommendationResponse) : HospitalAiRecommendationState()
    data class Error(val message: String) : HospitalAiRecommendationState()
}
