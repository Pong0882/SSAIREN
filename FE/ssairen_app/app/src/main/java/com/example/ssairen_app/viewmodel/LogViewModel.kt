// LogViewModel.kt
package com.example.ssairen_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ssairen_app.data.local.AuthManager
import com.example.ssairen_app.data.repository.ReportRepository
import com.example.ssairen_app.data.model.request.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log

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
    val reportDatetime: String = "",          // 신고 일시 (ISO 8601)
    val departureTime: String = "",           // 출동 시각 (HH:mm)
    val arrivalSceneTime: String = "",        // 현장 도착 (HH:mm)
    val departureSceneTime: String = "",      // 현장 출발 (HH:mm)
    val contactTime: String = "",             // 환자 접촉 (HH:mm)
    val arrivalHospitalTime: String = "",     // 병원 도착 (HH:mm)
    val distanceKm: Double = 0.0,             // 거리 (km)
    val returnTime: String = "",              // 귀소 시간 (HH:mm)
    val dispatchType: String = "정상",         // 출동 유형
    val sceneLocationName: String = "집",      // 환자 발생 장소
    val sceneLocationValue: String? = null,   // 기타 입력값
    val painSymptoms: Set<String> = setOf(),  // 통증 증상들
    val traumaSymptoms: Set<String> = setOf(),// 외상 증상들
    val otherSymptoms: Set<String> = setOf(), // 그 외 증상들
    val otherPainValue: String? = null,       // "그 밖의 통증" 실제 입력값
    val otherSymptomValue: String? = null     // "기타" 증상 실제 입력값
)

