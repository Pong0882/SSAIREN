package com.ssairen.global.security.converter;

import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.global.security.dto.TokenResponse;
import com.ssairen.global.security.enums.UserType;
import org.springframework.stereotype.Component;

/**
 * TokenResponse 변환 유틸리티
 */
@Component
public class TokenResponseConverter {

    /**
     * 기본 TokenResponse 생성 (상세 정보 없음)
     *
     * @param accessToken  액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param userType     사용자 타입
     * @param userId       사용자 ID
     * @param username     사용자명
     * @return TokenResponse
     */
    public TokenResponse toTokenResponse(String accessToken, String refreshToken,
                                          UserType userType, Integer userId, String username) {
        return new TokenResponse(
                accessToken,
                refreshToken,
                userType,
                userId,
                username
        );
    }

    /**
     * 구급대원 상세 정보를 포함한 TokenResponse 생성
     *
     * @param accessToken  액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param paramedic    구급대원 엔티티
     * @return TokenResponse (구급대원 상세 정보 포함)
     */
    public TokenResponse toTokenResponseWithParamedic(String accessToken, String refreshToken,
                                                       Paramedic paramedic) {
        return new TokenResponse(
                accessToken,
                refreshToken,
                UserType.PARAMEDIC,
                paramedic.getId(),
                paramedic.getStudentNumber(),
                paramedic.getName(),
                paramedic.getRank(),
                paramedic.getStatus(),
                paramedic.getFireState().getId(),
                paramedic.getFireState().getName()
        );
    }
}