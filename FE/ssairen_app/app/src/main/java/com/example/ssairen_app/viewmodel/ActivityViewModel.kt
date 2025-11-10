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
import kotlinx.coroutines.launch

class ActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReportRepository(AuthManager(application))

    companion object {
        private const val TAG = "ActivityViewModel"
        private const val HARDCODED_REPORT_ID = 21
    }

    init {
        Log.w(TAG, "⚠️⚠️⚠️ ActivityViewModel 생성됨! ⚠️⚠️⚠️")
        Log.w(TAG, "   호출 스택:")
        Thread.currentThread().stackTrace.take(10).forEach {
            Log.w(TAG, "   at ${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})")
        }
    }

    private val _currentEmergencyReportId = MutableLiveData<Int>().apply {
        value = HARDCODED_REPORT_ID
    }
    val currentEmergencyReportId: LiveData<Int> = _currentEmergencyReportId

    fun setEmergencyReportId(reportId: Int) {
        Log.d(TAG, "📝 출동보고서 ID 변경: ${_currentEmergencyReportId.value} → $reportId")
        _currentEmergencyReportId.postValue(reportId)
    }

    // ==========================================
    // 환자정보 (현재 사용 중)
    // ==========================================
    private val _patientInfoState = MutableLiveData<PatientInfoApiState>(PatientInfoApiState.Idle)
    val patientInfoState: LiveData<PatientInfoApiState> = _patientInfoState

    fun getPatientInfo() {
        val reportId = _currentEmergencyReportId.value ?: HARDCODED_REPORT_ID
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

    fun resetPatientInfoState() {
        _patientInfoState.postValue(PatientInfoApiState.Idle)
    }

//    // ==========================================
//    // 환자발생유형 (주석 처리 - PatientInfo 확인 후 사용)
//    // ==========================================
//    private val _patientTypeState = MutableLiveData<PatientTypeApiState>(PatientTypeApiState.Idle)
//    val patientTypeState: LiveData<PatientTypeApiState> = _patientTypeState
//
//    fun getPatientType() {
//        val reportId = _currentEmergencyReportId.value ?: HARDCODED_REPORT_ID
//        getPatientType(reportId)
//    }
//
//    fun getPatientType(emergencyReportId: Int) {
//        Log.d(TAG, "=== 환자발생유형 조회 시작 (ViewModel) ===")
//        Log.d(TAG, "출동보고서 ID: $emergencyReportId")
//
//        _patientTypeState.postValue(PatientTypeApiState.Loading)
//
//        viewModelScope.launch {
//            try {
//                val result = repository.getPatientType(emergencyReportId)
//
//                result.onSuccess { response ->
//                    Log.d(TAG, "✅ 환자발생유형 조회 성공 (ViewModel)")
//                    _patientTypeState.postValue(PatientTypeApiState.Success(response))
//                }.onFailure { error ->
//                    Log.e(TAG, "❌ 환자발생유형 조회 실패 (ViewModel): ${error.message}")
//                    _patientTypeState.postValue(PatientTypeApiState.Error(error.message ?: "알 수 없는 오류"))
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "💥 예외 발생 (ViewModel)", e)
//                _patientTypeState.postValue(PatientTypeApiState.Error(e.message ?: "알 수 없는 오류"))
//            }
//        }
//    }
//
//    fun resetPatientTypeState() {
//        _patientTypeState.postValue(PatientTypeApiState.Idle)
//    }
//
//    // ==========================================
//    // 환자평가 (주석 처리 - PatientInfo 확인 후 사용)
//    // ==========================================
//    private val _patientEvaState = MutableLiveData<PatientEvaApiState>(PatientEvaApiState.Idle)
//    val patientEvaState: LiveData<PatientEvaApiState> = _patientEvaState
//
//    fun getPatientEva() {
//        val reportId = _currentEmergencyReportId.value ?: HARDCODED_REPORT_ID
//        getPatientEva(reportId)
//    }
//
//    fun getPatientEva(emergencyReportId: Int) {
//        Log.d(TAG, "=== 환자평가 조회 시작 (ViewModel) ===")
//        Log.d(TAG, "출동보고서 ID: $emergencyReportId")
//
//        _patientEvaState.postValue(PatientEvaApiState.Loading)
//
//        viewModelScope.launch {
//            try {
//                val result = repository.getPatientEva(emergencyReportId)
//
//                result.onSuccess { response ->
//                    Log.d(TAG, "✅ 환자평가 조회 성공 (ViewModel)")
//                    _patientEvaState.postValue(PatientEvaApiState.Success(response))
//                }.onFailure { error ->
//                    Log.e(TAG, "❌ 환자평가 조회 실패 (ViewModel): ${error.message}")
//                    _patientEvaState.postValue(PatientEvaApiState.Error(error.message ?: "알 수 없는 오류"))
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "💥 예외 발생 (ViewModel)", e)
//                _patientEvaState.postValue(PatientEvaApiState.Error(e.message ?: "알 수 없는 오류"))
//            }
//        }
//    }
//
//    fun resetPatientEvaState() {
//        _patientEvaState.postValue(PatientEvaApiState.Idle)
//    }
//
//    // ==========================================
//    // 응급처치 (주석 처리 - PatientInfo 확인 후 사용)
//    // ==========================================
//    private val _firstAidState = MutableLiveData<FirstAidApiState>(FirstAidApiState.Idle)
//    val firstAidState: LiveData<FirstAidApiState> = _firstAidState
//
//    fun getFirstAid() {
//        val reportId = _currentEmergencyReportId.value ?: HARDCODED_REPORT_ID
//        getFirstAid(reportId)
//    }
//
//    fun getFirstAid(emergencyReportId: Int) {
//        Log.d(TAG, "=== 응급처치 조회 시작 (ViewModel) ===")
//        Log.d(TAG, "출동보고서 ID: $emergencyReportId")
//
//        _firstAidState.postValue(FirstAidApiState.Loading)
//
//        viewModelScope.launch {
//            try {
//                val result = repository.getFirstAid(emergencyReportId)
//
//                result.onSuccess { response ->
//                    Log.d(TAG, "✅ 응급처치 조회 성공 (ViewModel)")
//                    _firstAidState.postValue(FirstAidApiState.Success(response))
//                }.onFailure { error ->
//                    Log.e(TAG, "❌ 응급처치 조회 실패 (ViewModel): ${error.message}")
//                    _firstAidState.postValue(FirstAidApiState.Error(error.message ?: "알 수 없는 오류"))
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "💥 예외 발생 (ViewModel)", e)
//                _firstAidState.postValue(FirstAidApiState.Error(e.message ?: "알 수 없는 오류"))
//            }
//        }
//    }
//
//    fun resetFirstAidState() {
//        _firstAidState.postValue(FirstAidApiState.Idle)
//    }

    override fun onCleared() {
        super.onCleared()
        Log.w(TAG, "🧹 ActivityViewModel 정리됨")
    }
}

