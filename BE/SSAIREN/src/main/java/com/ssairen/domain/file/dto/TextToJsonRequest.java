package com.ssairen.domain.file.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 서버 Text To Json API 요청 DTO
 * STT 결과를 JSON으로 변환하기 위한 요청
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextToJsonRequest {

    /**
     * 대화 텍스트
     */
    @JsonProperty("conversation")
    private String conversation;

    /**
     * 최대 생성 토큰 수
     */
    @JsonProperty("max_new_tokens")
    @Builder.Default
    private Integer maxNewTokens = 700;

    /**
     * 생성 온도 (0.0 ~ 1.0)
     */
    @JsonProperty("temperature")
    @Builder.Default
    private Double temperature = 0.1;
}
