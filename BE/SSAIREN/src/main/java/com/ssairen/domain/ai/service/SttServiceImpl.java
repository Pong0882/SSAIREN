package com.ssairen.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssairen.domain.file.dto.SttResponse;
import com.ssairen.domain.file.dto.TokenUsage;
import com.ssairen.domain.file.dto.TranscriptSegment;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

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

            // 스트리밍 응답 파싱
            List<TranscriptSegment> segments = new ArrayList<>();
            String finalText = "";
            TokenUsage usage = null;

            for (String chunk : responseFlux.toIterable()) {
                log.debug("STT 청크 수신: {}", chunk);

                // "data: " 접두사 제거 (있으면)
                String jsonStr = chunk.trim();
                if (jsonStr.startsWith("data: ")) {
                    jsonStr = jsonStr.substring(6).trim();
                }

                // 빈 문자열이면 스킵
                if (jsonStr.isEmpty()) {
                    continue;
                }

                try {
                    JsonNode jsonNode = objectMapper.readTree(jsonStr);
                    String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "";

                    if ("transcript.text.segment".equals(type)) {
                        // 세그먼트 파싱
                        TranscriptSegment segment = TranscriptSegment.builder()
                                .id(jsonNode.has("id") ? jsonNode.get("id").asText() : null)
                                .speaker(jsonNode.has("speaker") ? jsonNode.get("speaker").asText() : null)
                                .start(jsonNode.has("start") ? jsonNode.get("start").asDouble() : null)
                                .end(jsonNode.has("end") ? jsonNode.get("end").asDouble() : null)
                                .text(jsonNode.has("text") ? jsonNode.get("text").asText() : "")
                                .build();
                        segments.add(segment);

                    } else if ("transcript.text.done".equals(type)) {
                        // 최종 결과 파싱
                        finalText = jsonNode.has("text") ? jsonNode.get("text").asText() : "";

                        // 토큰 사용량 파싱
                        if (jsonNode.has("usage")) {
                            JsonNode usageNode = jsonNode.get("usage");
                            JsonNode inputDetails = usageNode.has("input_token_details")
                                    ? usageNode.get("input_token_details") : null;

                            usage = TokenUsage.builder()
                                    .inputTokens(usageNode.has("input_tokens") ? usageNode.get("input_tokens").asInt() : null)
                                    .outputTokens(usageNode.has("output_tokens") ? usageNode.get("output_tokens").asInt() : null)
                                    .totalTokens(usageNode.has("total_tokens") ? usageNode.get("total_tokens").asInt() : null)
                                    .audioTokens(inputDetails != null && inputDetails.has("audio_tokens")
                                            ? inputDetails.get("audio_tokens").asInt() : null)
                                    .textTokens(inputDetails != null && inputDetails.has("text_tokens")
                                            ? inputDetails.get("text_tokens").asInt() : null)
                                    .build();
                        }
                    }
                } catch (Exception e) {
                    log.warn("JSON 파싱 실패 (무시): {}", jsonStr, e);
                }
            }

            log.info("STT 변환 완료 - 텍스트 길이: {} 문자, 세그먼트 수: {}",
                    finalText.length(), segments.size());

            return SttResponse.builder()
                    .text(finalText)
                    .segments(segments)
                    .language(language)
                    .usage(usage)
                    .build();

        } catch (Exception e) {
            log.error("STT 변환 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.STT_PROCESSING_FAILED);
        }
    }
}
