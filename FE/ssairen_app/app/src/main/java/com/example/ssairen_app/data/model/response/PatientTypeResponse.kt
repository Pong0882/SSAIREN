// PatientTypeResponse.kt
package com.example.ssairen_app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * 환자발생유형 섹션 조회 응답
 * GET /api/emergency-reports/{id}/sections/INCIDENT_TYPE
 */
data class PatientTypeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("data")
    val data: PatientTypeSectionData,

    @SerializedName("message")
    val message: String?,

    @SerializedName("timestamp")
    val timestamp: String
)

data class PatientTypeSectionData(
    @SerializedName("id")
    val id: Int,

    @SerializedName("emergencyReportId")
    val emergencyReportId: Int,

    @SerializedName("type")
    val type: String,  // "INCIDENT_TYPE"

    @SerializedName("data")
    val data: PatientTypeDataWrapper,

    @SerializedName("version")
    val version: Int,

    @SerializedName("createdAt")
    val createdAt: String
)

data class PatientTypeDataWrapper(
    @SerializedName("schemaVersion")
    val schemaVersion: Int,

    @SerializedName("incidentType")
    val incidentType: IncidentTypeData
)

data class IncidentTypeData(
    @SerializedName("category")
    val category: String?,  // "질병", "질병외", "기타"

    @SerializedName("category_other")
    val categoryOther: String?,  // "기타" 세부 정보

    @SerializedName("medicalHistory")
    val medicalHistory: MedicalHistoryData?,

    @SerializedName("legalSuspicion")
    val legalSuspicion: LegalSuspicionData?,

    @SerializedName("subCategory_traffic")
    val subCategoryTraffic: SubCategoryTrafficData?,

    @SerializedName("subCategory_injury")
    val subCategoryInjury: SubCategoryInjuryData?,

    @SerializedName("subCategory_nonTrauma")
    val subCategoryNonTrauma: SubCategoryNonTraumaData?,

    @SerializedName("subCategory_other")
    val subCategoryOther: SubCategoryOtherData?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?
)

// ✅ 병력 데이터 (실제 API 구조)
data class MedicalHistoryData(
    @SerializedName("status")
    val status: String?,  // "있음", "없음", "미상"

    @SerializedName("items")
    val items: List<MedicalHistoryItem>?
)

data class MedicalHistoryItem(
    @SerializedName("name")
    val name: String,  // "고혈압", "당뇨", "암", "감염병", "신부전", "기타" 등

    @SerializedName("value")
    val value: String?  // 추가 정보 (예: "폐암", "결핵", "예", "과거 수술 이력 있음")
)

// ✅ 범죄 의심 데이터 (실제 API 구조)
data class LegalSuspicionData(
    @SerializedName("name")
    val name: String?  // "경찰통보", "경찰입회", "긴급이송", "관련기관 통보"
)

// ✅ 교통사고 데이터 (실제 API 구조)
data class SubCategoryTrafficData(
    @SerializedName("type")
    val type: String?,  // "교통사고"

    @SerializedName("name")
    val name: String?,  // "운전자", "동승자", "보행자", "자전거", "오토바이", "개인형 이동장치", "그 밖의 탈 것", "미상"

    @SerializedName("value")
    val value: String?
)

// ✅ 그 외 외상 데이터 (실제 API 구조)
data class SubCategoryInjuryData(
    @SerializedName("type")
    val type: String?,  // "그 외 손상"

    @SerializedName("name")
    val name: String?  // "낙상", "추락", "관통상", "기계", "농기계", "그 밖의 둔상"
)

// ✅ 비외상성 손상 데이터 (실제 API 구조)
data class SubCategoryNonTraumaData(
    @SerializedName("type")
    val type: String?,  // "비외상성 손상"

    @SerializedName("name")
    val name: String?,  // "익수", "외력에 의한 압박", "이물질에 의한 기도막힘", "화염", "고온체", "전기", "물", "연기흡입", "중독", "화학물질", "동물/곤충", "온열손상", "한랭손상", "성폭행", "상해", "기타"

    @SerializedName("value")
    val value: String?  // 추가 정보 (예: "살모사")
)

// ✅ 기타 카테고리 데이터 (실제 API 구조)
data class SubCategoryOtherData(
    @SerializedName("name")
    val name: String?,  // "자연재해", "임신분만", "신생아", "단순구조", "기타"

    @SerializedName("value")
    val value: String?
)