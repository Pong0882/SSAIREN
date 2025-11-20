package com.ssairen.global.utils;

import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CursorUtilsTest {

    @Test
    @DisplayName("유틸리티 클래스 인스턴스화 방지")
    void constructor_shouldThrowException() {
        assertThatThrownBy(() -> {
            var constructor = CursorUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(AssertionError.class);
    }

    @Test
    @DisplayName("ID를 Base64로 인코딩")
    void encodeCursor_withValidId_shouldReturnBase64String() {
        // given
        Long id = 12345L;

        // when
        String cursor = CursorUtils.encodeCursor(id);

        // then
        assertThat(cursor).isNotNull();
        assertThat(cursor).isNotEmpty();
        assertThat(cursor).doesNotContain("="); // URL-safe without padding
    }

    @Test
    @DisplayName("null ID 인코딩 - null 반환")
    void encodeCursor_withNullId_shouldReturnNull() {
        // when
        String cursor = CursorUtils.encodeCursor(null);

        // then
        assertThat(cursor).isNull();
    }

    @Test
    @DisplayName("Base64 커서를 디코딩하여 ID 반환")
    void decodeCursor_withValidCursor_shouldReturnId() {
        // given
        Long originalId = 12345L;
        String cursor = CursorUtils.encodeCursor(originalId);

        // when
        Long decodedId = CursorUtils.decodeCursor(cursor);

        // then
        assertThat(decodedId).isEqualTo(originalId);
    }

    @Test
    @DisplayName("null 커서 디코딩 - null 반환")
    void decodeCursor_withNullCursor_shouldReturnNull() {
        // when
        Long result = CursorUtils.decodeCursor(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("빈 문자열 커서 디코딩 - null 반환")
    void decodeCursor_withBlankCursor_shouldReturnNull() {
        // when & then
        assertThat(CursorUtils.decodeCursor("")).isNull();
        assertThat(CursorUtils.decodeCursor("   ")).isNull();
    }

    @Test
    @DisplayName("유효하지 않은 Base64 커서 디코딩 - 예외 발생")
    void decodeCursor_withInvalidCursor_shouldThrowException() {
        // given
        String invalidCursor = "invalid-cursor-!@#$%";

        // when & then
        assertThatThrownBy(() -> CursorUtils.decodeCursor(invalidCursor))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CURSOR);
    }

    @Test
    @DisplayName("인코딩-디코딩 왕복 테스트")
    void encodeDecode_roundTrip_shouldReturnOriginalValue() {
        // given
        Long[] testIds = {1L, 100L, 999999L, Long.MAX_VALUE};

        for (Long id : testIds) {
            // when
            String cursor = CursorUtils.encodeCursor(id);
            Long decoded = CursorUtils.decodeCursor(cursor);

            // then
            assertThat(decoded).isEqualTo(id);
        }
    }

    @Test
    @DisplayName("다른 ID는 다른 커서 생성")
    void encodeCursor_differentIds_shouldProduceDifferentCursors() {
        // given
        Long id1 = 100L;
        Long id2 = 200L;

        // when
        String cursor1 = CursorUtils.encodeCursor(id1);
        String cursor2 = CursorUtils.encodeCursor(id2);

        // then
        assertThat(cursor1).isNotEqualTo(cursor2);
    }

    @Test
    @DisplayName("URL-safe Base64 인코딩 사용")
    void encodeCursor_shouldUseUrlSafeEncoding() {
        // given
        Long id = 12345L;

        // when
        String cursor = CursorUtils.encodeCursor(id);

        // then
        // URL-safe Base64는 +, /, = 문자를 사용하지 않음
        assertThat(cursor).doesNotContain("+", "/", "=");
    }
}
