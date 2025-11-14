package com.example.ssairen_app.data.model.request

/**
 * 환자이송 섹션 저장 요청 모델
 *
 * API: PATCH /api/emergency-reports/{emergencyReportId}/sections/TRANSPORT
 */
data class TransportRequest(
    val data: TransportRequestData
)

data class TransportRequestData(
    val schemaVersion: Int = 1,
    val transport: TransportInfo
)

data class TransportInfo(
    val firstTransport: TransportDetail?,       // 1차 이송 정보 (필수)
    val secondTransport: TransportDetail?,      // 2차 이송 정보 (선택)
    val createdAt: String,                      // 생성 시각 (ISO 8601)
    val updatedAt: String                       // 수정 시각 (ISO 8601)
)

data class TransportDetail(
    val hospitalName: String,                   // 이송 기관명
    val regionType: String,                     // 관할 | 타시·도
    val arrivalTime: String,                    // 도착 시각 (HH:mm)
    val distanceKm: Double,                     // 거리 (km)
    val selectedBy: String,                     // 의료기관 선정자
    val retransportReason: List<RetransportReason>, // 재이송 사유 (복수 선택)
    val receiver: String,                       // 환자 인수자: 의사 | 간호사 | 응급구조사 | 기타
    val receiverSign: ReceiverSign?             // 인수자 서명 (선택)
)

data class RetransportReason(
    val type: String,                           // 사유 타입 (병상부족, 전문의부재 등)
    val name: List<String>? = null,             // 병상부족일 경우만: 응급실, 중환자실 등
    val isCustom: Boolean = false               // 기타 입력 여부
)

data class ReceiverSign(
    val type: String?,                          // 파일 형식 (예: image/png)
    val data: String?                           // Base64 인코딩된 서명 이미지
)
