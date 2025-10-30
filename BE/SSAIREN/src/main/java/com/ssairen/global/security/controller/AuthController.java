package com.ssairen.global.security.controller;

import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import com.ssairen.global.security.dto.LoginRequest;
import com.ssairen.global.security.dto.RefreshRequest;
import com.ssairen.global.security.dto.TokenResponse;
import com.ssairen.global.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 API 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 로그인
     *
     * @param request 로그인 요청 (userType, username, password)
     * @return TokenResponse (accessToken, refreshToken 등)
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request: userType={}, username={}", request.userType(), request.username());

        TokenResponse response = authService.login(request);

        return ApiResponse.success(response, "로그인에 성공했습니다");
    }

    /**
     * AccessToken 재발급
     *
     * @param request RefreshToken
     * @return TokenResponse (새로운 accessToken)
     */
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        log.info("Token refresh request");

        TokenResponse response = authService.refresh(request.refreshToken());

        return ApiResponse.success(response, "토큰이 갱신되었습니다");
    }

    /**
     * 로그아웃
     *
     * @param principal 현재 인증된 사용자
     * @return 성공 메시지
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserPrincipal principal) {
        log.info("Logout request: userType={}, userId={}", principal.getUserType(), principal.getId());

        authService.logout(principal);

        return ApiResponse.success("로그아웃되었습니다");
    }
}
