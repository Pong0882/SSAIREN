package com.ssairen.global.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 비밀번호 유효성 검증기
 */
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;

    // 최소 하나의 영문, 숫자, 특수문자 포함
    private static final String PASSWORD_PATTERN =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{" + MIN_LENGTH + "," + MAX_LENGTH + "}$";

    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false;
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("비밀번호는 %d자 이상 %d자 이하여야 합니다.", MIN_LENGTH, MAX_LENGTH)
            ).addConstraintViolation();
            return false;
        }

        if (!pattern.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "비밀번호는 영문, 숫자, 특수문자(@$!%*#?&)를 각각 최소 1개씩 포함해야 합니다."
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
