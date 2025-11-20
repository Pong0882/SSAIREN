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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증 실패 시 처리 핸들러
 * Security 필터 체인에서 인증되지 않은 사용자가 보호된 리소스에 접근할 때 실행
 * GlobalExceptionHandler와 동일한 응답 형식 사용
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.warn("Authentication failed: {} - {}", request.getRequestURI(), authException.getMessage());

        // 기존 ErrorCode를 사용하여 일관된 에러 응답 생성
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.UNAUTHORIZED.getCode(),
                ErrorCode.UNAUTHORIZED.getMessage()
        );

        ApiResponse<Void> apiResponse = ApiResponse.error(
                errorResponse,
                ErrorCode.UNAUTHORIZED.getStatus().value()
        );

        // JSON 응답 설정 (GlobalExceptionHandler와 동일)
        response.setStatus(ErrorCode.UNAUTHORIZED.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        // 응답 본문 작성
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
