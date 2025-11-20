package com.example.ssairen_app.data.model.request

/**
 * AI 기반 병원 추천 요청 모델
 *
 * API: POST /api/hospital-selection/ai-recommendation
 */
data class HospitalAiRecommendationRequest(
    val emergencyReportId: Long,
    val latitude: Double,
    val longitude: Double,
    val radius: Int
)
