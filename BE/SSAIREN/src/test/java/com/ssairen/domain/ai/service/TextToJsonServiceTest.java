package com.ssairen.domain.ai.service;

import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TextToJsonServiceTest {

    @Mock
    private WebClient aiServerWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private TextToJsonService service;

    @BeforeEach
    void setUp() {
        // WebClient mock chain 설정
        when(aiServerWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("텍스트를 JSON으로 변환 - 성공 (기본 파라미터)")
    void convertTextToJson_success_defaultParams() {
        // given
        String conversation = "환자가 복통을 호소합니다. 나이는 45세입니다.";
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("patientInfo", Map.of("age", 45, "complaint", "복통"));

        when(responseSpec.bodyToMono(Object.class))
                .thenReturn(Mono.just(expectedResponse));

        // when
        Object result = service.convertTextToJson(conversation);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
        verify(aiServerWebClient).post();
    }

    @Test
    @DisplayName("텍스트를 JSON으로 변환 - 성공 (커스텀 파라미터)")
    void convertTextToJson_success_customParams() {
        // given
        String conversation = "환자 정보";
        Integer maxNewTokens = 500;
        Double temperature = 0.2;
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("data", "test");

        when(responseSpec.bodyToMono(Object.class))
                .thenReturn(Mono.just(expectedResponse));

        // when
        Object result = service.convertTextToJson(conversation, maxNewTokens, temperature);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("텍스트를 JSON으로 변환 - null 파라미터는 기본값 사용")
    void convertTextToJson_nullParams_useDefaults() {
        // given
        String conversation = "환자 정보";
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("data", "test");

        when(responseSpec.bodyToMono(Object.class))
                .thenReturn(Mono.just(expectedResponse));

        // when
        Object result = service.convertTextToJson(conversation, null, null);

        // then
        assertThat(result).isNotNull();
        verify(aiServerWebClient).post();
    }

    @Test
    @DisplayName("텍스트를 JSON으로 변환 - AI 서버 응답 없음")
    void convertTextToJson_nullResponse() {
        // given
        String conversation = "환자 정보";

        when(responseSpec.bodyToMono(Object.class))
                .thenReturn(Mono.empty());

        // when & then
        assertThatThrownBy(() -> service.convertTextToJson(conversation))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTERNAL_SERVER_ERROR)
                .hasMessageContaining("AI 서버로부터 응답을 받지 못했습니다");
    }

    @Test
    @DisplayName("텍스트를 JSON으로 변환 - WebClient 예외 발생")
    void convertTextToJson_webClientException() {
        // given
        String conversation = "환자 정보";

        when(responseSpec.bodyToMono(Object.class))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        // when & then
        assertThatThrownBy(() -> service.convertTextToJson(conversation))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INTERNAL_SERVER_ERROR)
                .hasMessageContaining("Text to JSON 변환에 실패했습니다");
    }

    @Test
    @DisplayName("텍스트를 JSON으로 변환 - 빈 문자열")
    void convertTextToJson_emptyString() {
        // given
        String conversation = "";
        Map<String, Object> expectedResponse = new HashMap<>();

        when(responseSpec.bodyToMono(Object.class))
                .thenReturn(Mono.just(expectedResponse));

        // when
        Object result = service.convertTextToJson(conversation);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("텍스트를 JSON으로 변환 - 긴 텍스트")
    void convertTextToJson_longText() {
        // given
        String conversation = "환자 정보 ".repeat(1000);
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("result", "success");

        when(responseSpec.bodyToMono(Object.class))
                .thenReturn(Mono.just(expectedResponse));

        // when
        Object result = service.convertTextToJson(conversation);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedResponse);
    }
}
