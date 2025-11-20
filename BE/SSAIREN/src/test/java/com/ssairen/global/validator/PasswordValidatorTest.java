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
class PasswordValidatorTest {

    private PasswordValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder builder;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();

        // Mock 설정 - 에러 메시지 빌더 체인 (lenient로 설정하여 불필요한 stubbing 허용)
        lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        lenient().when(builder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    @DisplayName("유효한 비밀번호 - 성공")
    void isValid_validPassword() {
        // given
        String validPassword = "Password1!";

        // when
        boolean result = validator.isValid(validPassword, context);

        // then
        assertThat(result).isTrue();
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("유효한 비밀번호 - 최소 길이 (8자)")
    void isValid_minimumLength() {
        // given
        String password = "Pass123!";

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유효한 비밀번호 - 긴 비밀번호 (50자)")
    void isValid_longPassword() {
        // given
        String password = "A1!" + "a".repeat(47); // 총 50자

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유효한 비밀번호 - 모든 특수문자 포함")
    void isValid_allSpecialCharacters() {
        // given
        String[] passwords = {
                "Password1@",
                "Password1$",
                "Password1!",
                "Password1%",
                "Password1*",
                "Password1#",
                "Password1?",
                "Password1&"
        };

        for (String password : passwords) {
            // when
            boolean result = validator.isValid(password, context);

            // then
            assertThat(result).isTrue();
        }
    }

    @Test
    @DisplayName("비밀번호가 null인 경우 - 실패")
    void isValid_nullPassword() {
        // when
        boolean result = validator.isValid(null, context);

        // then
        assertThat(result).isFalse();
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    @DisplayName("비밀번호가 빈 문자열인 경우 - 실패")
    void isValid_emptyPassword() {
        // when
        boolean result = validator.isValid("", context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("비밀번호가 공백만 있는 경우 - 실패")
    void isValid_blankPassword() {
        // when
        boolean result = validator.isValid("   ", context);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("비밀번호 길이 부족 (7자) - 실패")
    void isValid_tooShort() {
        // given
        String password = "Pass12!";

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("비밀번호는 8자 이상 100자 이하여야 합니다.");
    }

    @Test
    @DisplayName("비밀번호 길이 초과 (101자) - 실패")
    void isValid_tooLong() {
        // given
        String password = "A1!" + "a".repeat(98); // 총 101자

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isFalse();
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("비밀번호는 8자 이상 100자 이하여야 합니다.");
    }

    @Test
    @DisplayName("영문이 없는 경우 - 실패")
    void isValid_noLetter() {
        // given
        String password = "12345678!";

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("비밀번호는 영문, 숫자, 특수문자(@$!%*#?&)를 각각 최소 1개씩 포함해야 합니다.");
    }

    @Test
    @DisplayName("숫자가 없는 경우 - 실패")
    void isValid_noDigit() {
        // given
        String password = "Password!";

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("비밀번호는 영문, 숫자, 특수문자(@$!%*#?&)를 각각 최소 1개씩 포함해야 합니다.");
    }

    @Test
    @DisplayName("특수문자가 없는 경우 - 실패")
    void isValid_noSpecialCharacter() {
        // given
        String password = "Password1";

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("비밀번호는 영문, 숫자, 특수문자(@$!%*#?&)를 각각 최소 1개씩 포함해야 합니다.");
    }

    @Test
    @DisplayName("허용되지 않는 특수문자 포함 - 실패")
    void isValid_invalidSpecialCharacter() {
        // given
        String password = "Password1^"; // ^ 는 허용되지 않음

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("비밀번호는 영문, 숫자, 특수문자(@$!%*#?&)를 각각 최소 1개씩 포함해야 합니다.");
    }

    @Test
    @DisplayName("공백 포함 - 실패")
    void isValid_containsSpace() {
        // given
        String password = "Pass word1!";

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isFalse();
        verify(context).buildConstraintViolationWithTemplate("비밀번호는 영문, 숫자, 특수문자(@$!%*#?&)를 각각 최소 1개씩 포함해야 합니다.");
    }

    @Test
    @DisplayName("대소문자 혼합 비밀번호 - 성공")
    void isValid_mixedCase() {
        // given
        String password = "PaSsWoRd1!";

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("소문자만 사용한 비밀번호 - 성공")
    void isValid_lowercaseOnly() {
        // given
        String password = "password1!";

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("대문자만 사용한 비밀번호 - 성공")
    void isValid_uppercaseOnly() {
        // given
        String password = "PASSWORD1!";

        // when
        boolean result = validator.isValid(password, context);

        // then
        assertThat(result).isTrue();
    }
}
