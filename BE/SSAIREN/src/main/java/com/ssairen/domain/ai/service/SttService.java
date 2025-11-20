package com.ssairen.domain.ai.service;

import com.ssairen.domain.file.dto.SttResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * STT (Speech-to-Text) Service 인터페이스
 */
public interface SttService {

    /**
     * Whisper 모델을 사용한 음성-텍스트 변환
     *
     * @param file 오디오 파일
     * @param language 언어 코드 (선택사항, null이면 자동 감지)
     * @return STT 변환 결과
     */
    SttResponse convertSpeechToText(MultipartFile file, String language);
}
