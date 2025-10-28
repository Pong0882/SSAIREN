package com.ssairen.domain.file.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * STT (Speech-to-Text) 응답 DTO
 */
@Getter
@Builder
public class SttResponse {

    /**
     * 변환된 텍스트
     */
    private String text;

    /**
     * 감지된 언어 (예: ko, en, ja)
     */
    private String language;

    /**
     * 처리 시간 (초)
     */
    private Double processingTime;
}
