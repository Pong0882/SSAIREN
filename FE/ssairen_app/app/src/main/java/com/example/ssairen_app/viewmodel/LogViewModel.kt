// LogViewModel.kt
package com.example.ssairen_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ==========================================
// ✅ 각 화면별 데이터 클래스로 분리
// ==========================================

// 0. 환자정보
data class PatientInfoData(
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
    val guardianPhone: String = ""
)

// 1. 구급출동
data class DispatchData(
    val dispatchTime: String = "",
    val arrivalTime: String = "",
    val departureTime: String = "",
    val sceneLocation: String = ""
)

// 2. 환자발생유형
data class PatienTypeData(
    // 병력 유무
    val hasMedicalHistory: String = "있음", // 있음/없음/미상
    val medicalHistoryList: Set<String> = setOf(), // 고혈압, 당뇨, 뇌혈관질환 등 (복수선택)

    // 환자 발생 유형
    val mainType: String = "", // 질병/질병 외/기타

    // 질병외 관련
    val crimeOption: String = "", // 경찰통보/경찰입회/긴급이송/관련기관 통보
    val subType: String = "", // 교통사고/그 외 외상/비외상성 손상
    val accidentVictimType: String = "", // 운전자/동승자/보행자/자전거/오토바이 등

    // 기타 관련
    val etcType: String = "" // 자연재해/임신분만/신생아/단순구조/기타
)

// 3. 환자평가
data class PatientEvaData(
    val patientLevel: String = "LEVEL 1",

    // 의식 상태 1차
    val consciousness1stAlert: Boolean = false,
    val consciousness1stVerbal: Boolean = false,
    val consciousness1stPainful: Boolean = false,
    val consciousness1stUnresponsive: Boolean = false,

    // 의식 상태 2차
    val consciousness2ndAlert: Boolean = false,
    val consciousness2ndVerbal: Boolean = false,
    val consciousness2ndPainful: Boolean = false,
    val consciousness2ndUnresponsive: Boolean = false,

    // 동공반응 좌
    val leftPupilNormal: Boolean = false,
    val leftPupilSlow: Boolean = false,
    val leftPupilReactive: Boolean = false,
    val leftPupilNonReactive: Boolean = false,

    // 동공반응 우
    val rightPupilNormal: Boolean = false,
    val rightPupilSlow: Boolean = false,
    val rightPupilReactive: Boolean = false,
    val rightPupilNonReactive: Boolean = false,

    // 활력 징후 좌
    val leftTime: String = "",
    val leftPulse: String = "",
    val leftBloodPressure: String = "",
    val leftTemperature: String = "",
    val leftOxygenSaturation: String = "",
    val leftRespiratoryRate: String = "",
    val leftBloodSugar: String = "",

    // 활력 징후 우
    val rightTime: String = "",
    val rightPulse: String = "",
    val rightBloodPressure: String = "",
    val rightTemperature: String = "",
    val rightOxygenSaturation: String = "",
    val rightRespiratoryRate: String = "",
    val rightBloodSugar: String = ""
)

// 4. 응급처치
data class FirstAidData(
    // 기도 확보
    val airwayJawThrust: Boolean = false,
    val airwayHeadTilt: Boolean = false,
    val airwayNPA: Boolean = false,
    val airwayOPA: Boolean = false,
    val airwayIntubation: Boolean = false,
    val airwaySupraglottic: Boolean = false,

    // 산소 투여
    val oxygenMask: Boolean = false,
    val oxygenNasal: Boolean = false,
    val oxygenBVM: Boolean = false,
    val oxygenVentilator: Boolean = false,
    val oxygenSuction: Boolean = false,

    // CPR
    val cprPerformed: Boolean = false,
    val cprManual: Boolean = false,
    val cprDNR: Boolean = false,
    val cprTermination: Boolean = false,

    // AED
    val aedShock: Boolean = false,
    val aedMonitoring: Boolean = false,
    val aedApplicationOnly: Boolean = false,

    // 처치
    val treatmentOxygenSaturation: Boolean = false,
    val treatmentShockPrevention: Boolean = false,
    val treatmentInjection: Boolean = false,

    // 고정
    val immobilizationSpinal: Boolean = false,
    val immobilizationCSpine: Boolean = false,
    val immobilizationSplint: Boolean = false,
    val immobilizationOther: Boolean = false,

    // 상처 처치
    val woundDressing: Boolean = false,
    val woundBandage: Boolean = false,
    val woundHemostasis: Boolean = false,
    val woundParalysis: Boolean = false
)

// 5. 의료지도
data class MedicalGuidanceData(
    val medicalGuidance: String = "",
    val guidanceDoctor: String = "",
    val guidanceTime: String = ""
)

// 6. 환자이송
data class PatientTransportData(
    val transportDestination: String = "",
    val transportTime: String = "",
    val transportMethod: String = ""
)

// 7. 세부사항표
data class ReportDetailData(
    val detailedSituation: String = "",
    val specialNotes: String = "",
    val crewMembers: String = ""
)

