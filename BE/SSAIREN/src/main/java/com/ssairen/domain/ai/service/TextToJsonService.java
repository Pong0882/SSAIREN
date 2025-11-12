package com.ssairen.domain.ai.service;

import com.ssairen.domain.file.dto.TextToJsonRequest;
import com.ssairen.domain.file.dto.TextToJsonResponse;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Text To Json Service
 * AI 서버의 STT to JSON API를 호출하여 대화 텍스트를 JSON으로 변환
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TextToJsonService {

    private final WebClient aiServerWebClient;

    private static final String TEXT_TO_JSON_ENDPOINT = "/api/integrated/process-conversation";

    /**
     * 대화 텍스트를 JSON으로 변환
     *
     * @param conversation 대화 텍스트
     * @return 변환된 JSON 결과
     */
    public TextToJsonResponse convertTextToJson(String conversation) {
        return convertTextToJson(conversation, 700, 0.1);
    }

    /**
     * 대화 텍스트를 JSON으로 변환 (파라미터 커스터마이징)
     *
     * @param conversation 대화 텍스트
     * @param maxNewTokens 최대 생성 토큰 수
     * @param temperature 생성 온도
     * @return 변환된 JSON 결과
     */
    public TextToJsonResponse convertTextToJson(String conversation, Integer maxNewTokens, Double temperature) {
        try {
            log.info("Text to JSON 변환 시작 - 텍스트 길이: {} 문자", conversation.length());

            // 요청 DTO 생성
            TextToJsonRequest request = TextToJsonRequest.builder()
                    .conversation(conversation)
                    .maxNewTokens(maxNewTokens != null ? maxNewTokens : 700)
                    .temperature(temperature != null ? temperature : 0.1)
                    .build();

            // AI 서버로 요청
            TextToJsonResponse response = aiServerWebClient.post()
                    .uri(TEXT_TO_JSON_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(TextToJsonResponse.class)
                    .block();

            if (response == null) {
                throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "AI 서버로부터 응답을 받지 못했습니다.");
            }

            log.info("Text to JSON 변환 완료 - 성공: {}", response.getSuccess());

            return response;

        } catch (Exception e) {
            log.error("Text to JSON 변환 실패: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "Text to JSON 변환에 실패했습니다: " + e.getMessage());
        }
    }
}
