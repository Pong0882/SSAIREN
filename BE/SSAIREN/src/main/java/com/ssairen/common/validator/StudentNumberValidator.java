package com.ssairen.common.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 학번 유효성 검증기
 */
public class StudentNumberValidator implements ConstraintValidator<ValidStudentNumber, String> {

    private static final int MAX_LENGTH = 20;

    // 학번은 숫자로만 구성 (유연하게 처리하려면 패턴 수정 가능)
    private static final String STUDENT_NUMBER_PATTERN = "^[0-9]{1," + MAX_LENGTH + "}$";

    private static final Pattern pattern = Pattern.compile(STUDENT_NUMBER_PATTERN);

    @Override
    public boolean isValid(String studentNumber, ConstraintValidatorContext context) {
        if (studentNumber == null || studentNumber.isBlank()) {
            return false;
        }

        if (studentNumber.length() > MAX_LENGTH) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("학번은 %d자 이내여야 합니다.", MAX_LENGTH)
            ).addConstraintViolation();
            return false;
        }

        if (!pattern.matcher(studentNumber).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "학번은 숫자만 입력 가능합니다."
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}