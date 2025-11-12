package com.ssairen.domain.file.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AI 서버 Text To Json API 응답 DTO
 * AI 서버로부터 받은 JSON 추출 결과
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextToJsonResponse {

    /**
     * 성공 여부
     */
    @JsonProperty("success")
    private Boolean success;

    /**
     * 추출된 JSON 객체
     */
    @JsonProperty("extracted_json")
    private Object extractedJson;

    /**
     * 원본 출력 문자열
     */
    @JsonProperty("raw_output")
    private String rawOutput;
}
