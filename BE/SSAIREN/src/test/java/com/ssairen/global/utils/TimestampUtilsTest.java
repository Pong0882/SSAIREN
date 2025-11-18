package com.ssairen.global.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimestampUtilsTest {

    @Test
    @DisplayName("유틸리티 클래스 인스턴스화 방지")
    void constructor_shouldThrowException() {
        assertThatThrownBy(() -> {
            var constructor = TimestampUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("현재 시간을 ISO-8601 형식 문자열로 반환")
    void now_shouldReturnIso8601FormattedString() {
        // when
        String result = TimestampUtils.now();

        // then
        assertThat(result).isNotNull();
        assertThat(result).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z");
    }

    @Test
    @DisplayName("현재 시간을 밀리초로 반환")
    void currentTimeMillis_shouldReturnCurrentTimeInMillis() {
        // given
        long before = System.currentTimeMillis();

        // when
        long result = TimestampUtils.currentTimeMillis();

        // then
        long after = System.currentTimeMillis();
        assertThat(result).isGreaterThanOrEqualTo(before);
        assertThat(result).isLessThanOrEqualTo(after);
    }

    @Test
    @DisplayName("연속 호출 시 다른 밀리초 값 반환")
    void currentTimeMillis_consecutiveCalls_shouldReturnDifferentOrEqualValues() throws InterruptedException {
        // when
        long first = TimestampUtils.currentTimeMillis();
        Thread.sleep(10); // 10ms 대기
        long second = TimestampUtils.currentTimeMillis();

        // then
        assertThat(second).isGreaterThanOrEqualTo(first);
    }

    @Test
    @DisplayName("ISO-8601 문자열이 유효한 Instant로 파싱 가능")
    void now_returnedValue_shouldBeParsableAsInstant() {
        // when
        String timestamp = TimestampUtils.now();

        // then
        assertThat(Instant.parse(timestamp)).isNotNull();
    }

    @Test
    @DisplayName("ISO-8601 문자열에 밀리초 포함")
    void now_shouldIncludeMilliseconds() {
        // when
        String result = TimestampUtils.now();

        // then
        // 예: 2024-01-15T10:30:45.123Z
        assertThat(result).contains(".");
        String[] parts = result.split("\\.");
        assertThat(parts).hasSize(2);
        assertThat(parts[1]).matches("\\d{3}Z");
    }
}
