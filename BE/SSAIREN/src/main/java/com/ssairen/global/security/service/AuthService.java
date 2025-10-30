package com.ssairen.global.security.service;

import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import com.ssairen.global.security.converter.TokenResponseConverter;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import com.ssairen.global.security.dto.LoginRequest;
import com.ssairen.global.security.dto.TokenResponse;
import com.ssairen.global.security.enums.UserType;
import com.ssairen.global.security.jwt.JwtTokenProvider;
import com.ssairen.global.security.service.RefreshTokenService.RefreshTokenInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

/**
 * 인증 서비스
 * 로그인, 로그아웃, 토큰 갱신 등의 인증 관련 비즈니스 로직 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final ParamedicUserDetailsService paramedicUserDetailsService;
    private final HospitalUserDetailsService hospitalUserDetailsService;
    private final ParamedicRepository paramedicRepository;
    private final TokenResponseConverter tokenResponseConverter;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    /**
     * 로그인
     *
     * @param request 로그인 요청 (userType, username, password)
     * @return TokenResponse (accessToken, refreshToken 등)
     * @throws CustomException 인증 실패 시
     */
    public TokenResponse login(LoginRequest request) {
        log.debug("Login attempt: userType={}, username={}", request.userType(), request.username());

        // 1. userType에 따라 UserDetailsService 선택
        UserDetails userDetails = loadUserByTypeAndUsername(request.userType(), request.username());

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), userDetails.getPassword())) {
            log.warn("Invalid password for user: userType={}, username={}", request.userType(), request.username());
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        CustomUserPrincipal principal = (CustomUserPrincipal) userDetails;

        // 3. JWT 생성
        String accessToken = jwtTokenProvider.generateAccessToken(
                principal.getId(),
                principal.getUsername(),
                principal.getUserType(),
                principal.getAuthorities()
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(
                principal.getId(),
                principal.getUserType()
        );

        // 4. authorities를 문자열로 변환
        String authoritiesStr = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 5. RefreshToken Redis 저장 (username과 authorities 포함)
        refreshTokenService.save(
                principal.getId(),
                principal.getUserType(),
                principal.getUsername(),
                authoritiesStr,
                refreshToken
        );

        log.info("Login successful: userType={}, userId={}, username={}",
                principal.getUserType(), principal.getId(), principal.getUsername());

        // 5. 구급대원인 경우 상세 정보 포함
        if (principal.getUserType() == UserType.PARAMEDIC) {
            Paramedic paramedic = paramedicRepository.findById(principal.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PARAMEDIC_NOT_FOUND));

            return tokenResponseConverter.toTokenResponseWithParamedic(
                    accessToken,
                    refreshToken,
                    paramedic
            );
        }

        // 6. 그 외 사용자는 기본 정보만 반환
        return tokenResponseConverter.toTokenResponse(
                accessToken,
                refreshToken,
                principal.getUserType(),
                principal.getId(),
                principal.getUsername()
        );
    }

    /**
     * RefreshToken으로 AccessToken 재발급
     * - DB 조회 없이 Redis의 캐시된 정보 사용 (성능 최적화)
     *
     * @param refreshTokenStr RefreshToken 문자열
     * @return TokenResponse (새로운 accessToken 포함)
     * @throws CustomException RefreshToken이 유효하지 않은 경우
     */
    public TokenResponse refresh(String refreshTokenStr) {
        log.debug("Token refresh attempt");

        // 1. Redis에서 RefreshToken 조회 및 검증 (username, authorities 포함)
        RefreshTokenInfo info = refreshTokenService.findByToken(refreshTokenStr);

        // 2. authorities 문자열을 파싱하여 Collection으로 변환
        // JwtAuthenticationFilter.parseAuthorities() 로직과 동일
        String[] authArray = info.authorities().split(",");
        var authorities = java.util.Arrays.stream(authArray)
                .map(String::trim)
                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // 3. 새로운 AccessToken 생성 (DB 조회 없이 Redis 정보 사용)
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                info.userId(),
                info.username(),
                info.userType(),
                authorities
        );

        log.info("Token refreshed successfully: userType={}, userId={}, username={}",
                info.userType(), info.userId(), info.username());

        // 4. 구급대원인 경우 상세 정보 포함
        if (info.userType() == UserType.PARAMEDIC) {
            Paramedic paramedic = paramedicRepository.findById(info.userId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PARAMEDIC_NOT_FOUND));

            return tokenResponseConverter.toTokenResponseWithParamedic(
                    newAccessToken,
                    refreshTokenStr,  // RefreshToken은 그대로 유지
                    paramedic
            );
        }

        // 5. 그 외 사용자는 기본 정보만 반환
        return tokenResponseConverter.toTokenResponse(
                newAccessToken,
                refreshTokenStr,  // RefreshToken은 그대로 유지
                info.userType(),
                info.userId(),
                info.username()
        );
    }

    /**
     * 로그아웃
     *
     * @param principal 현재 인증된 사용자
     */
    public void logout(CustomUserPrincipal principal) {
        log.debug("Logout attempt: userType={}, userId={}", principal.getUserType(), principal.getId());

        // RefreshToken 삭제
        refreshTokenService.deleteByUserIdAndUserType(principal.getId(), principal.getUserType());

        log.info("Logout successful: userType={}, userId={}", principal.getUserType(), principal.getId());
    }

    /**
     * userType과 username으로 사용자 조회
     */
    private UserDetails loadUserByTypeAndUsername(UserType userType, String username) {
        if (userType == UserType.PARAMEDIC) {
            return paramedicUserDetailsService.loadUserByUsername(username);
        } else {
            return hospitalUserDetailsService.loadUserByUsername(username);
        }
    }
}
