package com.example.ssairen_app.data.model.response

/**
 * AI 기반 병원 추천 응답 모델
 *
 * API: POST /api/hospital-selection/ai-recommendation
 */
data class HospitalAiRecommendationResponse(
    val success: Boolean,
    val message: String,
    val data: HospitalAiRecommendationData?
)

data class HospitalAiRecommendationData(
    val emergencyReportId: Long,
    val gptReasoning: String,
    val recommendedHospitals: List<String>,
    val totalHospitalsFound: Int,
    val reasoningTime: Double,
    val hospitalsDetail: List<HospitalDetail>,
    val hospitalSelections: List<HospitalSelectionInfo>
)

data class HospitalDetail(
    val name: String,
    val distance: Double,
    val specialties: List<String>,
    val emergencyLevel: String
)

data class HospitalSelectionInfo(
    val hospitalSelectionId: Int,
    val hospitalName: String,
    val status: String,
    val createdAt: String
)
