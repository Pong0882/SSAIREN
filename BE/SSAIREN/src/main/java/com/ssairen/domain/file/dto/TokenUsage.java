package com.ssairen.domain.file.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 토큰 사용량 정보 DTO
 */
@Getter
@Builder
public class TokenUsage {

    /**
     * 입력 토큰 수
     */
    private Integer inputTokens;

    /**
     * 출력 토큰 수
     */
    private Integer outputTokens;

    /**
     * 전체 토큰 수
     */
    private Integer totalTokens;

    /**
     * 오디오 토큰 수
     */
    private Integer audioTokens;

    /**
     * 텍스트 토큰 수
     */
    private Integer textTokens;
}