// 의료지도
data class MedicalGuidanceData(
    val contactStatus: String = "",                         // 의료지도 연결 여부: 연결 | 미연결
    val requestTime: String = "",                           // 요청 시각 (HH:mm)
    val requestMethod: String = "",                         // 요청 방법
    val requestMethodValue: String? = null,                 // 기타 요청 방법 입력값
    val guidanceAgency: String = "",                        // 의료지도 기관: 소방 | 병원 | 기타
    val guidanceAgencyValue: String? = null,                // 기타 기관 입력값
    val guidanceDoctor: String = "",                        // 의료지도 의사 성명
    val emergencyTreatment: Set<String> = setOf(),          // 응급처치 (복수 선택)
    val emergencyTreatmentOtherValue: String? = null,       // 응급처치 기타 입력값
    val medication: Set<String> = setOf(),                  // 약물투여 (복수 선택)
    val medicationOtherValue: String? = null,               // 약물투여 기타 입력값
    val hospitalRequest: Boolean = false,                   // 병원선정
    val patientEvaluation: Boolean = false,                 // 환자평가
    val cprTransfer: Boolean = false,                       // CPR유보중단
    val transferRefusal: Boolean = false,                   // 이송거절
    val transferRejection: Boolean = false                  // 이송거부
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

// 6. 환자이송
data class PatientTransportData(
    // 1차 이송
    val firstHospitalName: String = "",
    val firstRegionType: String = "관할",           // 관할 | 타시·도
    val firstArrivalTime: String = "",              // HH:mm
    val firstDistanceKm: Double = 0.0,
    val firstSelectedBy: String = "",               // 의료기관 선정자
    val firstBedShortageReasons: Set<String> = setOf(), // 병상부족 사유 (복수선택)
    val firstOtherReasons: Set<String> = setOf(),   // 기타 재이송 사유 (복수선택)
    val firstReceiver: String = "",                 // 환자 인수자
    val firstReceiverSignature: List<androidx.compose.ui.geometry.Offset> = emptyList(), // 1차 인수자 서명

    // 2차 이송 (선택)
    val secondHospitalName: String = "",
    val secondRegionType: String = "관할",
    val secondArrivalTime: String = "",
    val secondDistanceKm: Double = 0.0,
    val secondSelectedBy: String = "",
    val secondBedShortageReasons: Set<String> = setOf(),
    val secondOtherReasons: Set<String> = setOf(),
    val secondReceiver: String = "",
    val secondReceiverSignature: List<androidx.compose.ui.geometry.Offset> = emptyList() // 2차 인수자 서명
)

// 7. 세부사항표
data class ReportDetailData(
    // 의사
    val doctorAffiliation: String = "",
    val doctorName: String = "",
    val doctorSignature: List<androidx.compose.ui.geometry.Offset> = emptyList(),

    // 구급대원 1
    val paramedic1Grade: String = "",
    val paramedic1Rank: String = "",
    val paramedic1Name: String = "",
    val paramedic1Signature: List<androidx.compose.ui.geometry.Offset> = emptyList(),

    // 구급대원 2
    val paramedic2Grade: String = "",
    val paramedic2Rank: String = "",
    val paramedic2Name: String = "",
    val paramedic2Signature: List<androidx.compose.ui.geometry.Offset> = emptyList(),

    // 운전요원
    val driverGrade: String = "",
    val driverRank: String = "",
    val driverName: String = "",
    val driverSignature: List<androidx.compose.ui.geometry.Offset> = emptyList(),

    // 기타 인원
    val otherGrade: String = "",
    val otherRank: String = "",
    val otherName: String = "",
    val otherSignature: List<androidx.compose.ui.geometry.Offset> = emptyList(),

    // 장애요인 (복수 선택 가능)
    val obstacles: Set<String> = setOf(),
    val obstacleOtherValue: String? = null
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
// ✅ 저장 상태 (UI에 피드백용)
// ==========================================
sealed class SaveState {
    object Idle : SaveState()
    object Saving : SaveState()
    data class Success(val message: String) : SaveState()
    data class Error(val message: String) : SaveState()
}

// ==========================================
// ✅ ViewModel
// ==========================================
class LogViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "LogViewModel"
    }

    // Repository 초기화
    private val repository = ReportRepository(AuthManager(application))

    private val _activityLogData = MutableStateFlow(ActivityLogData())
    val activityLogData: StateFlow<ActivityLogData> = _activityLogData.asStateFlow()

    private val _lastSavedTime = MutableStateFlow("")
    val lastSavedTime: StateFlow<String> = _lastSavedTime.asStateFlow()

    // 저장 상태 추가
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState.asStateFlow()

    // 현재 emergencyReportId (ActivityViewModel에서 설정)
    private var currentEmergencyReportId: Int = 0

    /**
     * emergencyReportId 설정
     */
    fun setEmergencyReportId(reportId: Int) {
        currentEmergencyReportId = reportId
        Log.d(TAG, "📝 LogViewModel에 보고서 ID 설정: $reportId")
    }

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
     * ✅ 의료지도 업데이트
     */
    fun updateMedicalGuidance(data: MedicalGuidanceData) {
        _activityLogData.value = _activityLogData.value.copy(
            medicalGuidance = data
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

            Log.d(TAG, "📝 로컬 자동 저장됨: ${_lastSavedTime.value}")
        }
    }

    /**
     * ✅ 탭 변경 시 백엔드에 현재 섹션 저장
     * @param tabIndex 현재 탭 인덱스 (0: 환자정보, 2: 환자발생유형, 3: 환자평가, 4: 응급처치)
     */
    fun saveToBackend(tabIndex: Int) {
        if (currentEmergencyReportId == 0) {
            Log.e(TAG, "❌ emergencyReportId가 설정되지 않았습니다")
            _saveState.value = SaveState.Error("보고서 ID가 설정되지 않았습니다")
            return
        }

        viewModelScope.launch {
            _saveState.value = SaveState.Saving
            val currentData = _activityLogData.value

            try {
                when (tabIndex) {
                    0 -> {
                        // 환자정보 저장
                        Log.d(TAG, "💾 [백엔드 저장] 환자정보 시작")
                        val request = convertToPatientInfoRequest(currentData.patientInfo)

                        repository.updatePatientInfo(currentEmergencyReportId, request)
                            .onSuccess { response ->
                                Log.d(TAG, "✅ 환자정보 저장 성공")
                                _saveState.value = SaveState.Success("환자정보 저장 완료")
                                updateSaveTime()
                            }
                            .onFailure { error ->
                                Log.e(TAG, "❌ 환자정보 저장 실패: ${error.message}")
                                _saveState.value = SaveState.Error(error.message ?: "저장 실패")
                            }
                    }

                    1 -> {
                        // 구급출동 저장
                        Log.d(TAG, "💾 [백엔드 저장] 구급출동 시작")
                        val request = convertToDispatchRequest(currentData.dispatch)

                        repository.updateDispatch(currentEmergencyReportId, request)
                            .onSuccess { response ->
                                Log.d(TAG, "✅ 구급출동 저장 성공")
                                _saveState.value = SaveState.Success("구급출동 저장 완료")
                                updateSaveTime()
                            }
                            .onFailure { error ->
                                Log.e(TAG, "❌ 구급출동 저장 실패: ${error.message}")
                                _saveState.value = SaveState.Error(error.message ?: "저장 실패")
                            }
                    }

                    2 -> {
                        // 환자발생유형 저장
                        Log.d(TAG, "💾 [백엔드 저장] 환자발생유형 시작")
                        val request = convertToPatientTypeRequest(currentData.patienType)

                        repository.updatePatientType(currentEmergencyReportId, request)
                            .onSuccess { response ->
                                Log.d(TAG, "✅ 환자발생유형 저장 성공")
                                _saveState.value = SaveState.Success("환자발생유형 저장 완료")
                                updateSaveTime()
                            }
                            .onFailure { error ->
                                Log.e(TAG, "❌ 환자발생유형 저장 실패: ${error.message}")
                                _saveState.value = SaveState.Error(error.message ?: "저장 실패")
                            }
                    }

                    3 -> {
                        // 환자평가 저장
                        Log.d(TAG, "💾 [백엔드 저장] 환자평가 시작")
                        val request = convertToPatientEvaRequest(currentData.patientEva)

                        repository.updatePatientEva(currentEmergencyReportId, request)
                            .onSuccess { response ->
                                Log.d(TAG, "✅ 환자평가 저장 성공")
                                _saveState.value = SaveState.Success("환자평가 저장 완료")
                                updateSaveTime()
                            }
                            .onFailure { error ->
                                Log.e(TAG, "❌ 환자평가 저장 실패: ${error.message}")
                                _saveState.value = SaveState.Error(error.message ?: "저장 실패")
                            }
                    }

                    4 -> {
                        // 응급처치 저장
                        Log.d(TAG, "💾 [백엔드 저장] 응급처치 시작")
                        val request = convertToFirstAidRequest(currentData.firstAid)

                        repository.updateFirstAid(currentEmergencyReportId, request)
                            .onSuccess { response ->
                                Log.d(TAG, "✅ 응급처치 저장 성공")
                                _saveState.value = SaveState.Success("응급처치 저장 완료")
                                updateSaveTime()
                            }
                            .onFailure { error ->
                                Log.e(TAG, "❌ 응급처치 저장 실패: ${error.message}")
                                _saveState.value = SaveState.Error(error.message ?: "저장 실패")
                            }
                    }

                    5 -> {
                        // 의료지도 저장
                        Log.d(TAG, "💾 [백엔드 저장] 의료지도 시작")
                        val request = convertToMedicalGuidanceRequest(currentData.medicalGuidance)

                        repository.updateMedicalGuidance(currentEmergencyReportId, request)
                            .onSuccess { response ->
                                Log.d(TAG, "✅ 의료지도 저장 성공")
                                _saveState.value = SaveState.Success("의료지도 저장 완료")
                                updateSaveTime()
                            }
                            .onFailure { error ->
                                Log.e(TAG, "❌ 의료지도 저장 실패: ${error.message}")
                                _saveState.value = SaveState.Error(error.message ?: "저장 실패")
                            }
                    }

                    6 -> {
                        // 환자이송 저장
                        Log.d(TAG, "💾 [백엔드 저장] 환자이송 시작")
                        val request = convertToTransportRequest(currentData.patientTransport)

                        repository.updateTransport(currentEmergencyReportId, request)
                            .onSuccess { response ->
                                Log.d(TAG, "✅ 환자이송 저장 성공")
                                _saveState.value = SaveState.Success("환자이송 저장 완료")
                                updateSaveTime()
                            }
                            .onFailure { error ->
                                Log.e(TAG, "❌ 환자이송 저장 실패: ${error.message}")
                                _saveState.value = SaveState.Error(error.message ?: "저장 실패")
                            }
                    }

                    else -> {
                        Log.d(TAG, "⚠️ 탭 $tabIndex 는 백엔드 저장이 구현되지 않았습니다")
                        _saveState.value = SaveState.Success("로컬 저장만 완료")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 백엔드 저장 중 예외 발생", e)
                _saveState.value = SaveState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    /**
     * 저장 시간 업데이트
     */
    private fun updateSaveTime() {
        _lastSavedTime.value = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()
        ).format(Date())
    }

    // ==========================================
    // ✅ 데이터 변환 함수들
    // ==========================================

    /**
     * PatientInfoData → PatientInfoRequest 변환
     */
    private fun convertToPatientInfoRequest(data: PatientInfoData): PatientInfoRequest {
        // 생년월일 조합 (YYYY-MM-DD 형식)
        val birthDate = if (data.birthYear.isNotEmpty() &&
            data.birthMonth.isNotEmpty() &&
            data.birthDay.isNotEmpty()) {
            "${data.birthYear}-${data.birthMonth.padStart(2, '0')}-${data.birthDay.padStart(2, '0')}"
        } else null

        // 나이 계산 (입력된 값 사용)
        val ageYears = data.patientAge.toIntOrNull()

        return PatientInfoRequest(
            data = PatientInfoRequestData(
                schemaVersion = 1,
                patientInfo = PatientInfoContent(
                    reporter = ReporterInfo(
                        phone = data.reporterPhone.ifEmpty { null },
                        reportMethod = data.reportMethod.ifEmpty { null }
                    ),
                    patient = PatientInfoDetail(
                        name = data.patientName.ifEmpty { null },
                        gender = data.patientGender.ifEmpty { null },
                        ageYears = ageYears,
                        birthDate = birthDate,
                        address = data.patientAddress.ifEmpty { null }
                    ),
                    guardian = GuardianInfo(
                        name = data.guardianName.ifEmpty { null },
                        relation = data.guardianRelation.ifEmpty { null },
                        phone = data.guardianPhone.ifEmpty { null }
                    ),
                    incidentLocation = IncidentLocation(
                        text = null // 구급출동 섹션에 있으므로 여기선 null
                    )
                )
            )
        )
    }

    /**
     * DispatchData → DispatchRequest 변환
     */
    private fun convertToDispatchRequest(data: DispatchData): DispatchRequest {
        // 현재 시간을 ISO 8601 형식으로 생성
        val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

        // 증상 데이터 변환
        val painSymptoms = data.painSymptoms.map { symptom ->
            SymptomItem(
                name = symptom,
                value = if (symptom == "그 밖의 통증") data.otherPainValue else null
            )
        }

        val traumaSymptoms = data.traumaSymptoms.map { symptom ->
            SymptomItem(name = symptom, value = null)
        }

        val otherSymptoms = data.otherSymptoms.map { symptom ->
            SymptomItem(
                name = symptom,
                value = if (symptom == "기타") data.otherSymptomValue else null
            )
        }

        return DispatchRequest(
            data = DispatchRequestData(
                dispatch = DispatchInfo(
                    reportDatetime = data.reportDatetime.ifEmpty { currentTime },
                    departureTime = data.departureTime.ifEmpty { "00:00" },
                    arrivalSceneTime = data.arrivalSceneTime.ifEmpty { "00:00" },
                    contactTime = data.contactTime.ifEmpty { "00:00" },
                    distanceKm = data.distanceKm,
                    departureSceneTime = data.departureSceneTime.ifEmpty { "00:00" },
                    arrivalHospitalTime = data.arrivalHospitalTime.ifEmpty { "00:00" },
                    returnTime = data.returnTime.ifEmpty { "00:00" },
                    dispatchType = data.dispatchType.ifEmpty { "정상" },
                    sceneLocation = SceneLocation(
                        name = data.sceneLocationName,
                        value = data.sceneLocationValue
                    ),
                    symptoms = Symptoms(
                        pain = painSymptoms,
                        trauma = traumaSymptoms,
                        otherSymptoms = otherSymptoms
                    ),
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
            )
        )
    }

    /**
     * MedicalGuidanceData → MedicalGuidanceRequest 변환
     */
    private fun convertToMedicalGuidanceRequest(data: MedicalGuidanceData): MedicalGuidanceRequest {
        // 현재 시간을 ISO 8601 형식으로 생성
        val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

        // 응급처치 리스트 변환
        val emergencyTreatmentList = data.emergencyTreatment.map { item ->
            com.example.ssairen_app.data.model.request.TreatmentItem(
                name = item,
                value = if (item == "기타") data.emergencyTreatmentOtherValue else null
            )
        }

        // 약물투여 리스트 변환
        val medicationList = data.medication.map { item ->
            com.example.ssairen_app.data.model.request.TreatmentItem(
                name = item,
                value = if (item == "기타") data.medicationOtherValue else null
            )
        }

        return MedicalGuidanceRequest(
            data = MedicalGuidanceRequestData(
                medicalGuidance = MedicalGuidanceInfo(
                    contactStatus = data.contactStatus.ifEmpty { "연결" },
                    requestTime = data.requestTime.ifEmpty { "00:00" },
                    requestMethod = com.example.ssairen_app.data.model.request.RequestMethod(
                        type = data.requestMethod,
                        value = if (data.requestMethod == "기타") data.requestMethodValue else null
                    ),
                    guidanceAgency = com.example.ssairen_app.data.model.request.GuidanceAgency(
                        type = data.guidanceAgency,
                        value = if (data.guidanceAgency == "기타") data.guidanceAgencyValue else null
                    ),
                    guidanceDoctor = com.example.ssairen_app.data.model.request.GuidanceDoctor(
                        name = data.guidanceDoctor
                    ),
                    guidanceContent = com.example.ssairen_app.data.model.request.GuidanceContent(
                        emergencyTreatment = emergencyTreatmentList,
                        medication = medicationList,
                        hospitalRequest = data.hospitalRequest,
                        patientEvaluation = data.patientEvaluation,
                        cprTransfer = data.cprTransfer,
                        transferRefusal = data.transferRefusal,
                        transferRejection = data.transferRejection,
                        notes = null
                    ),
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
            )
        )
    }

    /**
     * PatientTransportData → TransportRequest 변환
     */
    private fun convertToTransportRequest(data: PatientTransportData): com.example.ssairen_app.data.model.request.TransportRequest {
        val currentTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(Date())

        // 1차 이송 정보 (필수 - 빈 값이어도 항상 전송)
        val firstTransport = com.example.ssairen_app.data.model.request.TransportDetail(
            hospitalName = data.firstHospitalName,
            regionType = data.firstRegionType,
            arrivalTime = data.firstArrivalTime,
            distanceKm = data.firstDistanceKm,
            selectedBy = data.firstSelectedBy,
            retransportReason = buildList {
                // 병상부족 사유
                if (data.firstBedShortageReasons.isNotEmpty()) {
                    add(com.example.ssairen_app.data.model.request.RetransportReason(
                        type = "병상부족",
                        name = data.firstBedShortageReasons.toList(),
                        isCustom = false
                    ))
                }
                // 기타 사유
                data.firstOtherReasons.forEach { reason ->
                    add(com.example.ssairen_app.data.model.request.RetransportReason(
                        type = reason,
                        name = null,
                        isCustom = reason == "기타"
                    ))
                }
            },
            receiver = data.firstReceiver,
            receiverSign = null  // 서명 기능은 추후 구현
        )

        // 2차 이송 정보 (선택 - 데이터가 있을 때만)
        val secondTransport = if (data.secondHospitalName.isNotEmpty()) {
            com.example.ssairen_app.data.model.request.TransportDetail(
                hospitalName = data.secondHospitalName,
                regionType = data.secondRegionType,
                arrivalTime = data.secondArrivalTime,
                distanceKm = data.secondDistanceKm,
                selectedBy = data.secondSelectedBy,
                retransportReason = buildList {
                    // 병상부족 사유
                    if (data.secondBedShortageReasons.isNotEmpty()) {
                        add(com.example.ssairen_app.data.model.request.RetransportReason(
                            type = "병상부족",
                            name = data.secondBedShortageReasons.toList(),
                            isCustom = false
                        ))
                    }
                    // 기타 사유
                    data.secondOtherReasons.forEach { reason ->
                        add(com.example.ssairen_app.data.model.request.RetransportReason(
                            type = reason,
                            name = null,
                            isCustom = reason == "기타"
                        ))
                    }
                },
                receiver = data.secondReceiver,
                receiverSign = null  // 서명 기능은 추후 구현
            )
        } else null

        return com.example.ssairen_app.data.model.request.TransportRequest(
            data = com.example.ssairen_app.data.model.request.TransportRequestData(
                transport = com.example.ssairen_app.data.model.request.TransportInfo(
                    firstTransport = firstTransport,
                    secondTransport = secondTransport,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
            )
        )
    }

    /**
     * PatienTypeData → PatientTypeRequest 변환
     */
    private fun convertToPatientTypeRequest(data: PatienTypeData): PatientTypeRequest {
        // 병력 리스트를 MedicalItem 리스트로 변환
        val medicalItems = data.medicalHistoryList.map {
            MedicalItem(name = it)
        }

        return PatientTypeRequest(
            data = PatientTypeRequestData(
                schemaVersion = 1,
                incidentType = IncidentTypeContent(
                    medicalHistory = MedicalHistory(
                        status = data.hasMedicalHistory.ifEmpty { null },
                        items = if (medicalItems.isNotEmpty()) medicalItems else null
                    ),
                    category = data.mainType.ifEmpty { null },
                    subCategory_traffic = if (data.subType == "교통사고") {
                        SubCategoryTraffic(
                            name = data.accidentVictimType.ifEmpty { null }
                        )
                    } else null,
                    subCategory_injury = if (data.subType == "그 외 외상") {
                        SubCategoryInjury(
                            name = data.subType
                        )
                    } else null,
                    subCategory_nonTrauma = if (data.subType == "비외상성 손상") {
                        SubCategoryNonTrauma(
                            name = data.subType,
                            value = null
                        )
                    } else null,
                    category_other = if (data.mainType == "기타") data.etcType.ifEmpty { null } else null,
                    subCategory_other = if (data.mainType == "기타") {
                        SubCategoryOther(
                            name = data.etcType.ifEmpty { null }
                        )
                    } else null,
                    legalSuspicion = if (data.crimeOption.isNotEmpty()) {
                        LegalSuspicion(name = data.crimeOption)
                    } else null
                )
            )
        )
    }

    /**
     * PatientEvaData → PatientEvaRequest 변환
     */
    private fun convertToPatientEvaRequest(data: PatientEvaData): PatientEvaRequest {
        // 의식 상태 1차
        val consciousness1stState = when {
            data.consciousness1stAlert -> "Alert"
            data.consciousness1stVerbal -> "Verbal"
            data.consciousness1stPainful -> "Painful"
            data.consciousness1stUnresponsive -> "Unresponsive"
            else -> null
        }

        // 의식 상태 2차
        val consciousness2ndState = when {
            data.consciousness2ndAlert -> "Alert"
            data.consciousness2ndVerbal -> "Verbal"
            data.consciousness2ndPainful -> "Painful"
            data.consciousness2ndUnresponsive -> "Unresponsive"
            else -> null
        }

        // 동공반응 좌
        val leftPupilStatus = when {
            data.leftPupilNormal -> "Normal"
            data.leftPupilSlow -> "Slow"
            else -> null
        }
        val leftPupilReaction = when {
            data.leftPupilReactive -> "Reactive"
            data.leftPupilNonReactive -> "Non-reactive"
            else -> null
        }

        // 동공반응 우
        val rightPupilStatus = when {
            data.rightPupilNormal -> "Normal"
            data.rightPupilSlow -> "Slow"
            else -> null
        }
        val rightPupilReaction = when {
            data.rightPupilReactive -> "Reactive"
            data.rightPupilNonReactive -> "Non-reactive"
            else -> null
        }

        return PatientEvaRequest(
            data = PatientEvaRequestData(
                schemaVersion = 1,
                assessment = AssessmentContent(
                    consciousness = ConsciousnessData(
                        first = ConsciousnessState(
                            time = null, // 시간은 UI에서 따로 입력받아야 함
                            state = consciousness1stState
                        ),
                        second = ConsciousnessState(
                            time = null,
                            state = consciousness2ndState
                        )
                    ),
                    pupilReaction = PupilReactionData(
                        left = PupilState(
                            status = leftPupilStatus,
                            reaction = leftPupilReaction
                        ),
                        right = PupilState(
                            status = rightPupilStatus,
                            reaction = rightPupilReaction
                        )
                    ),
                    vitalSigns = VitalSignsData(
                        first = VitalSign(
                            time = data.leftTime.ifEmpty { null },
                            bloodPressure = data.leftBloodPressure.ifEmpty { null },
                            pulse = data.leftPulse.toIntOrNull(),
                            respiration = data.leftRespiratoryRate.toIntOrNull(),
                            temperature = data.leftTemperature.toDoubleOrNull(),
                            spo2 = data.leftOxygenSaturation.toIntOrNull(),
                            bloodSugar = data.leftBloodSugar.toIntOrNull()
                        ),
                        second = VitalSign(
                            time = data.rightTime.ifEmpty { null },
                            bloodPressure = data.rightBloodPressure.ifEmpty { null },
                            pulse = data.rightPulse.toIntOrNull(),
                            respiration = data.rightRespiratoryRate.toIntOrNull(),
                            temperature = data.rightTemperature.toDoubleOrNull(),
                            spo2 = data.rightOxygenSaturation.toIntOrNull(),
                            bloodSugar = data.rightBloodSugar.toIntOrNull()
                        )
                    ),
                    patientLevel = data.patientLevel.ifEmpty { null },
                    notes = null // 주소증, 발병시각, 비고는 별도 입력 필요
                )
            )
        )
    }

    /**
     * FirstAidData → FirstAidRequest 변환
     */
    private fun convertToFirstAidRequest(data: FirstAidData): FirstAidRequest {
        // 기도 관리 방법 리스트 생성
        val airwayMethods = mutableListOf<String>()
        if (data.airwayJawThrust) airwayMethods.add("Jaw Thrust")
        if (data.airwayHeadTilt) airwayMethods.add("Head Tilt")
        if (data.airwayNPA) airwayMethods.add("NPA")
        if (data.airwayOPA) airwayMethods.add("OPA")
        if (data.airwayIntubation) airwayMethods.add("기관내삽관")
        if (data.airwaySupraglottic) airwayMethods.add("성문상기도기")

        // CPR 상태 결정
        val cprStatus = when {
            data.cprPerformed && data.cprManual -> "수행"
            data.cprDNR -> "DNR"
            data.cprTermination -> "중단"
            else -> null
        }

        // AED 상태 결정
        val aedType = when {
            data.aedShock -> "제세동"
            data.aedMonitoring -> "모니터링"
            data.aedApplicationOnly -> "부착만"
            else -> null
        }

        return FirstAidRequest(
            data = FirstAidRequestData(
                schemaVersion = 1,
                treatment = TreatmentContent(
                    airwayManagement = if (airwayMethods.isNotEmpty()) {
                        AirwayManagement(methods = airwayMethods)
                    } else null,
                    oxygenTherapy = null, // 산소 투여량은 별도 입력 필요
                    cpr = cprStatus,
                    ecg = null, // ECG는 별도 입력 필요
                    aed = if (aedType != null) {
                        AedData(type = aedType)
                    } else null,
                    notes = null,
                    circulation = null, // 순환 처치는 별도 입력 필요
                    drug = null, // 약물은 별도 입력 필요
                    fixed = if (data.immobilizationSpinal ||
                        data.immobilizationCSpine ||
                        data.immobilizationSplint ||
                        data.immobilizationOther) {
                        "고정 수행"
                    } else null,
                    woundCare = if (data.woundDressing ||
                        data.woundBandage ||
                        data.woundHemostasis ||
                        data.woundParalysis) {
                        "상처 처치 수행"
                    } else null,
                    deliverytime = null,
                    temperature = null
                )
            )
        )
    }

    /**
     * 최종 제출 - DB로 전송 (모든 섹션 저장)
     */
    fun submitToDatabase() {
        viewModelScope.launch {
            Log.d(TAG, "🚀 [전체 데이터 DB 전송 시작]")

            // 모든 섹션 순차적으로 저장
            saveToBackend(0) // 환자정보
            saveToBackend(2) // 환자발생유형
            saveToBackend(3) // 환자평가
            saveToBackend(4) // 응급처치

            Log.d(TAG, "✅ 전체 데이터 전송 완료")
        }
    }

    /**
     * 세부사항 섹션 저장 (탭 이탈 시 자동 호출)
     */
    suspend fun saveDetailReportSection(activityViewModel: ActivityViewModel): Result<Unit> {
        return try {
            val detailData = _activityLogData.value.reportDetail
            val emergencyReportId = currentEmergencyReportId

            if (emergencyReportId == 0) {
                Log.e(TAG, "❌ emergencyReportId가 0입니다. 저장 불가")
                return Result.failure(Exception("emergencyReportId가 없습니다"))
            }

            Log.d(TAG, "🔄 세부사항 섹션 저장 시작 - emergencyReportId: $emergencyReportId")

            // 의사 정보 변환 (이름이 있을 경우에만 포함)
            val doctor = if (detailData.doctorName.isNotEmpty()) {
                ParamedicMember(
                    affiliation = detailData.doctorAffiliation.takeIf { it.isNotEmpty() },
                    name = detailData.doctorName,
                    grade = null,
                    rank = null,
                    signature = if (detailData.doctorSignature.isNotEmpty()) "" else null
                )
            } else null

            // 구급대원1 정보 변환
            val paramedic1 = if (detailData.paramedic1Name.isNotEmpty()) {
                ParamedicMember(
                    affiliation = null,
                    name = detailData.paramedic1Name,
                    grade = detailData.paramedic1Grade.takeIf { it.isNotEmpty() },
                    rank = detailData.paramedic1Rank.takeIf { it.isNotEmpty() },
                    signature = if (detailData.paramedic1Signature.isNotEmpty()) "" else null
                )
            } else null

            // 구급대원2 정보 변환
            val paramedic2 = if (detailData.paramedic2Name.isNotEmpty()) {
                ParamedicMember(
                    affiliation = null,
                    name = detailData.paramedic2Name,
                    grade = detailData.paramedic2Grade.takeIf { it.isNotEmpty() },
                    rank = detailData.paramedic2Rank.takeIf { it.isNotEmpty() },
                    signature = if (detailData.paramedic2Signature.isNotEmpty()) "" else null
                )
            } else null

            // 운전요원 정보 변환
            val driver = if (detailData.driverName.isNotEmpty()) {
                ParamedicMember(
                    affiliation = null,
                    name = detailData.driverName,
                    grade = detailData.driverGrade.takeIf { it.isNotEmpty() },
                    rank = detailData.driverRank.takeIf { it.isNotEmpty() },
                    signature = if (detailData.driverSignature.isNotEmpty()) "" else null
                )
            } else null

            // 기타 인원 정보 변환
            val other = if (detailData.otherName.isNotEmpty()) {
                ParamedicMember(
                    affiliation = null,
                    name = detailData.otherName,
                    grade = detailData.otherGrade.takeIf { it.isNotEmpty() },
                    rank = detailData.otherRank.takeIf { it.isNotEmpty() },
                    signature = if (detailData.otherSignature.isNotEmpty()) "" else null
                )
            } else null

            // 장애요인 변환 - Set<String>을 List<ObstacleItem>으로 변환
            val obstacles = detailData.obstacles
                .filterNotNull()
                .filter { it.isNotEmpty() }
                .map { obstacleName ->
                    ObstacleItem(
                        type = obstacleName,
                        isCustom = obstacleName == "기타",
                        value = if (obstacleName == "기타") detailData.obstacleOtherValue else null
                    )
                }

            // createdAt, updatedAt 생성
            val currentTime = java.time.ZonedDateTime.now()
            val formatter = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
            val currentIsoTime = currentTime.format(formatter)

            val request = DetailReportRequest(
                data = DetailReportRequestData(
                    detailReport = DetailReportInfo(
                        doctor = doctor,
                        paramedic1 = paramedic1,
                        paramedic2 = paramedic2,
                        driver = driver,
                        other = other,
                        obstacles = obstacles,
                        createdAt = currentIsoTime,
                        updatedAt = currentIsoTime
                    )
                )
            )

            // ActivityViewModel의 updateDetailReport 호출
            activityViewModel.updateDetailReport(emergencyReportId, request)

            Log.d(TAG, "✅ 세부사항 섹션 저장 요청 완료")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 세부사항 섹션 저장 실패: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * 데이터 초기화
     */
    fun clearData() {
        _activityLogData.value = ActivityLogData()
        _lastSavedTime.value = ""
        _saveState.value = SaveState.Idle
        currentEmergencyReportId = 0
        Log.d(TAG, "🧹 LogViewModel 데이터 초기화")
    }
}