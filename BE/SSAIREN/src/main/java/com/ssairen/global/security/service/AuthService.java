package com.ssairen.global.security.service;

import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import com.ssairen.global.security.converter.TokenResponseConverter;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import com.ssairen.global.security.dto.LoginRequest;
import com.ssairen.global.security.dto.TokenResponse;
import com.ssairen.global.security.entity.RefreshToken;
import com.ssairen.global.security.enums.UserType;
import com.ssairen.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 4. RefreshToken DB 저장
        refreshTokenService.save(principal.getId(), principal.getUserType(), refreshToken);

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
     *
     * @param refreshTokenStr RefreshToken 문자열
     * @return TokenResponse (새로운 accessToken 포함)
     * @throws CustomException RefreshToken이 유효하지 않은 경우
     */
    public TokenResponse refresh(String refreshTokenStr) {
        log.debug("Token refresh attempt");

        // 1. DB에서 RefreshToken 조회 및 검증
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr);

        // 2. RefreshToken에서 사용자 정보 추출
        Integer userId = refreshToken.getUserId();
        UserType userType = refreshToken.getUserType();

        // 3. 사용자 정보 재조회
        UserDetails userDetails = loadUserByTypeAndUserId(userType, userId);
        CustomUserPrincipal principal = (CustomUserPrincipal) userDetails;

        // 4. 새로운 AccessToken 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                principal.getId(),
                principal.getUsername(),
                principal.getUserType(),
                principal.getAuthorities()
        );

        log.info("Token refreshed successfully: userType={}, userId={}", userType, userId);

        // 5. 구급대원인 경우 상세 정보 포함
        if (principal.getUserType() == UserType.PARAMEDIC) {
            Paramedic paramedic = paramedicRepository.findById(principal.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PARAMEDIC_NOT_FOUND));

            return tokenResponseConverter.toTokenResponseWithParamedic(
                    newAccessToken,
                    refreshTokenStr,  // RefreshToken은 그대로 유지
                    paramedic
            );
        }

        // 6. 그 외 사용자는 기본 정보만 반환
        return tokenResponseConverter.toTokenResponse(
                newAccessToken,
                refreshTokenStr,  // RefreshToken은 그대로 유지
                principal.getUserType(),
                principal.getId(),
                principal.getUsername()
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

    /**
     * userType과 userId로 사용자 조회
     */
    private UserDetails loadUserByTypeAndUserId(UserType userType, Integer userId) {
        if (userType == UserType.PARAMEDIC) {
            return paramedicUserDetailsService.loadUserById(userId);
        } else {
            return hospitalUserDetailsService.loadUserById(userId);
        }
    }
}
