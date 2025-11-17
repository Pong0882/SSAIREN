package com.example.ssairen_app.util

import com.example.ssairen_app.data.model.request.CreatePatientInfoRequest
import com.example.ssairen_app.data.model.response.PatientInfoResponse
import com.example.ssairen_app.data.model.response.PatientEvaResponse
import com.example.ssairen_app.data.model.response.PatientTypeResponse
import com.example.ssairen_app.data.model.response.DispatchResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 구급일지 데이터를 환자 정보 생성 API 요청 바디로 맵핑하는 유틸리티
 */
object PatientInfoMapper {

    /**
     * 구급일지 섹션 데이터들을 환자 정보 생성 요청으로 맵핑
     *
     * @param emergencyReportId 구급일지 ID
     * @param patientInfo 환자 정보 섹션
     * @param patientEva 환자 평가 섹션
     * @param patientType 환자 발생 유형 섹션
     * @param dispatch 구급 출동 섹션
     * @return CreatePatientInfoRequest 환자 정보 생성 요청
     */
    fun mapToCreatePatientInfoRequest(
        emergencyReportId: Int,
        patientInfo: PatientInfoResponse?,
        patientEva: PatientEvaResponse?,
        patientType: PatientTypeResponse?,
        dispatch: DispatchResponse?
    ): CreatePatientInfoRequest {

        // 현재 시간을 recordTime으로 사용
        val currentTime = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val recordTime = formatter.format(currentTime)

        // gender: patientInfo - patient - gender ("남성" → "M", "여성" → "F")
        val genderRaw = patientInfo?.data?.data?.patientInfo?.patient?.gender
        val gender = when (genderRaw) {
            "남성" -> "M"
            "여성" -> "F"
            else -> genderRaw
        }

        // age: patientInfo - patient - ageYears
        val age = patientInfo?.data?.data?.patientInfo?.patient?.ageYears

        // mentalStatus: assessment - consciousness - first - state
        // 값이 null이거나 빈 문자열인 경우 'ALERT'로 설정
        // A -> ALERT, V -> VERBAL, P -> PAIN, U -> UNRESPONSIVE
        val mentalStatusRaw = patientEva?.data?.data?.assessment?.consciousness?.first?.state
        val mentalStatus = when {
            mentalStatusRaw.isNullOrBlank() -> "ALERT"
            mentalStatusRaw == "A" -> "ALERT"
            mentalStatusRaw == "V" -> "VERBAL"
            mentalStatusRaw == "P" -> "PAIN"
            mentalStatusRaw == "U" -> "UNRESPONSIVE"
            else -> "ALERT"  // 알 수 없는 값도 기본값으로 처리
        }

        // chiefComplaint: dispatch - pain, trauma, otherSymptoms의 name
        val chiefComplaint = buildChiefComplaint(dispatch)

        // hr: assessment - vitalSigns - first - pulse
        val hr = patientEva?.data?.data?.assessment?.vitalSigns?.first?.pulse

        // bp: assessment - vitalSigns - first - bloodPressure
        val bp = patientEva?.data?.data?.assessment?.vitalSigns?.first?.bloodPressure

        // rr: assessment - vitalSigns - first - respiration
        val rr = patientEva?.data?.data?.assessment?.vitalSigns?.first?.respiration

        // spo2: assessment - vitalSigns - first - spo2
        val spo2 = patientEva?.data?.data?.assessment?.vitalSigns?.first?.spo2

        // bt: assessment - vitalSigns - first - temperature
        val bt = patientEva?.data?.data?.assessment?.vitalSigns?.first?.temperature

        // hx: incidentType - medicalHistory - items - name (전부 다) + value
        val hx = buildMedicalHistory(patientType)

        // lnt: assessment - notes - onset
        // 날짜/시간 형식이 아니면 null로 처리 (LocalDateTime 파싱 가능한 형식만 허용)
        val lntRaw = patientEva?.data?.data?.assessment?.notes?.onset
        val lnt = parseToDateTime(lntRaw)

        // hasGuardian: patientInfo - guardian 있으면 true, 없으면 false
        val hasGuardian = patientInfo?.data?.data?.patientInfo?.guardian != null

        // onsetTime: lnt와 동일하게 설정
        val onsetTime = lnt

        return CreatePatientInfoRequest(
            emergencyReportId = emergencyReportId,
            gender = gender,
            age = age,
            recordTime = recordTime,
            mentalStatus = mentalStatus,
            chiefComplaint = chiefComplaint,
            hr = hr,
            bp = bp,
            spo2 = spo2,
            rr = rr,
            bt = bt,
            hasGuardian = hasGuardian,
            hx = hx,
            onsetTime = onsetTime,
            lnt = lnt
        )
    }

