package com.ssairen.domain.firestation.service;

import com.google.firebase.messaging.*;
import com.ssairen.domain.firestation.entity.FcmToken;
import com.ssairen.domain.firestation.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * FCM 알림 Service 구현체
 * Firebase Cloud Messaging을 사용하여 푸시 알림을 전송합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FcmServiceImpl implements FcmService {

    private final FcmTokenRepository fcmTokenRepository;

    /**
     * 특정 구급대원에게 푸시 알림 전송
     * 해당 구급대원의 모든 활성화된 디바이스에 알림을 전송합니다.
     *
     * @param paramedicId 구급대원 ID
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터 (선택사항)
     */
    @Override
    public void sendNotification(Integer paramedicId, String title, String body, Map<String, String> data) {
        // 구급대원의 활성화된 FCM 토큰 목록 조회
        List<FcmToken> tokens = fcmTokenRepository.findByParamedicIdAndActiveTrue(paramedicId);

        if (tokens.isEmpty()) {
            log.warn("FCM 토큰이 없습니다. paramedicId={}", paramedicId);
            return;
        }

        log.info("FCM 알림 전송 시작 - paramedicId={}, 토큰 개수={}", paramedicId, tokens.size());

        // 각 토큰별로 알림 전송
        for (FcmToken fcmToken : tokens) {
            try {
                sendToToken(fcmToken, title, body, data);
            } catch (Exception e) {
                log.error("FCM 전송 실패 - paramedicId={}, token={}", paramedicId, fcmToken.getToken(), e);
            }
        }
    }

    /**
     * 개별 토큰으로 FCM 메시지 전송 (Android 전용)
     *
     * @param fcmToken FCM 토큰 엔티티
     * @param title 알림 제목
     * @param body 알림 내용
     * @param data 추가 데이터
     */
    private void sendToToken(FcmToken fcmToken, String title, String body, Map<String, String> data) {
        try {
            // FCM 메시지 구성 (Android 전용)
            Message message = Message.builder()
                    .setToken(fcmToken.getToken())
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putAllData(data != null ? data : Collections.emptyMap())
                    // Android 설정
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .setChannelId("ssairen_emergency") // 앱에서 정의한 채널 ID
                                    .setPriority(AndroidNotification.Priority.HIGH)
                                    .setDefaultSound(true)
                                    .setDefaultVibrateTimings(true)
                                    .build())
                            .build())
                    .build();

            // FCM 전송
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 전송 성공 - response={}, paramedicId={}", response, fcmToken.getParamedicId());

        } catch (FirebaseMessagingException e) {
            log.error("FCM 전송 실패 - paramedicId={}, errorCode={}, message={}",
                    fcmToken.getParamedicId(), e.getMessagingErrorCode(), e.getMessage());

            // 토큰이 유효하지 않은 경우 비활성화 처리
            handleInvalidToken(e, fcmToken);
        }
    }

    /**
     * 유효하지 않은 토큰 처리
     * 토큰이 만료되었거나 삭제된 경우 비활성화합니다.
     *
     * @param e FirebaseMessagingException
     * @param fcmToken FCM 토큰 엔티티
     */
    private void handleInvalidToken(FirebaseMessagingException e, FcmToken fcmToken) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        // 토큰이 등록 해제되었거나 유효하지 않은 경우
        if (errorCode == MessagingErrorCode.UNREGISTERED ||
                errorCode == MessagingErrorCode.INVALID_ARGUMENT) {

            log.warn("유효하지 않은 FCM 토큰 비활성화 - paramedicId={}, token={}",
                    fcmToken.getParamedicId(), fcmToken.getToken());

            fcmToken.deactivate();
            fcmTokenRepository.save(fcmToken);
        }
    }

    /**
     * FCM 토큰 등록
     * 이미 존재하는 토큰인 경우 활성화 상태로 업데이트합니다.
     *
     * @param paramedicId 구급대원 ID
     * @param token FCM 토큰
     */
    @Override
    public void registerToken(Integer paramedicId, String token) {
        // 기존 토큰 조회
        fcmTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        // 이미 존재하는 토큰 -> 활성화
                        existingToken -> {
                            existingToken.activate();
                            fcmTokenRepository.save(existingToken);
                            log.info("기존 FCM 토큰 재활성화 - paramedicId={}", paramedicId);
                        },
                        // 새로운 토큰 -> 생성
                        () -> {
                            FcmToken newToken = FcmToken.builder()
                                    .paramedicId(paramedicId)
                                    .token(token)
                                    .active(true)
                                    .build();
                            fcmTokenRepository.save(newToken);
                            log.info("새로운 FCM 토큰 등록 - paramedicId={}", paramedicId);
                        }
                );
    }

    /**
     * FCM 토큰 삭제
     *
     * @param token FCM 토큰
     */
    @Override
    public void deleteToken(String token) {
        fcmTokenRepository.deleteByToken(token);
        log.info("FCM 토큰 삭제 완료 - token={}", token);
    }

    /**
     * 특정 구급대원의 모든 토큰 삭제
     * 로그아웃 시 사용
     *
     * @param paramedicId 구급대원 ID
     */
    @Override
    public void deleteAllParamedicTokens(Integer paramedicId) {
        fcmTokenRepository.deleteByParamedicId(paramedicId);
        log.info("구급대원의 모든 FCM 토큰 삭제 완료 - paramedicId={}", paramedicId);
    }
}
