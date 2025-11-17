package com.ssairen.global.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("성공 응답 생성 - 데이터와 메시지 포함")
    void success_withDataAndMessage() {
        // given
        String data = "테스트 데이터";
        String message = "성공했습니다";

        // when
        ApiResponse<String> response = ApiResponse.success(data, message);

        // then
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("성공 응답 생성 - 데이터만 포함")
    void success_withDataOnly() {
        // given
        String data = "테스트 데이터";

        // when
        ApiResponse<String> response = ApiResponse.success(data, null);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo(data);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("성공 응답 생성 - 메시지만 포함 (데이터 없음)")
    void success_withMessageOnly() {
        // given
        String message = "처리 완료";

        // when
        ApiResponse<Void> response = ApiResponse.success(message);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("에러 응답 생성")
    void error_response() {
        // given
        ErrorResponse error = ErrorResponse.of("ERR001", "에러 발생");
        int status = 400;

        // when
        ApiResponse<String> response = ApiResponse.error(error, status);

        // then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isEqualTo(error);
        assertThat(response.getStatus()).isEqualTo(status);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("성공 응답 - 복잡한 객체 데이터")
    void success_withComplexObject() {
        // given
        List<String> data = Arrays.asList("항목1", "항목2", "항목3");
        String message = "목록 조회 성공";

        // when
        ApiResponse<List<String>> response = ApiResponse.success(data, message);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).hasSize(3);
        assertThat(response.getData()).containsExactly("항목1", "항목2", "항목3");
        assertThat(response.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("성공 응답 - null 데이터")
    void success_withNullData() {
        // when
        ApiResponse<String> response = ApiResponse.success(null, "완료");

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNull();
        assertThat(response.getMessage()).isEqualTo("완료");
    }

    @Test
    @DisplayName("타임스탬프 확인")
    void timestamp_verification() {
        // given
        ZonedDateTime before = ZonedDateTime.now().minusSeconds(1);

        // when
        ApiResponse<String> response = ApiResponse.success("데이터", null);
        ZonedDateTime after = ZonedDateTime.now().plusSeconds(1);

        // then
        assertThat(response.getTimestamp()).isNotNull();
        assertThat(response.getTimestamp()).isAfter(before);
        assertThat(response.getTimestamp()).isBefore(after);
    }

    @Test
    @DisplayName("Builder 패턴 사용")
    void builder_pattern() {
        // when
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .data("커스텀 데이터")
                .message("커스텀 메시지")
                .timestamp(ZonedDateTime.now())
                .build();

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isEqualTo("커스텀 데이터");
        assertThat(response.getMessage()).isEqualTo("커스텀 메시지");
    }

    @Test
    @DisplayName("여러 타입의 데이터 응답")
    void success_variousDataTypes() {
        // Integer 타입
        ApiResponse<Integer> intResponse = ApiResponse.success(123);
        assertThat(intResponse.getData()).isEqualTo(123);

        // Boolean 타입
        ApiResponse<Boolean> boolResponse = ApiResponse.success(true);
        assertThat(boolResponse.getData()).isTrue();

        // Custom 객체 타입
        ErrorResponse customObj = ErrorResponse.of("CODE", "MSG");
        ApiResponse<ErrorResponse> objResponse = ApiResponse.success(customObj);
        assertThat(objResponse.getData().getCode()).isEqualTo("CODE");
    }

    @Test
    @DisplayName("에러 응답 - 여러 상태 코드")
    void error_variousStatusCodes() {
        ErrorResponse error = ErrorResponse.of("ERR", "에러");

        ApiResponse<Void> response400 = ApiResponse.error(error, 400);
        assertThat(response400.getStatus()).isEqualTo(400);

        ApiResponse<Void> response500 = ApiResponse.error(error, 500);
        assertThat(response500.getStatus()).isEqualTo(500);

        ApiResponse<Void> response404 = ApiResponse.error(error, 404);
        assertThat(response404.getStatus()).isEqualTo(404);
    }
}
