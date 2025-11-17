package com.ssairen.domain.firestation.service;

import com.ssairen.domain.firestation.entity.FcmToken;
import com.ssairen.domain.firestation.repository.FcmTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmServiceImplTest {

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @InjectMocks
    private FcmServiceImpl service;

    private Integer paramedicId;
    private String fcmToken;

    @BeforeEach
    void setUp() {
        paramedicId = 1;
        fcmToken = "test-fcm-token-12345";
    }

    @Test
    @DisplayName("FCM 알림 전송 - 토큰 없음")
    void sendNotification_noTokens() {
        // given
        String title = "긴급 출동 요청";
        String body = "새로운 출동 요청이 있습니다.";
        Map<String, String> data = new HashMap<>();
        data.put("dispatchId", "123");

        when(fcmTokenRepository.findByParamedicIdAndActiveTrue(paramedicId))
                .thenReturn(Collections.emptyList());

        // when
        service.sendNotification(paramedicId, title, body, data);

        // then
        verify(fcmTokenRepository).findByParamedicIdAndActiveTrue(paramedicId);
        // 토큰이 없으므로 전송 시도 없이 종료
    }

    @Test
    @DisplayName("FCM 토큰 등록 - 신규 토큰")
    void registerToken_newToken() {
        // given
        when(fcmTokenRepository.findByToken(fcmToken))
                .thenReturn(Optional.empty());

        // when
        service.registerToken(paramedicId, fcmToken);

        // then
        verify(fcmTokenRepository).findByToken(fcmToken);
        verify(fcmTokenRepository).save(argThat(token ->
                token.getParamedicId().equals(paramedicId) &&
                token.getToken().equals(fcmToken) &&
                token.getActive()
        ));
    }

    @Test
    @DisplayName("FCM 토큰 등록 - 기존 토큰 재활성화")
    void registerToken_existingToken() {
        // given
        FcmToken existingToken = FcmToken.builder()
                .id(1L)
                .paramedicId(paramedicId)
                .token(fcmToken)
                .active(false)
                .build();

        when(fcmTokenRepository.findByToken(fcmToken))
                .thenReturn(Optional.of(existingToken));

        // when
        service.registerToken(paramedicId, fcmToken);

        // then
        verify(fcmTokenRepository).findByToken(fcmToken);
        verify(fcmTokenRepository).save(argThat(token ->
                token.getId().equals(1L) &&
                token.getActive()
        ));
    }

    @Test
    @DisplayName("FCM 토큰 삭제")
    void deleteToken_success() {
        // given
        doNothing().when(fcmTokenRepository).deleteByToken(fcmToken);

        // when
        service.deleteToken(fcmToken);

        // then
        verify(fcmTokenRepository).deleteByToken(fcmToken);
    }

    @Test
    @DisplayName("구급대원의 모든 FCM 토큰 삭제")
    void deleteAllParamedicTokens_success() {
        // given
        doNothing().when(fcmTokenRepository).deleteByParamedicId(paramedicId);

        // when
        service.deleteAllParamedicTokens(paramedicId);

        // then
        verify(fcmTokenRepository).deleteByParamedicId(paramedicId);
    }

    @Test
    @DisplayName("FCM 토큰 등록 - 다른 구급대원이 사용 중인 토큰")
    void registerToken_tokenUsedByAnother() {
        // given
        Integer anotherParamedicId = 2;
        FcmToken existingToken = FcmToken.builder()
                .id(1L)
                .paramedicId(anotherParamedicId)
                .token(fcmToken)
                .active(true)
                .build();

        when(fcmTokenRepository.findByToken(fcmToken))
                .thenReturn(Optional.of(existingToken));

        // when
        service.registerToken(paramedicId, fcmToken);

        // then
        // 기존 토큰이 있으면 재활성화 (다른 구급대원 것이어도)
        verify(fcmTokenRepository).findByToken(fcmToken);
        verify(fcmTokenRepository).save(any(FcmToken.class));
    }

    @Test
    @DisplayName("FCM 토큰 등록 - null 데이터로 알림 전송")
    void sendNotification_withNullData() {
        // given
        String title = "테스트 알림";
        String body = "본문 내용";

        when(fcmTokenRepository.findByParamedicIdAndActiveTrue(paramedicId))
                .thenReturn(Collections.emptyList());

        // when
        service.sendNotification(paramedicId, title, body, null);

        // then
        verify(fcmTokenRepository).findByParamedicIdAndActiveTrue(paramedicId);
    }

    @Test
    @DisplayName("FCM 토큰 등록 - 여러 토큰 등록")
    void registerToken_multipleTokens() {
        // given
        String token1 = "token-1";
        String token2 = "token-2";
        String token3 = "token-3";

        when(fcmTokenRepository.findByToken(anyString()))
                .thenReturn(Optional.empty());

        // when
        service.registerToken(paramedicId, token1);
        service.registerToken(paramedicId, token2);
        service.registerToken(paramedicId, token3);

        // then
        verify(fcmTokenRepository, times(3)).findByToken(anyString());
        verify(fcmTokenRepository, times(3)).save(any(FcmToken.class));
    }
}
