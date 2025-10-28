package com.ssairen.domain.file.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * STT 변환 세그먼트 DTO
 * 각 발화자별 타임스탬프와 텍스트 정보
 */
@Getter
@Builder
public class TranscriptSegment {

    /**
     * 세그먼트 ID
     */
    private String id;

    /**
     * 발화자 (A, B, C 등)
     */
    private String speaker;

    /**
     * 시작 시간 (초)
     */
    private Double start;

    /**
     * 종료 시간 (초)
     */
    private Double end;

    /**
     * 변환된 텍스트
     */
    private String text;
}
