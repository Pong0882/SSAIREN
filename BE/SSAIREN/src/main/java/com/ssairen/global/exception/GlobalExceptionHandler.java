package com.ssairen.global.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 전역 예외 핸들러
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * CustomException 처리
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        log.error("CustomException occurred: code={}, message={}", e.getErrorCode().getCode(), e.getMessage(), e);

        ErrorCode errorCode = e.getErrorCode();
        String message = e.getDetailMessage() != null ? e.getDetailMessage() : errorCode.getMessage();
        ErrorResponse errorResponse = ErrorResponse.of(errorCode.getCode(), message);

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorResponse, errorCode.getStatus().value()));
    }

    /**
     * @Valid 검증 실패 (@RequestBody)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.VALIDATION_ERROR.getCode(),
                ErrorCode.VALIDATION_ERROR.getMessage(),
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse, HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * @Valid 검증 실패 (@ModelAttribute)
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(BindException e) {
        log.warn("Bind validation failed: {}", e.getMessage());

        List<ErrorResponse.FieldError> fieldErrors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> ErrorResponse.FieldError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.VALIDATION_ERROR.getCode(),
                ErrorCode.VALIDATION_ERROR.getMessage(),
                fieldErrors
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse, HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * 필수 파라미터 누락
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.warn("Missing required parameter: {}", e.getParameterName());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.MISSING_REQUEST_PARAMETER.getCode(),
                String.format("필수 파라미터 '%s'가 누락되었습니다.", e.getParameterName())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse, HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * 파라미터 타입 불일치
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn("Type mismatch for parameter: {}", e.getName());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_TYPE_VALUE.getCode(),
                String.format("'%s' 파라미터의 타입이 올바르지 않습니다. 입력값: '%s'", e.getName(), e.getValue())
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse, HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * JSON 파싱 실패
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("JSON parse error: {}", e.getMessage());

        String message = ErrorCode.INVALID_JSON_FORMAT.getMessage();

        // InvalidFormatException인 경우 더 상세한 메시지 제공
        if (e.getCause() instanceof InvalidFormatException) {
            InvalidFormatException cause = (InvalidFormatException) e.getCause();
            message = String.format("필드 '%s'의 값이 올바르지 않습니다. 입력값: '%s'",
                    cause.getPath().get(0).getFieldName(),
                    cause.getValue());
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_JSON_FORMAT.getCode(),
                message
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(errorResponse, HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * 지원하지 않는 HTTP 메소드
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.warn("Method not allowed: {}", e.getMethod());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                String.format("'%s' 메소드는 지원하지 않습니다.", e.getMethod())
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(errorResponse, HttpStatus.METHOD_NOT_ALLOWED.value()));
    }

    /**
     * Spring Security AuthenticationException 처리 (인증 실패)
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED.getCode(),
                ErrorCode.UNAUTHORIZED.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(errorResponse, HttpStatus.UNAUTHORIZED.value()));
    }

    /**
     * Spring Security AccessDeniedException 처리 (권한 부족)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("Access denied: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.ACCESS_DENIED.getCode(),
                ErrorCode.ACCESS_DENIED.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(errorResponse, HttpStatus.FORBIDDEN.value()));
    }

    /**
     * 모든 예외의 최종 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception occurred", e);

        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}