package com.ssairen.domain.ai.service;

import com.ssairen.domain.file.dto.LocalWhisperSttResponse;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 로컬 Whisper STT Service
 * 로컬 Faster-Whisper API를 호출하여 음성을 텍스트로 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalWhisperSttService {

    @Value("${ai.local-whisper.base-url:https://alondra-reprobationary-margeret.ngrok-free.dev}")
    private String localWhisperBaseUrl;

    private static final String LOCAL_STT_ENDPOINT = "/api/stt/local/full";

    /**
     * 로컬 Whisper 모델을 사용한 음성-텍스트 변환 (전체 텍스트 반환)
     *
     * @param file 오디오 파일
     * @param language 언어 코드 (기본값: ko)
     * @return 로컬 Whisper STT 변환 결과
     */
    public LocalWhisperSttResponse convertSpeechToText(MultipartFile file, String language) {
        try {
            log.info("로컬 Whisper STT 변환 시작 - 파일명: {}, 언어: {}",
                    file.getOriginalFilename(), language);

            // Multipart 요청 바디 생성
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource())
                    .contentType(MediaType.parseMediaType(file.getContentType()));

            // WebClient 생성 (로컬 Whisper API용)
            WebClient localWhisperWebClient = WebClient.builder()
                    .baseUrl(localWhisperBaseUrl)
                    .build();

            // 로컬 Whisper API로 STT 요청
            LocalWhisperSttResponse response = localWhisperWebClient.post()
                    .uri(uriBuilder -> {
                        var uri = uriBuilder.path(LOCAL_STT_ENDPOINT);
                        if (language != null && !language.isEmpty()) {
                            uri.queryParam("language", language);
                        }
                        return uri.build();
                    })
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(LocalWhisperSttResponse.class)
                    .block();

            log.info("로컬 Whisper STT 변환 완료 - 텍스트 길이: {} 문자, 세그먼트 수: {}, 오디오 길이: {}초",
                    response.getText().length(),
                    response.getSegments() != null ? response.getSegments().size() : 0,
                    response.getDuration());

            return response;

        } catch (Exception e) {
            log.error("로컬 Whisper STT 변환 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.STT_PROCESSING_FAILED,
                    "로컬 Whisper STT 변환에 실패했습니다: " + e.getMessage());
        }
    }
}
