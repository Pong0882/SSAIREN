package com.ssairen.domain.file.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * STT (Speech-to-Text) 응답 DTO
 */
@Getter
@Builder
public class SttResponse {

    /**
     * 전체 변환된 텍스트
     */
    private String text;

    /**
     * 발화자별 세그먼트 리스트
     */
    private List<TranscriptSegment> segments;

    /**
     * 감지된 언어 (예: ko, en, ja)
     */
    private String language;

    /**
     * 토큰 사용량
     */
    private TokenUsage usage;

    /**
     * 처리 시간 (초)
     */
    private Double processingTime;
}
