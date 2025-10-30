package com.ssairen.global.security.jwt;

import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import com.ssairen.global.security.config.JwtProperties;
import com.ssairen.global.security.enums.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성 및 검증 제공자
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    /**
     * SecretKey 초기화
     */
    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * AccessToken 생성
     *
     * @param userId      사용자 ID
     * @param username    사용자명 (studentNumber 또는 hospitalId)
     * @param userType    사용자 타입
     * @param authorities 권한 목록
     * @return AccessToken
     */
    public String generateAccessToken(Integer userId, String username, UserType userType,
                                      Collection<? extends GrantedAuthority> authorities) {
        // subject: "PARAMEDIC:123" 형태
        String subject = userType.name() + ":" + userId;

        // authorities를 문자열 리스트로 변환
        String authoritiesStr = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(subject)
                .claim("username", username)
                .claim("userType", userType.name())
                .claim("authorities", authoritiesStr)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * RefreshToken 생성 (claims 최소화)
     *
     * @param userId   사용자 ID
     * @param userType 사용자 타입
     * @return RefreshToken
     */
    public String generateRefreshToken(Integer userId, UserType userType) {
        String subject = userType.name() + ":" + userId;

        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(subject)
                .claim("userType", userType.name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * JWT 검증 및 파싱
     *
     * @param token JWT 토큰
     * @return Claims
     * @throws CustomException 토큰이 유효하지 않은 경우
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (Exception e) {
            log.error("JWT parsing error: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    /**
     * JWT에서 사용자 정보 추출
     *
     * @param token JWT 토큰
     * @return UserInfo
     */
    public UserInfo extractUserInfo(String token) {
        Claims claims = parseToken(token);

        // subject 파싱: "PARAMEDIC:123" → ["PARAMEDIC", "123"]
        String subject = claims.getSubject();
        String[] parts = subject.split(":");

        if (parts.length != 2) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "잘못된 토큰 형식입니다");
        }

        UserType userType = UserType.valueOf(parts[0]);
        Integer userId = Integer.parseInt(parts[1]);
        String username = claims.get("username", String.class);

        return new UserInfo(userId, username, userType);
    }

    /**
     * JWT 유효성 검증만 수행 (파싱 없이)
     *
     * @param token JWT 토큰
     * @return 유효하면 true
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (CustomException e) {
            return false;
        }
    }

    /**
     * 사용자 정보 DTO
     */
    @Getter
    @RequiredArgsConstructor
    public static class UserInfo {
        private final Integer userId;
        private final String username;
        private final UserType userType;
    }
}
