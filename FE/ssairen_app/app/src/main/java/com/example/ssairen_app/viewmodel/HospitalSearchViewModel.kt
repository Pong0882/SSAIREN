package com.example.ssairen_app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.repository.ReportRepository
import com.example.ssairen_app.data.model.response.HospitalAiRecommendationResponse
import com.example.ssairen_app.data.model.response.HospitalSelectionInfo
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

        val currentList = _hospitals.value
        val updatedList = currentList.map { hospital ->
            if (hospital.hospitalSelectionId == hospitalSelectionId) {
                hospital.copy(status = newStatus)
            } else {
                hospital
            }
        }

        _hospitals.value = updatedList
        Log.d(TAG, "✅ 병원 상태 업데이트 완료")
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
