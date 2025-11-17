package com.ssairen.global.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    @DisplayName("기본 에러 응답 생성 - of 메서드")
    void of_basicError() {
        // given
        String code = "ERR001";
        String message = "잘못된 요청입니다";

        // when
        ErrorResponse response = ErrorResponse.of(code, message);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(code);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getDetails()).isNull();
    }

    @Test
    @DisplayName("상세 정보 포함 에러 응답 생성")
    void of_withDetails() {
        // given
        String code = "VALIDATION_ERROR";
        String message = "유효성 검증 실패";
        List<ErrorResponse.FieldError> details = Arrays.asList(
                new ErrorResponse.FieldError("name", "이름은 필수입니다", null),
                new ErrorResponse.FieldError("age", "나이는 0보다 커야 합니다", -1)
        );

        // when
        ErrorResponse response = ErrorResponse.of(code, message, details);

        // then
        assertThat(response.getCode()).isEqualTo(code);
        assertThat(response.getMessage()).isEqualTo(message);
        assertThat(response.getDetails()).hasSize(2);
        assertThat(response.getDetails().get(0).getField()).isEqualTo("name");
        assertThat(response.getDetails().get(1).getField()).isEqualTo("age");
    }

    @Test
    @DisplayName("FieldError 생성 및 검증")
    void fieldError_creation() {
        // given
        String field = "email";
        String message = "이메일 형식이 올바르지 않습니다";
        String rejectedValue = "invalid-email";

        // when
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(field, message, rejectedValue);

        // then
        assertThat(fieldError.getField()).isEqualTo(field);
        assertThat(fieldError.getMessage()).isEqualTo(message);
        assertThat(fieldError.getRejectedValue()).isEqualTo(rejectedValue);
    }

    @Test
    @DisplayName("FieldError - Builder 패턴")
    void fieldError_builder() {
        // when
        ErrorResponse.FieldError fieldError = ErrorResponse.FieldError.builder()
                .field("password")
                .message("비밀번호는 8자 이상이어야 합니다")
                .rejectedValue("short")
                .build();

        // then
        assertThat(fieldError.getField()).isEqualTo("password");
        assertThat(fieldError.getMessage()).isEqualTo("비밀번호는 8자 이상이어야 합니다");
        assertThat(fieldError.getRejectedValue()).isEqualTo("short");
    }

    @Test
    @DisplayName("ErrorResponse - Builder 패턴")
    void errorResponse_builder() {
        // given
        List<ErrorResponse.FieldError> details = Arrays.asList(
                new ErrorResponse.FieldError("field1", "error1", "value1")
        );

        // when
        ErrorResponse response = ErrorResponse.builder()
                .code("CUSTOM_ERROR")
                .message("커스텀 에러 메시지")
                .details(details)
                .build();

        // then
        assertThat(response.getCode()).isEqualTo("CUSTOM_ERROR");
        assertThat(response.getMessage()).isEqualTo("커스텀 에러 메시지");
        assertThat(response.getDetails()).hasSize(1);
    }

    @Test
    @DisplayName("다양한 에러 코드 테스트")
    void various_errorCodes() {
        ErrorResponse err1 = ErrorResponse.of("NOT_FOUND", "리소스를 찾을 수 없습니다");
        assertThat(err1.getCode()).isEqualTo("NOT_FOUND");

        ErrorResponse err2 = ErrorResponse.of("UNAUTHORIZED", "인증이 필요합니다");
        assertThat(err2.getCode()).isEqualTo("UNAUTHORIZED");

        ErrorResponse err3 = ErrorResponse.of("FORBIDDEN", "권한이 없습니다");
        assertThat(err3.getCode()).isEqualTo("FORBIDDEN");
    }

    @Test
    @DisplayName("여러 필드 에러 처리")
    void multiple_fieldErrors() {
        // given
        List<ErrorResponse.FieldError> details = Arrays.asList(
                new ErrorResponse.FieldError("name", "이름 필수", ""),
                new ErrorResponse.FieldError("email", "이메일 형식 오류", "wrong"),
                new ErrorResponse.FieldError("age", "나이는 양수", -5),
                new ErrorResponse.FieldError("phone", "전화번호 형식 오류", "123")
        );

        // when
        ErrorResponse response = ErrorResponse.of("VALIDATION_ERROR", "입력 검증 실패", details);

        // then
        assertThat(response.getDetails()).hasSize(4);
        assertThat(response.getDetails()).extracting("field")
                .containsExactly("name", "email", "age", "phone");
    }

    @Test
    @DisplayName("FieldError - null rejectedValue")
    void fieldError_nullRejectedValue() {
        // when
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(
                "optionalField", "값이 비어있습니다", null
        );

        // then
        assertThat(fieldError.getRejectedValue()).isNull();
    }

    @Test
    @DisplayName("FieldError - 복잡한 객체 rejectedValue")
    void fieldError_complexRejectedValue() {
        // given
        List<String> complexValue = Arrays.asList("item1", "item2");

        // when
        ErrorResponse.FieldError fieldError = new ErrorResponse.FieldError(
                "items", "리스트가 비어있으면 안됩니다", complexValue
        );

        // then
        assertThat(fieldError.getRejectedValue()).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<String> rejectedList = (List<String>) fieldError.getRejectedValue();
        assertThat(rejectedList).containsExactly("item1", "item2");
    }

    @Test
    @DisplayName("빈 details 리스트")
    void empty_details() {
        // given
        List<ErrorResponse.FieldError> emptyDetails = Arrays.asList();

        // when
        ErrorResponse response = ErrorResponse.of("ERROR", "에러 발생", emptyDetails);

        // then
        assertThat(response.getDetails()).isEmpty();
    }
}
