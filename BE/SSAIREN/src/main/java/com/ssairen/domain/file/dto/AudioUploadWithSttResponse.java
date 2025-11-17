package com.ssairen.domain.file.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 오디오 파일 업로드 + STT 통합 응답 DTO
 */
@Getter
@Builder
public class AudioUploadWithSttResponse {

    /**
     * 파일 업로드 정보
     */
    private FileUploadResponse fileInfo;

    /**
     * STT 변환 결과
     */
    private SttResponse sttResult;
}
