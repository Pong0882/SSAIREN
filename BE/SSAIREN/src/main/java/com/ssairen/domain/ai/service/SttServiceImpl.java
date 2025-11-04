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

            // ������������ ������ ������
            SttParsingResult result = parseStreamingResponse(responseFlux);

            log.info("STT ������ ������ - ��������� ������: {} ������, ������������ ���: {}",
                    result.finalText.length(), result.segments.size());

            return SttResponse.builder()
                    .text(result.finalText)
                    .segments(result.segments)
                    .language(language)
                    .usage(result.usage)
                    .build();

        } catch (Exception e) {
            log.error("STT 변환 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.STT_PROCESSING_FAILED);
        }
    }

    /**
     * ������������ ��������� ������������ ��������� ������
     */
    private SttParsingResult parseStreamingResponse(Flux<String> responseFlux) {
        List<TranscriptSegment> segments = new ArrayList<>();
        String finalText = "";
        TokenUsage usage = null;

        for (String chunk : responseFlux.toIterable()) {
            log.debug("STT ������ ������: {}", chunk);

            String jsonStr = preprocessChunk(chunk);
            if (jsonStr.isEmpty()) {
                continue;
            }

            try {
                JsonNode jsonNode = objectMapper.readTree(jsonStr);
                String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "";

                if ("transcript.text.segment".equals(type)) {
                    TranscriptSegment segment = parseTranscriptSegment(jsonNode);
                    segments.add(segment);
                } else if ("transcript.text.done".equals(type)) {
                    finalText = jsonNode.has("text") ? jsonNode.get("text").asText() : "";
                    usage = parseTokenUsage(jsonNode);
                }
            } catch (Exception e) {
                log.warn("JSON ������ ������ (������): {}", jsonStr, e);
            }
        }

        return new SttParsingResult(segments, finalText, usage);
    }

    /**
     * ������ ��������� ���������
     */
    private String preprocessChunk(String chunk) {
        String jsonStr = chunk.trim();
        if (jsonStr.startsWith("data: ")) {
            jsonStr = jsonStr.substring(6).trim();
        }
        return jsonStr;
    }

    /**
     * ������������������ ������������ ������
     */
    private TranscriptSegment parseTranscriptSegment(JsonNode jsonNode) {
        return TranscriptSegment.builder()
                .id(jsonNode.has("id") ? jsonNode.get("id").asText() : null)
                .speaker(jsonNode.has("speaker") ? jsonNode.get("speaker").asText() : null)
                .start(jsonNode.has("start") ? jsonNode.get("start").asDouble() : null)
                .end(jsonNode.has("end") ? jsonNode.get("end").asDouble() : null)
                .text(jsonNode.has("text") ? jsonNode.get("text").asText() : "")
                .build();
    }

    /**
     * ������ ��������� ������
     */
    private TokenUsage parseTokenUsage(JsonNode jsonNode) {
        if (!jsonNode.has("usage")) {
            return null;
        }

        JsonNode usageNode = jsonNode.get("usage");
        JsonNode inputDetails = usageNode.has("input_token_details")
                ? usageNode.get("input_token_details") : null;

        return TokenUsage.builder()
                .inputTokens(usageNode.has("input_tokens") ? usageNode.get("input_tokens").asInt() : null)
                .outputTokens(usageNode.has("output_tokens") ? usageNode.get("output_tokens").asInt() : null)
                .totalTokens(usageNode.has("total_tokens") ? usageNode.get("total_tokens").asInt() : null)
                .audioTokens(inputDetails != null && inputDetails.has("audio_tokens")
                        ? inputDetails.get("audio_tokens").asInt() : null)
                .textTokens(inputDetails != null && inputDetails.has("text_tokens")
                        ? inputDetails.get("text_tokens").asInt() : null)
                .build();
    }

    /**
     * STT ������ ��������� ������ ������ ���������
     */
    private static class SttParsingResult {
        final List<TranscriptSegment> segments;
        final String finalText;
        final TokenUsage usage;

        SttParsingResult(List<TranscriptSegment> segments, String finalText, TokenUsage usage) {
            this.segments = segments;
            this.finalText = finalText;
            this.usage = usage;
        }
    }
}
