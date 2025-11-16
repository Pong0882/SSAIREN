package com.example.ssairen_app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.repository.ReportRepository
import com.example.ssairen_app.data.model.response.DispatchResponseInfo
import com.example.ssairen_app.data.model.response.IncidentTypeData
import com.example.ssairen_app.data.model.response.PatientAssessmentData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 요약 화면 ViewModel
 * Dispatch, PatientType, PatientEva 섹션 데이터를 조회하여 표시
 */
class SummationViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SummationViewModel"
    }

    private val authManager = AuthManager(application)
    private val repository = ReportRepository(authManager)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _dispatchData = MutableStateFlow<DispatchResponseInfo?>(null)
    val dispatchData: StateFlow<DispatchResponseInfo?> = _dispatchData.asStateFlow()

    private val _patientTypeData = MutableStateFlow<IncidentTypeData?>(null)
    val patientTypeData: StateFlow<IncidentTypeData?> = _patientTypeData.asStateFlow()

    private val _patientEvaData = MutableStateFlow<PatientAssessmentData?>(null)
    val patientEvaData: StateFlow<PatientAssessmentData?> = _patientEvaData.asStateFlow()

    /**
     * 요약 데이터 로드
     * Dispatch, PatientType, PatientEva 섹션 데이터를 조회
     */
    fun loadSummaryData(emergencyReportId: Int) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== 요약 데이터 로드 시작 ===")
                Log.d(TAG, "emergencyReportId: $emergencyReportId")
                _isLoading.value = true

                // 1. Dispatch 데이터 조회
                Log.d(TAG, "📋 Dispatch 데이터 조회 중...")
                val dispatchResult = repository.getDispatch(emergencyReportId)
                dispatchResult.onSuccess { response ->
                    _dispatchData.value = response.data.data.dispatch
                    Log.d(TAG, "✅ Dispatch 데이터 로드 성공")
                }.onFailure { error ->
                    Log.e(TAG, "❌ Dispatch 데이터 로드 실패: ${error.message}")
                    _dispatchData.value = null
                }

                // 2. PatientType 데이터 조회
                Log.d(TAG, "📋 PatientType 데이터 조회 중...")
                val patientTypeResult = repository.getPatientType(emergencyReportId)
                patientTypeResult.onSuccess { response ->
                    _patientTypeData.value = response.data.data.incidentType
                    Log.d(TAG, "✅ PatientType 데이터 로드 성공")
                    Log.d(TAG, "   - category: ${response.data.data.incidentType.category}")
                }.onFailure { error ->
                    Log.e(TAG, "❌ PatientType 데이터 로드 실패: ${error.message}")
                    _patientTypeData.value = null
                }

                // 3. PatientEva 데이터 조회
                Log.d(TAG, "📋 PatientEva 데이터 조회 중...")
                val patientEvaResult = repository.getPatientEva(emergencyReportId)
                patientEvaResult.onSuccess { response ->
                    _patientEvaData.value = response.data.data.assessment
                    Log.d(TAG, "✅ PatientEva 데이터 로드 성공")
                }.onFailure { error ->
                    Log.e(TAG, "❌ PatientEva 데이터 로드 실패: ${error.message}")
                    _patientEvaData.value = null
                }

                Log.d(TAG, "=== 요약 데이터 로드 완료 ===")

            } catch (e: Exception) {
                Log.e(TAG, "💥 요약 데이터 로드 예외 발생", e)
                _dispatchData.value = null
                _patientTypeData.value = null
                _patientEvaData.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 데이터 초기화
     */
    fun clearData() {
        _dispatchData.value = null
        _patientTypeData.value = null
        _patientEvaData.value = null
        Log.d(TAG, "데이터 초기화 완료")
    }
}
