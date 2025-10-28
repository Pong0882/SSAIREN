package com.ssairen.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssairen.domain.file.dto.SttResponse;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

/**
 * STT (Speech-to-Text) Service 구현체
 * AI 서버의 Whisper API를 호출하여 음성을 텍스트로 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SttServiceImpl implements SttService {

    private final WebClient aiServerWebClient;
    private final ObjectMapper objectMapper;

    private static final String STT_ENDPOINT = "/api/stt/whisper";

    /**
     * Whisper 모델을 사용한 음성-텍스트 변환
     */
    @Override
    public SttResponse convertSpeechToText(MultipartFile file, String language) {
        try {
            log.info("STT 변환 시작 - 파일명: {}, 언어: {}", file.getOriginalFilename(), language);

            // Multipart 요청 바디 생성
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource())
                    .contentType(MediaType.parseMediaType(file.getContentType()));

            // AI 서버로 STT 요청 (스트리밍 응답)
            Flux<String> responseFlux = aiServerWebClient.post()
                    .uri(uriBuilder -> {
                        var uri = uriBuilder.path(STT_ENDPOINT);
                        if (language != null && !language.isEmpty()) {
                            uri.queryParam("language", language);
                        }
                        return uri.build();
                    })
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToFlux(String.class);

            // 스트리밍 응답을 하나의 문자열로 결합
            String fullResponse = responseFlux
                    .reduce("", (acc, chunk) -> acc + chunk)
                    .block();

            log.debug("STT 응답 수신: {}", fullResponse);

            // JSON 파싱
            JsonNode jsonNode = objectMapper.readTree(fullResponse);

            String text = jsonNode.has("text") ? jsonNode.get("text").asText() : "";
            String detectedLanguage = jsonNode.has("language") ? jsonNode.get("language").asText() : language;
            Double processingTime = jsonNode.has("processing_time") ? jsonNode.get("processing_time").asDouble() : null;

            log.info("STT 변환 완료 - 텍스트 길이: {} 문자, 처리 시간: {}초",
                    text.length(), processingTime);

            return SttResponse.builder()
                    .text(text)
                    .language(detectedLanguage)
                    .processingTime(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("STT 변환 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.STT_PROCESSING_FAILED);
        }
    }
}