    /**
     * dispatch 섹션의 증상들을 chiefComplaint 문자열로 변환
     * pain, trauma, otherSymptoms를 모두 포함하며, 쉼표로 구분
     * 모두 없는 경우 "없음" 반환
     */
    private fun buildChiefComplaint(dispatch: DispatchResponse?): String? {
        val symptoms = mutableListOf<String>()

        dispatch?.data?.data?.dispatch?.symptoms?.let { symptomData ->
            // pain 증상 추가
            symptomData.pain?.forEach { symptom ->
                val symptomText = if (symptom.value != null) {
                    "${symptom.name}(${symptom.value})"
                } else {
                    symptom.name
                }
                symptoms.add(symptomText)
            }

            // trauma 증상 추가
            symptomData.trauma?.forEach { symptom ->
                val symptomText = if (symptom.value != null) {
                    "${symptom.name}(${symptom.value})"
                } else {
                    symptom.name
                }
                symptoms.add(symptomText)
            }

            // otherSymptoms 추가
            symptomData.otherSymptoms?.forEach { symptom ->
                val symptomText = if (symptom.value != null) {
                    "${symptom.name}(${symptom.value})"
                } else {
                    symptom.name
                }
                symptoms.add(symptomText)
            }
        }

        return if (symptoms.isEmpty()) {
            "없음"
        } else {
            symptoms.joinToString(", ")
        }
    }

    /**
     * patientType 섹션의 병력을 hx 문자열로 변환
     * status가 "있음"인 경우에만 items의 name과 value를 포함
     * name이 "기타"인 경우 "기타(value내용)" 형식으로 표시
     */
    private fun buildMedicalHistory(patientType: PatientTypeResponse?): String? {
        val medicalHistory = patientType?.data?.data?.incidentType?.medicalHistory

        // status가 "있음"이 아니면 null 또는 빈 문자열 반환
        if (medicalHistory?.status != "있음") {
            return "없음"
        }

        val historyItems = mutableListOf<String>()

        medicalHistory.items?.forEach { item ->
            val itemText = if (item.name == "기타" && item.value != null) {
                "기타(${item.value})"
            } else if (item.value != null) {
                "${item.name}(${item.value})"
            } else {
                item.name
            }
            historyItems.add(itemText)
        }

        return if (historyItems.isEmpty()) {
            null
        } else {
            historyItems.joinToString(", ")
        }
    }

    /**
     * 문자열을 LocalDateTime 형식으로 변환
     * ISO 8601 형식이 아니면 현재 시간 기준으로 임의의 시간 생성
     */
    private fun parseToDateTime(dateTimeString: String?): String? {
        if (dateTimeString.isNullOrBlank()) {
            return null
        }

        // ISO 8601 형식인지 확인 (yyyy-MM-dd'T'HH:mm:ss 형태)
        val iso8601Pattern = Regex("""^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$""")

        return if (iso8601Pattern.matches(dateTimeString)) {
            // 이미 올바른 형식이면 그대로 반환
            dateTimeString
        } else {
            // 형식이 맞지 않으면 현재 시간 기준 1시간 전으로 설정
            val calendar = java.util.Calendar.getInstance()
            calendar.add(java.util.Calendar.HOUR_OF_DAY, -1)  // 1시간 전

            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            formatter.format(calendar.time)
        }
    }
}
