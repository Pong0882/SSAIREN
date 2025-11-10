package com.ssairen.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Firebase 클라이언트 설정
 * Firebase Admin SDK를 초기화하여 FCM 푸시 알림을 사용할 수 있도록 합니다.
 */
@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${FIREBASE_CREDENTIALS}")
    private String firebaseCredentials;

    /**
     * Firebase Admin SDK 초기화
     * 애플리케이션 시작 시 환경변수에서 Firebase 인증 정보를 읽어 초기화합니다.
     *
     * @throws CustomException Firebase 초기화 실패 시
     */
    @PostConstruct
    public void initialize() {
        try {
            log.info("Firebase Admin SDK 초기화 시작");

            // 이미 초기화된 FirebaseApp이 있는지 확인
            if (!FirebaseApp.getApps().isEmpty()) {
                log.info("Firebase Admin SDK가 이미 초기화되어 있습니다.");
                return;
            }

            // 환경변수에서 가져온 JSON 문자열을 InputStream으로 변환
            ByteArrayInputStream credentialsStream = new ByteArrayInputStream(
                    firebaseCredentials.getBytes(StandardCharsets.UTF_8)
            );

            // Firebase 옵션 설정
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();

            // FirebaseApp 초기화
            FirebaseApp.initializeApp(options);

            log.info("Firebase Admin SDK 초기화 완료");

        } catch (IOException e) {
            log.error("Firebase 인증 정보 파싱 실패: {}", e.getMessage(), e);
            throw new CustomException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "Firebase 초기화 실패: 인증 정보를 확인해주세요.",
                    e
            );
        } catch (IllegalStateException e) {
            log.error("Firebase 초기화 상태 오류: {}", e.getMessage(), e);
            throw new CustomException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "Firebase 초기화 실패: 이미 초기화되었거나 설정이 잘못되었습니다.",
                    e
            );
        } catch (Exception e) {
            log.error("Firebase 초기화 중 예상치 못한 오류: {}", e.getMessage(), e);
            throw new CustomException(
                    ErrorCode.EXTERNAL_API_ERROR,
                    "Firebase 초기화 실패",
                    e
            );
        }
    }
}
