package com.ssairen.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.dto.ErrorResponse;
import com.ssairen.global.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 권한 부족 시 처리 핸들러
 * Security 필터 체인에서 인가되지 않은 사용자가 접근할 때 실행
 * GlobalExceptionHandler와 동일한 응답 형식 사용
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("Access denied: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());

        // 기존 ErrorCode를 사용하여 일관된 에러 응답 생성
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.ACCESS_DENIED.getCode(),
                ErrorCode.ACCESS_DENIED.getMessage()
        );

        ApiResponse<Void> apiResponse = ApiResponse.error(
                errorResponse,
                ErrorCode.ACCESS_DENIED.getStatus().value()
        );

        // JSON 응답 설정 (GlobalExceptionHandler와 동일)
        response.setStatus(ErrorCode.ACCESS_DENIED.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 응답 본문 작성
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}