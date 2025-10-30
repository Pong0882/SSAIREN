package com.ssairen.global.security.controller;

import com.ssairen.global.dto.ApiResponse;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import com.ssairen.global.security.dto.LoginRequest;
import com.ssairen.global.security.dto.RefreshRequest;
import com.ssairen.global.security.dto.TokenResponse;
import com.ssairen.global.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "인증 API", description = "로그인, 로그아웃, 토큰 갱신 등 인증 관련 API")
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
    @Operation(
            summary = "로그인",
            description = "구급대원(PARAMEDIC) 또는 병원(HOSPITAL) 계정으로 로그인합니다. " +
                    "성공 시 AccessToken(15분)과 RefreshToken(7일)을 발급받습니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "로그인 실패 - 잘못된 인증 정보",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
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
    @Operation(
            summary = "AccessToken 갱신",
            description = "RefreshToken을 사용하여 만료된 AccessToken을 갱신합니다. " +
                    "DB 조회 없이 Redis 캐시 정보를 사용하여 빠르게 처리됩니다. " +
                    "새로운 AccessToken과 기존 RefreshToken을 반환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "토큰 갱신 성공",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "RefreshToken이 유효하지 않거나 만료됨",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
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
//    @Operation(
//            summary = "로그아웃",
//            description = "현재 로그인된 사용자를 로그아웃합니다. " +
//                    "Redis에 저장된 RefreshToken을 삭제하여 토큰 갱신을 불가능하게 만듭니다. " +
//                    "Authorization 헤더에 유효한 AccessToken이 필요합니다.",
//            security = @SecurityRequirement(name = "bearer-jwt")
//    )
//    @ApiResponses(value = {
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "200",
//                    description = "로그아웃 성공",
//                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
//            ),
//            @io.swagger.v3.oas.annotations.responses.ApiResponse(
//                    responseCode = "401",
//                    description = "인증되지 않음 - AccessToken이 없거나 유효하지 않음",
//                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
//            )
//    })
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserPrincipal principal) {
        log.info("Logout request: userType={}, userId={}", principal.getUserType(), principal.getId());

        authService.logout(principal);

        return ApiResponse.success("로그아웃되었습니다");
    }
}
