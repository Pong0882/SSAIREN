package com.example.ssairen.wear

/**
 * 메시지 우선순위 레벨
 * 숫자가 높을수록 우선순위가 높음
 */
enum class MessagePriority(val level: Int) {
    CLEAR(0),           // 빈 문자열 (메시지 숨김)
    IN_PROGRESS(1),     // 진행 중 상태 (정상 작동)
    CONNECTING(2),      // 초기 연결 상태 (사용자 대기 필요)
    MEASUREMENT_ERROR(3), // 측정 오류/재시도 필요
    CRITICAL_ERROR(4);  // 치명적 오류 (즉시 조치 필요)

    companion object {
        /**
         * 메시지 내용으로부터 우선순위 레벨을 결정
         */
        fun fromMessage(message: String): MessagePriority {
            if (message.isEmpty()) return CLEAR

            return when {
                // 레벨 4: 치명적 오류
                message.contains("센서 연결 실패") ||
                message.contains("센서 초기화 실패") ||
                message.contains("센서 연결 종료됨") ||
                message.contains("산소포화도 센서 오류") -> CRITICAL_ERROR

                // 레벨 3: 측정 오류
                message.contains("측정 실패") ||
                message.contains("손목 착용 확인") ||
                message.contains("측정 오류") -> MEASUREMENT_ERROR

                // 레벨 2: 초기 연결 상태
                message.contains("센서 연결 중") ||
                message.contains("센서 초기화 중") ||
                message.contains("첫 심박수 측정 중") -> CONNECTING

                // 레벨 1: 진행 중 상태
                message.contains("SpO₂ 측정 중") ||
                message.contains("센서 재정비 중") ||
                message.contains("간격 측정 시작") ||
                message.contains("반복 측정 중지") -> IN_PROGRESS

                // 기타 메시지는 진행 중으로 처리
                else -> IN_PROGRESS
            }
        }
    }
}

/**
 * 우선순위가 있는 메시지 데이터 클래스
 */
data class PriorityMessage(
    val content: String,
    val priority: MessagePriority = MessagePriority.fromMessage(content),
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * 다른 메시지보다 우선순위가 높은지 확인
     */
    fun hasHigherPriorityThan(other: PriorityMessage?): Boolean {
        if (other == null) return content.isNotEmpty()

        // 특수 케이스 1: 빈 문자열은 오류가 아닌 메시지만 지움
        if (this.priority == MessagePriority.CLEAR) {
            return when (other.priority) {
                MessagePriority.CRITICAL_ERROR -> false  // 치명적 오류는 지우지 않음
                MessagePriority.MEASUREMENT_ERROR -> false  // 측정 오류는 지우지 않음
                else -> true  // 나머지는 지움
            }
        }

        // 특수 케이스 2: 새로운 "측정 중" 메시지는 오래된 오류 메시지를 덮어씀 (5초 경과 시)
        if (this.content.contains("측정 중") && other.isError()) {
            val elapsedTime = System.currentTimeMillis() - other.timestamp
            if (elapsedTime > 5_000L) {  // 5초 경과
                return true  // 오류 메시지를 덮어씀
            }
        }

        return when {
            // 우선순위가 높으면 덮어씀
            this.priority.level > other.priority.level -> true

            // 우선순위가 같으면 최신 메시지 사용
            this.priority.level == other.priority.level -> true

            // 우선순위가 낮으면 무시
            else -> false
        }
    }

    /**
     * 오류 메시지인지 확인 (색상 표시용)
     */
    fun isError(): Boolean {
        return priority == MessagePriority.CRITICAL_ERROR ||
               priority == MessagePriority.MEASUREMENT_ERROR
    }

    /**
     * 연결 상태 메시지인지 확인 (색상 표시용)
     */
    fun isConnecting(): Boolean {
        return priority == MessagePriority.CONNECTING
    }
}
