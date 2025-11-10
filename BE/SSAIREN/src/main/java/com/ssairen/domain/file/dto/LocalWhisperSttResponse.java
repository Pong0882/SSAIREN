package com.ssairen.domain.file.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 로컬 Whisper STT API 응답 DTO
 * 외부 로컬 Whisper API의 응답 형식에 맞춘 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalWhisperSttResponse {

    /**
     * 전체 변환된 텍스트
     */
    @JsonProperty("text")
    private String text;

    /**
     * 발화자별 세그먼트 리스트
     */
    @JsonProperty("segments")
    private List<TranscriptSegment> segments;

    /**
     * 감지된 언어 (예: ko, en, ja)
     */
    @JsonProperty("language")
    private String language;

    /**
     * 오디오 길이 (초)
     */
    @JsonProperty("duration")
    private Double duration;
}
