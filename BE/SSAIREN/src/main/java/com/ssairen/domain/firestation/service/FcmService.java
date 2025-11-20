package com.ssairen.domain.firestation.service;

import java.util.Map;

/**
 * FCM 알림 Service 인터페이스
 */
public interface FcmService {

    /**
     * 특정 구급대원에게 푸시 알림 전송
     *
     * @param paramedicId 구급대원 ID
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (optional)
     */
    void sendNotification(Integer paramedicId, String title, String body, Map<String, String> data);

    /**
     * FCM 토큰 등록
     *
     * @param paramedicId 구급대원 ID
     * @param token FCM 토큰
     */
    void registerToken(Integer paramedicId, String token);

    /**
     * FCM 토큰 삭제
     *
     * @param token FCM 토큰
     */
    void deleteToken(String token);

    /**
     * 특정 구급대원의 모든 토큰 삭제 (로그아웃 시 사용)
     *
     * @param paramedicId 구급대원 ID
     */
    void deleteAllParamedicTokens(Integer paramedicId);
}