// ==========================================
// ✅ 전체 구급활동일지 데이터
// ==========================================
data class ActivityLogData(
    val patientInfo: PatientInfoData = PatientInfoData(),
    val dispatch: DispatchData = DispatchData(),
    val patienType: PatienTypeData = PatienTypeData(),
    val patientEva: PatientEvaData = PatientEvaData(),
    val firstAid: FirstAidData = FirstAidData(),
    val medicalGuidance: MedicalGuidanceData = MedicalGuidanceData(),
    val patientTransport: PatientTransportData = PatientTransportData(),
    val reportDetail: ReportDetailData = ReportDetailData()
)

// ==========================================
// ✅ ViewModel
// ==========================================
class LogViewModel : ViewModel() {

    private val _activityLogData = MutableStateFlow(ActivityLogData())
    val activityLogData: StateFlow<ActivityLogData> = _activityLogData.asStateFlow()

    private val _lastSavedTime = MutableStateFlow("")
    val lastSavedTime: StateFlow<String> = _lastSavedTime.asStateFlow()

    /**
     * ✅ 0. 환자정보 업데이트
     */
    fun updatePatientInfo(data: PatientInfoData) {
        _activityLogData.value = _activityLogData.value.copy(
            patientInfo = data
        )
        saveToLocal()
    }

    /**
     * ✅ 1. 구급출동 업데이트
     */
    fun updateDispatch(data: DispatchData) {
        _activityLogData.value = _activityLogData.value.copy(
            dispatch = data
        )
        saveToLocal()
    }

    /**
     * ✅ 2. 환자발생유형 업데이트
     */
    fun updatePatienType(data: PatienTypeData) {
        _activityLogData.value = _activityLogData.value.copy(
            patienType = data
        )
        saveToLocal()
    }

    /**
     * ✅ 3. 환자평가 업데이트
     */
    fun updatePatientEva(data: PatientEvaData) {
        _activityLogData.value = _activityLogData.value.copy(
            patientEva = data
        )
        saveToLocal()
    }

    /**
     * ✅ 4. 응급처치 업데이트
     */
    fun updateFirstAid(data: FirstAidData) {
        _activityLogData.value = _activityLogData.value.copy(
            firstAid = data
        )
        saveToLocal()
    }

    /**
     * ✅ 5. 의료지도 업데이트
     */
    fun updateMedicalGuidance(data: MedicalGuidanceData) {
        _activityLogData.value = _activityLogData.value.copy(
            medicalGuidance = data
        )
        saveToLocal()
    }

    /**
     * ✅ 6. 환자이송 업데이트
     */
    fun updatePatientTransport(data: PatientTransportData) {
        _activityLogData.value = _activityLogData.value.copy(
            patientTransport = data
        )
        saveToLocal()
    }

    /**
     * ✅ 7. 세부사항표 업데이트
     */
    fun updateReportDetail(data: ReportDetailData) {
        _activityLogData.value = _activityLogData.value.copy(
            reportDetail = data
        )
        saveToLocal()
    }

    /**
     * 로컬에 자동 저장
     */
    private fun saveToLocal() {
        viewModelScope.launch {
            _lastSavedTime.value = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())

            println("📝 자동 저장됨: ${_lastSavedTime.value}")
            println("💾 저장된 데이터: ${_activityLogData.value}")
        }
    }

    /**
     * 최종 제출 - DB로 전송
     */
    fun submitToDatabase() {
        viewModelScope.launch {
            // TODO: API 연결 시 주석 해제
            println("🚀 [DB 전송 준비 완료] 데이터: ${_activityLogData.value}")
        }
    }

    /**
     * ✅ 탭 변경 시 백엔드에 현재 섹션 저장
     * @param tabIndex 현재 탭 인덱스 (0: 환자정보, 1: 구급출동, ...)
     */
    fun saveToBackend(tabIndex: Int) {
        viewModelScope.launch {
            val currentData = _activityLogData.value

            when (tabIndex) {
                0 -> {
                    // 환자정보 저장
                    println("💾 [백엔드 저장] 환자정보: ${currentData.patientInfo}")
                    // TODO: API 연결
                    // repository.updatePatientInfo(emergencyReportId, currentData.patientInfo)
                }
                1 -> {
                    // 구급출동 저장
                    println("💾 [백엔드 저장] 구급출동: ${currentData.dispatch}")
                    // TODO: API 연결
                }
                2 -> {
                    // 환자발생유형 저장
                    println("💾 [백엔드 저장] 환자발생유형: ${currentData.patienType}")
                    // TODO: API 연결
                }
                3 -> {
                    // 환자평가 저장
                    println("💾 [백엔드 저장] 환자평가: ${currentData.patientEva}")
                    // TODO: API 연결
                }
                4 -> {
                    // 응급처치 저장
                    println("💾 [백엔드 저장] 응급처치: ${currentData.firstAid}")
                    // TODO: API 연결
                }
                5 -> {
                    // 의료지도 저장
                    println("💾 [백엔드 저장] 의료지도: ${currentData.medicalGuidance}")
                    // TODO: API 연결
                }
                6 -> {
                    // 환자이송 저장
                    println("💾 [백엔드 저장] 환자이송: ${currentData.patientTransport}")
                    // TODO: API 연결
                }
                7 -> {
                    // 세부사항표 저장
                    println("💾 [백엔드 저장] 세부사항표: ${currentData.reportDetail}")
                    // TODO: API 연결
                }
            }

            // 저장 시간 업데이트
            _lastSavedTime.value = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(Date())

            println("✅ 백엔드 저장 완료: ${_lastSavedTime.value}")
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