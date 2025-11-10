package com.ssairen.domain.firestation.repository;

import com.ssairen.domain.firestation.entity.FcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FCM 토큰 Repository
 * 구급대원의 FCM 토큰을 관리합니다.
 */
@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    /**
     * 토큰으로 FCM 토큰 엔티티 조회
     *
     * @param token FCM 토큰 문자열
     * @return FCM 토큰 엔티티
     */
    Optional<FcmToken> findByToken(String token);

    /**
     * 특정 구급대원의 활성화된 FCM 토큰 목록 조회
     * 한 구급대원이 여러 디바이스를 사용할 수 있으므로 List로 반환
     *
     * @param paramedicId 구급대원 ID
     * @return 활성화된 FCM 토큰 목록
     */
    List<FcmToken> findByParamedicIdAndActiveTrue(Integer paramedicId);

    /**
     * 특정 구급대원의 모든 FCM 토큰 조회 (활성/비활성 포함)
     *
     * @param paramedicId 구급대원 ID
     * @return FCM 토큰 목록
     */
    List<FcmToken> findByParamedicId(Integer paramedicId);

    /**
     * 토큰으로 FCM 토큰 삭제
     *
     * @param token FCM 토큰 문자열
     */
    void deleteByToken(String token);

    /**
     * 특정 구급대원의 모든 토큰 삭제
     *
     * @param paramedicId 구급대원 ID
     */
    void deleteByParamedicId(Integer paramedicId);
}
