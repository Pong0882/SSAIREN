package com.example.ssairen_app.data.model.request

/**
 * 구급출동 섹션 저장 요청 모델
 *
 * API: PATCH /api/emergency-reports/{emergencyReportId}/sections/DISPATCH
 */
data class DispatchRequest(
    val data: DispatchRequestData
)

data class DispatchRequestData(
    val dispatch: DispatchInfo
)

data class DispatchInfo(
    val reportDatetime: String,           // 신고 일시 (ISO 8601, 예: "2022-08-11T02:26:00")
    val departureTime: String,            // 출동 시각 (HH:mm)
    val arrivalSceneTime: String,         // 현장 도착 (HH:mm)
    val departureSceneTime: String,       // 현장 출발 (HH:mm)
    val contactTime: String,              // 환자 접촉 (HH:mm)
    val arrivalHospitalTime: String,      // 병원 도착 (HH:mm)
    val distanceKm: Double,               // 거리 (km)
    val returnTime: String,               // 귀소 시간 (HH:mm)
    val dispatchType: String,             // 출동 유형: 정상 | 오인 | 거짓 | 취소 | 기타
    val sceneLocation: SceneLocation,     // 환자 발생 장소
    val symptoms: Symptoms,               // 환자 증상
    val createdAt: String,                // 생성 시각 (ISO 8601)
    val updatedAt: String                 // 수정 시각 (ISO 8601)
)

data class SceneLocation(
    val name: String,                     // 장소 세부: 집 | 집단거주시설 | 도로 | ... | 기타
    val value: String?                    // '기타' 선택 시 세부 입력 내용
)

data class Symptoms(
    val pain: List<SymptomItem>,          // 통증 (두통, 흉통, 복통, 요통, 분만진통, 그 밖의 통증)
    val trauma: List<SymptomItem>,        // 외상 (골절, 탈구, 삠, 열상, 찰과상, 타박상, 절단, 압궤손상, 화상)
    val otherSymptoms: List<SymptomItem>  // 그 외 증상 (의식장애, 기도이물, 기침 등)
)

data class SymptomItem(
    val name: String,                     // 증상 이름
    val value: String?                    // null: 기본 항목, string: '그 밖의 통증' 또는 '기타'에 입력한 내용
)
