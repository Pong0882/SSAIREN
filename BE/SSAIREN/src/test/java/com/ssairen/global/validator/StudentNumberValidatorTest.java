package com.ssairen.global.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class StudentNumberValidatorTest {

    private StudentNumberValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new StudentNumberValidator();

        // Mock 설정 - 에러 메시지 빌더 체인 (lenient로 설정하여 불필요한 stubbing 허용)
        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        lenient().when(builder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    @DisplayName("유효한 학번 - 성공")
    void isValid_validStudentNumber() {
        // given
        String validNumber = "20230001";

        // when
        boolean result = validator.isValid(validNumber, context);

        // then
        assertThat(result).isTrue();
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("유효한 학번 - 1자리 숫자")
    void isValid_singleDigit() {
        // given
        String number = "1";

        // when
        boolean result = validator.isValid(number, context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유효한 학번 - 최대 길이 (20자)")
    void isValid_maxLength() {
        // given
        String number = "12345678901234567890"; // 20자

        // when
        boolean result = validator.isValid(number, context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유효한 학번 - 다양한 길이")
    void isValid_variousLengths() {
        // given
        String[] numbers = {
                "123",
                "12345",
                "202301",
                "20230001234"
        };

        for (String number : numbers) {
            // when
            boolean result = validator.isValid(number, context);

            // then
            assertThat(result).isTrue();
        }
    }

    @Test
    @DisplayName("학번이 null인 경우 - 실패")
    void isValid_nullStudentNumber() {
        // when
        boolean result = validator.isValid(null, context);

        // then
        assertThat(result).isFalse();
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("학번이 빈 문자열인 경우 - 실패")
    void isValid_emptyStudentNumber() {
        // when
        boolean result = validator.isValid("", context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("학번이 공백만 있는 경우 - 실패")
    void isValid_blankStudentNumber() {
        // when
        boolean result = validator.isValid("   ", context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("학번 길이 초과 (21자) - 실패")
    void isValid_tooLong() {
        // given
        String number = "123456789012345678901"; // 21자

        // when
        boolean result = validator.isValid(number, context);

        // then
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("학번은 20자 이내여야 합니다.");
    }

    @Test
    @DisplayName("학번에 영문 포함 - 실패")
    void isValid_containsLetter() {
        // given
        String number = "2023A001";

        // when
        boolean result = validator.isValid(number, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("학번은 숫자만 입력 가능합니다.");
    }

    @Test
    @DisplayName("학번에 특수문자 포함 - 실패")
    void isValid_containsSpecialCharacter() {
        // given
        String number = "2023-0001";

        // when
        boolean result = validator.isValid(number, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("학번은 숫자만 입력 가능합니다.");
    }

    @Test
    @DisplayName("학번에 공백 포함 - 실패")
    void isValid_containsSpace() {
        // given
        String number = "2023 0001";

        // when
        boolean result = validator.isValid(number, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("학번은 숫자만 입력 가능합니다.");
    }

    @Test
    @DisplayName("학번이 영문자로만 구성 - 실패")
    void isValid_onlyLetters() {
        // given
        String number = "ABCDEFG";

        // when
        boolean result = validator.isValid(number, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("학번은 숫자만 입력 가능합니다.");
    }

    @Test
    @DisplayName("학번에 소수점 포함 - 실패")
    void isValid_containsDecimal() {
        // given
        String number = "2023.001";

        // when
        boolean result = validator.isValid(number, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("학번은 숫자만 입력 가능합니다.");
    }

    @Test
    @DisplayName("학번에 음수 부호 포함 - 실패")
    void isValid_containsNegativeSign() {
        // given
        String number = "-20230001";

        // when
        boolean result = validator.isValid(number, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("학번은 숫자만 입력 가능합니다.");
    }

    @Test
    @DisplayName("학번에 양수 부호 포함 - 실패")
    void isValid_containsPositiveSign() {
        // given
        String number = "+20230001";

        // when
        boolean result = validator.isValid(number, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("학번은 숫자만 입력 가능합니다.");
    }

    @Test
    @DisplayName("학번에 한글 포함 - 실패")
    void isValid_containsKorean() {
        // given
        String number = "2023학번";

        // when
        boolean result = validator.isValid(number, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("학번은 숫자만 입력 가능합니다.");
    }
}
