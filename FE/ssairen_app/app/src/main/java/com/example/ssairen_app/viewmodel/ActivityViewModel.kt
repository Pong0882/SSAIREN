package com.example.ssairen_app.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.repository.ReportRepository
import com.example.ssairen_app.data.model.response.PatientInfoResponse
import com.example.ssairen_app.data.model.response.PatientTypeResponse
import com.example.ssairen_app.data.model.response.PatientEvaResponse
import com.example.ssairen_app.data.model.response.FirstAidResponse
import com.example.ssairen_app.data.model.request.PatientInfoRequest
import com.example.ssairen_app.data.model.request.PatientTypeRequest
import com.example.ssairen_app.data.model.request.PatientEvaRequest
import com.example.ssairen_app.data.model.request.FirstAidRequest

import kotlinx.coroutines.launch

class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReportRepository(AuthManager(application))

    companion object {
        private const val TAG = "ActivityViewModel"

        // ✅ 전역 현재 활성 보고서 ID (앱 전체에서 공유)
        private val _globalCurrentReportId = MutableLiveData<Int>(0)
        val globalCurrentReportId: LiveData<Int> = _globalCurrentReportId

        /**
         * 전역 현재 활성 보고서 ID 설정
         * @param reportId 보고서 ID (0이면 초기화)
         */
        fun setGlobalReportId(reportId: Int) {
            Log.d(TAG, "🌍 전역 보고서 ID 변경: ${_globalCurrentReportId.value} → $reportId")
            _globalCurrentReportId.postValue(reportId)
        }

        /**
         * 전역 현재 활성 보고서 ID 조회
         * @return 현재 활성 보고서 ID (없으면 0)
         */
        fun getGlobalReportId(): Int {
            return _globalCurrentReportId.value ?: 0
        }
    }

    init {
        Log.w(TAG, "⚠️⚠️⚠️ ActivityViewModel 생성됨! ⚠️⚠️⚠️")
        Log.w(TAG, "   호출 스택:")
        Thread.currentThread().stackTrace.take(10).forEach {
            Log.w(TAG, "   at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
        }
    }

    private val _currentEmergencyReportId = MutableLiveData<Int?>()
    val currentEmergencyReportId: LiveData<Int?> = _currentEmergencyReportId

    fun setEmergencyReportId(reportId: Int) {
        Log.d(TAG, "📝 출동보고서 ID 변경: ${_currentEmergencyReportId.value} → $reportId")
        _currentEmergencyReportId.postValue(reportId)
    }

    // ==========================================
    // 환자정보 (조회 + 업데이트)
    // ==========================================
    private val _patientInfoState = MutableLiveData<PatientInfoApiState>(PatientInfoApiState.Idle)
    val patientInfoState: LiveData<PatientInfoApiState> = _patientInfoState

    fun getPatientInfo() {
        val reportId = _currentEmergencyReportId.value
        if (reportId == null) {
            Log.e(TAG, "❌ emergencyReportId가 설정되지 않았습니다")
            _patientInfoState.postValue(PatientInfoApiState.Error("보고서 ID가 설정되지 않았습니다"))
            return
        }
        getPatientInfo(reportId)
    }

    fun getPatientInfo(emergencyReportId: Int) {
        Log.d(TAG, "=== 환자정보 조회 시작 (ViewModel) ===")
        Log.d(TAG, "출동보고서 ID: $emergencyReportId")

        _patientInfoState.postValue(PatientInfoApiState.Loading)

        viewModelScope.launch {
            try {
                val result = repository.getPatientInfo(emergencyReportId)

                result.onSuccess { response ->
                    Log.d(TAG, "✅ 환자정보 조회 성공 (ViewModel)")
                    _patientInfoState.postValue(PatientInfoApiState.Success(response))
                }.onFailure { error ->
                    Log.e(TAG, "❌ 환자정보 조회 실패 (ViewModel): ${error.message}")
                    _patientInfoState.postValue(PatientInfoApiState.Error(error.message ?: "알 수 없는 오류"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 예외 발생 (ViewModel)", e)
                _patientInfoState.postValue(PatientInfoApiState.Error(e.message ?: "알 수 없는 오류"))
            }
        }
    }

    /**
     * 환자정보 업데이트
     */
    fun updatePatientInfo(request: PatientInfoRequest) {
        val reportId = _currentEmergencyReportId.value
        if (reportId == null) {
            Log.e(TAG, "❌ emergencyReportId가 설정되지 않았습니다")
            _patientInfoState.postValue(PatientInfoApiState.UpdateError("보고서 ID가 설정되지 않았습니다"))
            return
        }
        updatePatientInfo(reportId, request)
    }

    fun updatePatientInfo(emergencyReportId: Int, request: PatientInfoRequest) {
        Log.d(TAG, "=== 환자정보 업데이트 시작 (ViewModel) ===")
        Log.d(TAG, "출동보고서 ID: $emergencyReportId")

        _patientInfoState.postValue(PatientInfoApiState.Updating)

        viewModelScope.launch {
            try {
                val result = repository.updatePatientInfo(emergencyReportId, request)

                result.onSuccess { response ->
                    Log.d(TAG, "✅ 환자정보 업데이트 성공 (ViewModel)")
                    _patientInfoState.postValue(PatientInfoApiState.UpdateSuccess(response))
                }.onFailure { error ->
                    Log.e(TAG, "❌ 환자정보 업데이트 실패 (ViewModel): ${error.message}")
                    _patientInfoState.postValue(PatientInfoApiState.UpdateError(error.message ?: "알 수 없는 오류"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 예외 발생 (ViewModel)", e)
                _patientInfoState.postValue(PatientInfoApiState.UpdateError(e.message ?: "알 수 없는 오류"))
            }
        }
    }

    fun resetPatientInfoState() {
        _patientInfoState.postValue(PatientInfoApiState.Idle)
    }

    // ==========================================
    // 환자발생유형 (조회 + 업데이트)
    // ==========================================
    private val _patientTypeState = MutableLiveData<PatientTypeApiState>(PatientTypeApiState.Idle)
    val patientTypeState: LiveData<PatientTypeApiState> = _patientTypeState

    fun getPatientType() {
        val reportId = _currentEmergencyReportId.value
        if (reportId == null) {
            Log.e(TAG, "❌ emergencyReportId가 설정되지 않았습니다")
            _patientTypeState.postValue(PatientTypeApiState.Error("보고서 ID가 설정되지 않았습니다"))
            return
        }
        getPatientType(reportId)
    }

    fun getPatientType(emergencyReportId: Int) {
        Log.d(TAG, "=== 환자발생유형 조회 시작 (ViewModel) ===")
        Log.d(TAG, "출동보고서 ID: $emergencyReportId")

        _patientTypeState.postValue(PatientTypeApiState.Loading)

        viewModelScope.launch {
            try {
                val result = repository.getPatientType(emergencyReportId)

                result.onSuccess { response ->
                    Log.d(TAG, "✅ 환자발생유형 조회 성공 (ViewModel)")
                    _patientTypeState.postValue(PatientTypeApiState.Success(response))
                }.onFailure { error ->
                    Log.e(TAG, "❌ 환자발생유형 조회 실패 (ViewModel): ${error.message}")
                    _patientTypeState.postValue(PatientTypeApiState.Error(error.message ?: "알 수 없는 오류"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 예외 발생 (ViewModel)", e)
                _patientTypeState.postValue(PatientTypeApiState.Error(e.message ?: "알 수 없는 오류"))
            }
        }
    }

    /**
     * 환자발생유형 업데이트
     */
    fun updatePatientType(request: PatientTypeRequest) {
        val reportId = _currentEmergencyReportId.value
        if (reportId == null) {
            Log.e(TAG, "❌ emergencyReportId가 설정되지 않았습니다")
            _patientTypeState.postValue(PatientTypeApiState.UpdateError("보고서 ID가 설정되지 않았습니다"))
            return
        }
        updatePatientType(reportId, request)
    }

    fun updatePatientType(emergencyReportId: Int, request: PatientTypeRequest) {
        Log.d(TAG, "=== 환자발생유형 업데이트 시작 (ViewModel) ===")
        Log.d(TAG, "출동보고서 ID: $emergencyReportId")

        _patientTypeState.postValue(PatientTypeApiState.Updating)

        viewModelScope.launch {
            try {
                val result = repository.updatePatientType(emergencyReportId, request)

                result.onSuccess { response ->
                    Log.d(TAG, "✅ 환자발생유형 업데이트 성공 (ViewModel)")
                    _patientTypeState.postValue(PatientTypeApiState.UpdateSuccess(response))
                }.onFailure { error ->
                    Log.e(TAG, "❌ 환자발생유형 업데이트 실패 (ViewModel): ${error.message}")
                    _patientTypeState.postValue(PatientTypeApiState.UpdateError(error.message ?: "알 수 없는 오류"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 예외 발생 (ViewModel)", e)
                _patientTypeState.postValue(PatientTypeApiState.UpdateError(e.message ?: "알 수 없는 오류"))
            }
        }
    }

    fun resetPatientTypeState() {
        _patientTypeState.postValue(PatientTypeApiState.Idle)
    }

    // ==========================================
    // 환자평가 (조회 + 업데이트)
    // ==========================================
    private val _patientEvaState = MutableLiveData<PatientEvaApiState>(PatientEvaApiState.Idle)
    val patientEvaState: LiveData<PatientEvaApiState> = _patientEvaState

    fun getPatientEva() {
        val reportId = _currentEmergencyReportId.value
        if (reportId == null) {
            Log.e(TAG, "❌ emergencyReportId가 설정되지 않았습니다")
            _patientEvaState.postValue(PatientEvaApiState.Error("보고서 ID가 설정되지 않았습니다"))
            return
        }
        getPatientEva(reportId)
    }

    fun getPatientEva(emergencyReportId: Int) {
        Log.d(TAG, "=== 환자평가 조회 시작 (ViewModel) ===")
        Log.d(TAG, "출동보고서 ID: $emergencyReportId")

        _patientEvaState.postValue(PatientEvaApiState.Loading)

        viewModelScope.launch {
            try {
                val result = repository.getPatientEva(emergencyReportId)

                result.onSuccess { response ->
                    Log.d(TAG, "✅ 환자평가 조회 성공 (ViewModel)")
                    _patientEvaState.postValue(PatientEvaApiState.Success(response))
                }.onFailure { error ->
                    Log.e(TAG, "❌ 환자평가 조회 실패 (ViewModel): ${error.message}")
                    _patientEvaState.postValue(PatientEvaApiState.Error(error.message ?: "알 수 없는 오류"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 예외 발생 (ViewModel)", e)
                _patientEvaState.postValue(PatientEvaApiState.Error(e.message ?: "알 수 없는 오류"))
            }
        }
    }

    /**
     * 환자평가 업데이트
     */
    fun updatePatientEva(request: PatientEvaRequest) {
        val reportId = _currentEmergencyReportId.value
        if (reportId == null) {
            Log.e(TAG, "❌ emergencyReportId가 설정되지 않았습니다")
            _patientEvaState.postValue(PatientEvaApiState.UpdateError("보고서 ID가 설정되지 않았습니다"))
            return
        }
        updatePatientEva(reportId, request)
    }

    fun updatePatientEva(emergencyReportId: Int, request: PatientEvaRequest) {
        Log.d(TAG, "=== 환자평가 업데이트 시작 (ViewModel) ===")
        Log.d(TAG, "출동보고서 ID: $emergencyReportId")

        _patientEvaState.postValue(PatientEvaApiState.Updating)

        viewModelScope.launch {
            try {
                val result = repository.updatePatientEva(emergencyReportId, request)

                result.onSuccess { response ->
                    Log.d(TAG, "✅ 환자평가 업데이트 성공 (ViewModel)")
                    _patientEvaState.postValue(PatientEvaApiState.UpdateSuccess(response))
                }.onFailure { error ->
                    Log.e(TAG, "❌ 환자평가 업데이트 실패 (ViewModel): ${error.message}")
                    _patientEvaState.postValue(PatientEvaApiState.UpdateError(error.message ?: "알 수 없는 오류"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 예외 발생 (ViewModel)", e)
                _patientEvaState.postValue(PatientEvaApiState.UpdateError(e.message ?: "알 수 없는 오류"))
            }
        }
    }

    fun resetPatientEvaState() {
        _patientEvaState.postValue(PatientEvaApiState.Idle)
    }

    // ==========================================
    // 응급처치 (조회 + 업데이트)
    // ==========================================
    private val _firstAidState = MutableLiveData<FirstAidApiState>(FirstAidApiState.Idle)
    val firstAidState: LiveData<FirstAidApiState> = _firstAidState

    fun getFirstAid() {
        val reportId = _currentEmergencyReportId.value
        if (reportId == null) {
            Log.e(TAG, "❌ emergencyReportId가 설정되지 않았습니다")
            _firstAidState.postValue(FirstAidApiState.Error("보고서 ID가 설정되지 않았습니다"))
            return
        }
        getFirstAid(reportId)
    }

    fun getFirstAid(emergencyReportId: Int) {
        Log.d(TAG, "=== 응급처치 조회 시작 (ViewModel) ===")
        Log.d(TAG, "출동보고서 ID: $emergencyReportId")

        _firstAidState.postValue(FirstAidApiState.Loading)

        viewModelScope.launch {
            try {
                val result = repository.getFirstAid(emergencyReportId)

                result.onSuccess { response ->
                    Log.d(TAG, "✅ 응급처치 조회 성공 (ViewModel)")
                    _firstAidState.postValue(FirstAidApiState.Success(response))
                }.onFailure { error ->
                    Log.e(TAG, "❌ 응급처치 조회 실패 (ViewModel): ${error.message}")
                    _firstAidState.postValue(FirstAidApiState.Error(error.message ?: "알 수 없는 오류"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 예외 발생 (ViewModel)", e)
                _firstAidState.postValue(FirstAidApiState.Error(e.message ?: "알 수 없는 오류"))
            }
        }
    }

    /**
     * 응급처치 업데이트
     */
    fun updateFirstAid(request: FirstAidRequest) {
        val reportId = _currentEmergencyReportId.value
        if (reportId == null) {
            Log.e(TAG, "❌ emergencyReportId가 설정되지 않았습니다")
            _firstAidState.postValue(FirstAidApiState.UpdateError("보고서 ID가 설정되지 않았습니다"))
            return
        }
        updateFirstAid(reportId, request)
    }

    fun updateFirstAid(emergencyReportId: Int, request: FirstAidRequest) {
        Log.d(TAG, "=== 응급처치 업데이트 시작 (ViewModel) ===")
        Log.d(TAG, "출동보고서 ID: $emergencyReportId")

        _firstAidState.postValue(FirstAidApiState.Updating)

        viewModelScope.launch {
            try {
                val result = repository.updateFirstAid(emergencyReportId, request)

                result.onSuccess { response ->
                    Log.d(TAG, "✅ 응급처치 업데이트 성공 (ViewModel)")
                    _firstAidState.postValue(FirstAidApiState.UpdateSuccess(response))
                }.onFailure { error ->
                    Log.e(TAG, "❌ 응급처치 업데이트 실패 (ViewModel): ${error.message}")
                    _firstAidState.postValue(FirstAidApiState.UpdateError(error.message ?: "알 수 없는 오류"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 예외 발생 (ViewModel)", e)
                _firstAidState.postValue(FirstAidApiState.UpdateError(e.message ?: "알 수 없는 오류"))
            }
        }
    }

    fun resetFirstAidState() {
        _firstAidState.postValue(FirstAidApiState.Idle)
    }

    override fun onCleared() {
        super.onCleared()
        Log.w(TAG, "🧹 ActivityViewModel 정리됨")
    }
}

// ==========================================
// State 클래스들 (업데이트 상태 추가)
// ==========================================
sealed class PatientInfoApiState {
    object Idle : PatientInfoApiState()
    object Loading : PatientInfoApiState()
    data class Success(val patientInfoResponse: PatientInfoResponse) : PatientInfoApiState()
    data class Error(val message: String) : PatientInfoApiState()

    // 업데이트 상태 추가
    object Updating : PatientInfoApiState()
    data class UpdateSuccess(val patientInfoResponse: PatientInfoResponse) : PatientInfoApiState()
    data class UpdateError(val message: String) : PatientInfoApiState()
}

sealed class PatientTypeApiState {
    object Idle : PatientTypeApiState()
    object Loading : PatientTypeApiState()
    data class Success(val patientTypeResponse: PatientTypeResponse) : PatientTypeApiState()
    data class Error(val message: String) : PatientTypeApiState()

    // 업데이트 상태 추가
    object Updating : PatientTypeApiState()
    data class UpdateSuccess(val patientTypeResponse: PatientTypeResponse) : PatientTypeApiState()
    data class UpdateError(val message: String) : PatientTypeApiState()
}

sealed class PatientEvaApiState {
    object Idle : PatientEvaApiState()
    object Loading : PatientEvaApiState()
    data class Success(val patientEvaResponse: PatientEvaResponse) : PatientEvaApiState()
    data class Error(val message: String) : PatientEvaApiState()

    // 업데이트 상태 추가
    object Updating : PatientEvaApiState()
    data class UpdateSuccess(val patientEvaResponse: PatientEvaResponse) : PatientEvaApiState()
    data class UpdateError(val message: String) : PatientEvaApiState()
}

sealed class FirstAidApiState {
    object Idle : FirstAidApiState()
    object Loading : FirstAidApiState()
    data class Success(val firstAidResponse: FirstAidResponse) : FirstAidApiState()
    data class Error(val message: String) : FirstAidApiState()

    // 업데이트 상태 추가
    object Updating : FirstAidApiState()
    data class UpdateSuccess(val firstAidResponse: FirstAidResponse) : FirstAidApiState()
    data class UpdateError(val message: String) : FirstAidApiState()
}