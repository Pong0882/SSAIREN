// LogViewModel.kt
package com.example.ssairen_app.ui.screens.emergencyact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ActivityLogData(
    // ✅ 0. 환자정보 (PatientInfo)
    val reporterPhone: String = "",
    val reportMethod: String = "일반전화",
    val patientName: String = "",
    val patientGender: String = "남성",
    val birthYear: String = "",
    val birthMonth: String = "",
    val birthDay: String = "",
    val patientAge: String = "",
    val patientAddress: String = "",
    val guardianName: String = "",
    val guardianRelation: String = "",
    val guardianPhone: String = "",

    // ✅ 1. 환자평가 (PatientEva)
    val consciousness: String = "",
    val breathing: String = "",
    val pulse: String = "",
    val bloodPressure: String = "",
    val temperature: String = "",

    // ✅ 2. 환자이송 (PatientTransport)
    val transportDestination: String = "",
    val transportTime: String = "",
    val transportMethod: String = "",

    // ✅ 3. 구급출동 (DispatchSection)
    val dispatchTime: String = "",
    val arrivalTime: String = "",
    val departureTime: String = "",
    val sceneLocation: String = "",

    // ✅ 4. 환자발생유형 (PatienType)
    val incidentType: String = "",
    val incidentLocation: String = "",
    val incidentCause: String = "",

    // ✅ 5. 응급처치 (FirstAid)
    val treatmentDetails: String = "",
    val medication: String = "",
    val cprPerformed: String = "",

    // ✅ 6. 의료지도 (MedicalGuidance)
    val medicalGuidance: String = "",
    val guidanceDoctor: String = "",
    val guidanceTime: String = "",

    // ✅ 7. 세부사항표 (ReportDetail)
    val detailedSituation: String = "",
    val specialNotes: String = "",
    val crewMembers: String = ""
)

class LogViewModel : ViewModel() {

    private val _activityLogData = MutableStateFlow(ActivityLogData())
    val activityLogData: StateFlow<ActivityLogData> = _activityLogData.asStateFlow()

    private val _lastSavedTime = MutableStateFlow("")
    val lastSavedTime: StateFlow<String> = _lastSavedTime.asStateFlow()

    /**
     * ✅ 0. 환자정보 (PatientInfo) 업데이트
     */
    fun updatePatientInfo(
        reporterPhone: String,
        reportMethod: String,
        patientName: String,
        patientGender: String,
        birthYear: String,
        birthMonth: String,
        birthDay: String,
        patientAge: String,
        patientAddress: String,
        guardianName: String,
        guardianRelation: String,
        guardianPhone: String
    ) {
        _activityLogData.value = _activityLogData.value.copy(
            reporterPhone = reporterPhone,
            reportMethod = reportMethod,
            patientName = patientName,
            patientGender = patientGender,
            birthYear = birthYear,
            birthMonth = birthMonth,
            birthDay = birthDay,
            patientAge = patientAge,
            patientAddress = patientAddress,
            guardianName = guardianName,
            guardianRelation = guardianRelation,
            guardianPhone = guardianPhone
        )
        saveToLocal()
    }

    /**
     * ✅ 1. 환자평가 (PatientEva) 업데이트
     */
    fun updatePatientEva(
        consciousness: String,
        breathing: String,
        pulse: String,
        bloodPressure: String,
        temperature: String
    ) {
        _activityLogData.value = _activityLogData.value.copy(
            consciousness = consciousness,
            breathing = breathing,
            pulse = pulse,
            bloodPressure = bloodPressure,
            temperature = temperature
        )
        saveToLocal()
    }

    /**
     * ✅ 2. 환자이송 (PatientTransport) 업데이트
     */
    fun updatePatientTransport(
        destination: String,
        time: String,
        method: String
    ) {
        _activityLogData.value = _activityLogData.value.copy(
            transportDestination = destination,
            transportTime = time,
            transportMethod = method
        )
        saveToLocal()
    }

    /**
     * ✅ 3. 구급출동 (DispatchSection) 업데이트
     */
    fun updateDispatch(
        dispatchTime: String,
        arrivalTime: String,
        departureTime: String,
        sceneLocation: String
    ) {
        _activityLogData.value = _activityLogData.value.copy(
            dispatchTime = dispatchTime,
            arrivalTime = arrivalTime,
            departureTime = departureTime,
            sceneLocation = sceneLocation
        )
        saveToLocal()
    }

    /**
     * ✅ 4. 환자발생유형 (PatienType) 업데이트
     */
    fun updatePatienType(
        incidentType: String,
        incidentLocation: String,
        incidentCause: String
    ) {
        _activityLogData.value = _activityLogData.value.copy(
            incidentType = incidentType,
            incidentLocation = incidentLocation,
            incidentCause = incidentCause
        )
        saveToLocal()
    }

    /**
     * ✅ 5. 응급처치 (FirstAid) 업데이트
     */
    fun updateFirstAid(
        treatmentDetails: String,
        medication: String,
        cprPerformed: String
    ) {
        _activityLogData.value = _activityLogData.value.copy(
            treatmentDetails = treatmentDetails,
            medication = medication,
            cprPerformed = cprPerformed
        )
        saveToLocal()
    }

    /**
     * ✅ 6. 의료지도 (MedicalGuidance) 업데이트
     */
    fun updateMedicalGuidance(
        guidance: String,
        doctor: String,
        time: String
    ) {
        _activityLogData.value = _activityLogData.value.copy(
            medicalGuidance = guidance,
            guidanceDoctor = doctor,
            guidanceTime = time
        )
        saveToLocal()
    }

    /**
     * ✅ 7. 세부사항표 (ReportDetail) 업데이트
     */
    fun updateReportDetail(
        situation: String,
        notes: String,
        crewMembers: String
    ) {
        _activityLogData.value = _activityLogData.value.copy(
            detailedSituation = situation,
            specialNotes = notes,
            crewMembers = crewMembers
        )
        saveToLocal()
    }

    /**
     * 로컬에 자동 저장
     */
    private fun saveToLocal() {
        viewModelScope.launch {
            _lastSavedTime.value = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
            ).format(java.util.Date())

            // TODO: SharedPreferences나 Room DB에 저장
            println("📝 자동 저장됨: ${_lastSavedTime.value}")
            println("💾 저장된 데이터: ${_activityLogData.value}")
        }
    }

    /**
     * 최종 제출 - DB로 전송 (주석 처리)
     */
    fun submitToDatabase() {
        viewModelScope.launch {
            // TODO: API 연결 시 주석 해제
            /*
            try {
                val response = apiService.submitActivityLog(_activityLogData.value)
                if (response.isSuccessful) {
                    println("✅ DB 전송 성공")
                } else {
                    println("❌ DB 전송 실패: ${response.errorBody()}")
                }
            } catch (e: Exception) {
                println("❌ 네트워크 에러: ${e.message}")
            }
            */

            println("🚀 [DB 전송 준비 완료] 데이터: ${_activityLogData.value}")
        }
    }

    /**
     * 데이터 초기화
     */
    fun clearData() {
        _activityLogData.value = ActivityLogData()
        _lastSavedTime.value = ""
    }
}