// ==========================================
// State 클래스들 (이름 변경: XXXState → XXXApiState)
// ==========================================
sealed class PatientInfoApiState {
    object Idle : PatientInfoApiState()
    object Loading : PatientInfoApiState()
    data class Success(val patientInfoResponse: PatientInfoResponse) : PatientInfoApiState()
    data class Error(val message: String) : PatientInfoApiState()
}

// 주석 처리 - PatientInfo 확인 후 사용
//sealed class PatientTypeApiState {
//    object Idle : PatientTypeApiState()
//    object Loading : PatientTypeApiState()
//    data class Success(val patientTypeResponse: PatientTypeResponse) : PatientTypeApiState()
//    data class Error(val message: String) : PatientTypeApiState()
//}
//
//sealed class PatientEvaApiState {
//    object Idle : PatientEvaApiState()
//    object Loading : PatientEvaApiState()
//    data class Success(val patientEvaResponse: PatientEvaResponse) : PatientEvaApiState()
//    data class Error(val message: String) : PatientEvaApiState()
//}
//
//sealed class FirstAidApiState {
//    object Idle : FirstAidApiState()
//    object Loading : FirstAidApiState()
//    data class Success(val firstAidResponse: FirstAidResponse) : FirstAidApiState()
//    data class Error(val message: String) : FirstAidApiState()
//}