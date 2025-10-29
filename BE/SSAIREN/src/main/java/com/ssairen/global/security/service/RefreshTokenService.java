package com.ssairen.global.security.service;

import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import com.ssairen.global.security.config.JwtProperties;
import com.ssairen.global.security.entity.RefreshToken;
import com.ssairen.global.security.enums.UserType;
import com.ssairen.global.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * RefreshToken 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    /**
     * RefreshToken 저장 (기존 토큰 있으면 덮어쓰기)
     *
     * @param userId   사용자 ID
     * @param userType 사용자 타입
     * @param token    RefreshToken 문자열
     */
    public void save(Integer userId, UserType userType, String token) {
        log.debug("Saving refresh token for user: userId={}, userType={}", userId, userType);

        // 기존 RefreshToken 삭제 (한 사용자당 하나의 RefreshToken만 유지)
        refreshTokenRepository.findByUserIdAndUserType(userId, userType)
                .ifPresent(refreshTokenRepository::delete);

        // 새로운 RefreshToken 저장
        LocalDateTime expiryDate = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .userId(userId)
                .userType(userType)
                .expiryDate(expiryDate)
                .build();

        refreshTokenRepository.save(refreshToken);
        log.info("Refresh token saved successfully: userId={}, userType={}", userId, userType);
    }

    /**
     * RefreshToken 조회 및 검증
     *
     * @param token RefreshToken 문자열
     * @return RefreshToken 엔티티
     * @throws CustomException 토큰을 찾을 수 없거나 만료된 경우
     */
    @Transactional(readOnly = true)
    public RefreshToken findByToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 만료 여부 확인
        if (refreshToken.isExpired()) {
            log.warn("Expired refresh token: token={}", token);
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        return refreshToken;
    }

    /**
     * 사용자 ID와 타입으로 RefreshToken 삭제 (로그아웃)
     *
     * @param userId   사용자 ID
     * @param userType 사용자 타입
     */
    public void deleteByUserIdAndUserType(Integer userId, UserType userType) {
        log.debug("Deleting refresh token: userId={}, userType={}", userId, userType);
        refreshTokenRepository.deleteByUserIdAndUserType(userId, userType);
        log.info("Refresh token deleted successfully: userId={}, userType={}", userId, userType);
    }

    /**
     * 토큰 문자열로 RefreshToken 삭제
     *
     * @param token RefreshToken 문자열
     */
    public void deleteByToken(String token) {
        log.debug("Deleting refresh token by token string");
        refreshTokenRepository.deleteByToken(token);
        log.info("Refresh token deleted successfully");
    }

    /**
     * 만료된 RefreshToken 정리 (스케줄러에서 호출)
     */
    public void deleteExpiredTokens() {
        log.debug("Cleaning up expired refresh tokens");
        refreshTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        log.info("Expired refresh tokens cleaned up");
    }
}
