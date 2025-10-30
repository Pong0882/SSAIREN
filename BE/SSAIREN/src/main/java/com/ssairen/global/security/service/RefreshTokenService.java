package com.ssairen.global.security.service;

import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import com.ssairen.global.security.config.JwtProperties;
import com.ssairen.global.security.enums.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * RefreshToken 관리 서비스 (Redis 기반)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String TOKEN_TO_USER_PREFIX = "token_to_user:";

    /**
     * RefreshToken 저장 (기존 토큰 있으면 덮어쓰기)
     * Redis에 두 개의 키로 저장:
     * 1. refresh_token:{userId}:{userType} -> token (사용자로 토큰 조회용)
     * 2. token_to_user:{token} -> userId:userType (토큰으로 사용자 조회용)
     *
     * @param userId   사용자 ID
     * @param userType 사용자 타입
     * @param token    RefreshToken 문자열
     */
    public void save(Integer userId, UserType userType, String token) {
        log.debug("Saving refresh token for user: userId={}, userType={}", userId, userType);

        String userKey = REFRESH_TOKEN_PREFIX + userId + ":" + userType.name();
        String tokenKey = TOKEN_TO_USER_PREFIX + token;
        String userValue = userId + ":" + userType.name();

        // TTL: milliseconds를 seconds로 변환
        long ttlSeconds = jwtProperties.getRefreshTokenExpiration() / 1000;

        // 1. 사용자 키로 토큰 저장 (한 사용자당 하나의 토큰만 유지)
        redisTemplate.opsForValue().set(userKey, token, ttlSeconds, TimeUnit.SECONDS);

        // 2. 토큰으로 사용자 정보 저장 (토큰으로 검증 시 사용)
        redisTemplate.opsForValue().set(tokenKey, userValue, ttlSeconds, TimeUnit.SECONDS);

        log.info("Refresh token saved successfully in Redis: userId={}, userType={}", userId, userType);
    }

    /**
     * RefreshToken 조회 및 검증
     * Redis에서 토큰이 존재하면 유효한 것으로 간주 (TTL로 자동 만료 처리)
     *
     * @param token RefreshToken 문자열
     * @return RefreshTokenInfo (userId, userType)
     * @throws CustomException 토큰을 찾을 수 없는 경우 (만료 포함)
     */
    public RefreshTokenInfo findByToken(String token) {
        String tokenKey = TOKEN_TO_USER_PREFIX + token;
        String userValue = redisTemplate.opsForValue().get(tokenKey);

        if (userValue == null) {
            log.warn("Refresh token not found or expired in Redis");
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        // userValue 형식: "userId:userType"
        String[] parts = userValue.split(":");
        if (parts.length != 2) {
            log.error("Invalid user value format in Redis: {}", userValue);
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 파싱 중 예외 처리
        Integer userId;
        UserType userType;
        try {
            userId = Integer.parseInt(parts[0]);
            userType = UserType.valueOf(parts[1]);
        } catch (NumberFormatException e) {
            log.error("Invalid userId format in Redis: {}", parts[0]);
            throw new CustomException(ErrorCode.INVALID_TOKEN, "Redis에 저장된 userId 형식이 올바르지 않습니다");
        } catch (IllegalArgumentException e) {
            log.error("Invalid userType in Redis: {}", parts[1]);
            throw new CustomException(ErrorCode.INVALID_TOKEN, "Redis에 저장된 userType이 올바르지 않습니다");
        }

        log.debug("Refresh token found in Redis: userId={}, userType={}", userId, userType);
        return new RefreshTokenInfo(userId, userType);
    }

    /**
     * RefreshToken 정보를 담는 레코드
     */
    public record RefreshTokenInfo(Integer userId, UserType userType) {
    }

    /**
     * 사용자 ID와 타입으로 RefreshToken 삭제 (로그아웃)
     * Redis에서 두 개의 키를 삭제:
     * 1. refresh_token:{userId}:{userType}
     * 2. token_to_user:{token} (먼저 토큰을 조회해서 삭제)
     *
     * @param userId   사용자 ID
     * @param userType 사용자 타입
     */
    public void deleteByUserIdAndUserType(Integer userId, UserType userType) {
        log.debug("Deleting refresh token: userId={}, userType={}", userId, userType);

        String userKey = REFRESH_TOKEN_PREFIX + userId + ":" + userType.name();

        // 1. 사용자 키로 토큰 조회
        String token = redisTemplate.opsForValue().get(userKey);

        // 2. 사용자 키 삭제
        redisTemplate.delete(userKey);

        // 3. 토큰 키도 삭제 (토큰이 존재하는 경우)
        if (token != null) {
            String tokenKey = TOKEN_TO_USER_PREFIX + token;
            redisTemplate.delete(tokenKey);
        }

        log.info("Refresh token deleted successfully from Redis: userId={}, userType={}", userId, userType);
    }

    /**
     * 토큰 문자열로 RefreshToken 삭제
     * Redis에서 두 개의 키를 삭제:
     * 1. token_to_user:{token}
     * 2. refresh_token:{userId}:{userType} (먼저 사용자 정보를 조회해서 삭제)
     *
     * @param token RefreshToken 문자열
     */
    public void deleteByToken(String token) {
        log.debug("Deleting refresh token by token string");

        String tokenKey = TOKEN_TO_USER_PREFIX + token;

        // 1. 토큰 키로 사용자 정보 조회
        String userValue = redisTemplate.opsForValue().get(tokenKey);

        // 2. 토큰 키 삭제
        redisTemplate.delete(tokenKey);

        // 3. 사용자 키도 삭제 (사용자 정보가 존재하는 경우)
        if (userValue != null) {
            String userKey = REFRESH_TOKEN_PREFIX + userValue;
            redisTemplate.delete(userKey);
        }

        log.info("Refresh token deleted successfully from Redis");
    }
}
