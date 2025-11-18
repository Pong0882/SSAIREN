package com.ssairen.global.utils;

import com.ssairen.global.annotation.ExcludeFromLogging;
import com.ssairen.global.annotation.Sensitive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaskingUtilsTest {

    @Test
    @DisplayName("유틸리티 클래스 인스턴스화 방지")
    void constructor_shouldThrowException() {
        assertThatThrownBy(() -> {
            var constructor = MaskingUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("null 객체 마스킹 - null 반환")
    void mask_withNullObject_shouldReturnNullText() {
        // when
        String result = MaskingUtils.mask(null);

        // then
        assertThat(result).isEqualTo("null");
    }

    @Test
    @DisplayName("단순 타입 마스킹 - 문자열 그대로 반환")
    void mask_withSimpleTypes_shouldReturnStringValue() {
        // given & when & then
        assertThat(MaskingUtils.mask("test")).isEqualTo("test");
        assertThat(MaskingUtils.mask(123)).isEqualTo("123");
        assertThat(MaskingUtils.mask(45.67)).isEqualTo("45.67");
        assertThat(MaskingUtils.mask(true)).isEqualTo("true");
    }

    @Test
    @DisplayName("일반 객체 마스킹 - 필드 정보 포함")
    void mask_withRegularObject_shouldIncludeFieldInfo() {
        // given
        TestObject obj = new TestObject("testValue", 42);

        // when
        String result = MaskingUtils.mask(obj);

        // then
        assertThat(result).contains("TestObject[");
        assertThat(result).contains("normalField=testValue");
        assertThat(result).contains("numberField=42");
    }

    @Test
    @DisplayName("@Sensitive 필드 마스킹 - 별표로 표시")
    void mask_withSensitiveField_shouldMaskValue() {
        // given
        SensitiveObject obj = new SensitiveObject("public", "secret");

        // when
        String result = MaskingUtils.mask(obj);

        // then
        assertThat(result).contains("publicField=public");
        assertThat(result).contains("sensitiveField=****");
        assertThat(result).doesNotContain("secret");
    }

    @Test
    @DisplayName("@ExcludeFromLogging 필드 - 제외 표시")
    void mask_withExcludedField_shouldShowExcluded() {
        // given
        ExcludedObject obj = new ExcludedObject("normal", "excluded");

        // when
        String result = MaskingUtils.mask(obj);

        // then
        assertThat(result).contains("normalField=normal");
        assertThat(result).contains("excludedField=<excluded>");
        assertThat(result).doesNotContain("excluded value");
    }

    @Test
    @DisplayName("복합 객체 마스킹 - 모든 어노테이션 처리")
    void mask_withComplexObject_shouldHandleAllAnnotations() {
        // given
        ComplexObject obj = new ComplexObject("normal", "sensitive", "excluded");

        // when
        String result = MaskingUtils.mask(obj);

        // then
        assertThat(result).contains("normalField=normal");
        assertThat(result).contains("sensitiveField=****");
        assertThat(result).contains("excludedField=<excluded>");
    }

    // 테스트용 클래스들
    static class TestObject {
        private String normalField;
        private Integer numberField;

        TestObject(String normalField, Integer numberField) {
            this.normalField = normalField;
            this.numberField = numberField;
        }
    }

    static class SensitiveObject {
        private String publicField;

        @Sensitive
        private String sensitiveField;

        SensitiveObject(String publicField, String sensitiveField) {
            this.publicField = publicField;
            this.sensitiveField = sensitiveField;
        }
    }

    static class ExcludedObject {
        private String normalField;

        @ExcludeFromLogging
        private String excludedField;

        ExcludedObject(String normalField, String excludedField) {
            this.normalField = normalField;
            this.excludedField = excludedField;
        }
    }

    static class ComplexObject {
        private String normalField;

        @Sensitive
        private String sensitiveField;

        @ExcludeFromLogging
        private String excludedField;

        ComplexObject(String normalField, String sensitiveField, String excludedField) {
            this.normalField = normalField;
            this.sensitiveField = sensitiveField;
            this.excludedField = excludedField;
        }
    }
}
