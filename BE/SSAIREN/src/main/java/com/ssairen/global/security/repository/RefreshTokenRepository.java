package com.ssairen.global.security.repository;

import com.ssairen.global.security.entity.RefreshToken;
import com.ssairen.global.security.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * RefreshToken 레포지토리
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰 문자열로 RefreshToken 조회
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사용자 ID와 타입으로 RefreshToken 조회
     */
    Optional<RefreshToken> findByUserIdAndUserType(Integer userId, UserType userType);

    /**
     * 사용자 ID와 타입으로 RefreshToken 삭제
     */
    void deleteByUserIdAndUserType(Integer userId, UserType userType);

    /**
     * 토큰 문자열로 RefreshToken 삭제
     */
    void deleteByToken(String token);

    /**
     * 만료된 토큰 삭제 (스케줄러에서 사용)
     */
    void deleteByExpiryDateBefore(LocalDateTime dateTime);
}
