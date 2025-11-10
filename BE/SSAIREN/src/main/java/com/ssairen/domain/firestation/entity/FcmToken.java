package com.ssairen.domain.firestation.entity;

import com.ssairen.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * FCM 토큰 엔티티
 * 구급대원의 모바일 기기에서 발급된 FCM 토큰을 저장하여
 * 푸시 알림을 전송할 수 있도록 관리합니다.
 */
@Entity
@Table(name = "fcm_tokens", indexes = {
        @Index(name = "idx_fcm_token", columnList = "token"),
        @Index(name = "idx_paramedic_id_active", columnList = "paramedic_id,active")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FcmToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 구급대원 ID
     */
    @Column(name = "paramedic_id", nullable = false)
    private Integer paramedicId;

    /**
     * FCM 토큰 (최대 500자)
     * Firebase에서 발급한 고유 디바이스 토큰
     */
    @Column(name = "token", nullable = false, unique = true, length = 500)
    private String token;

    /**
     * 토큰 활성화 상태
     * false인 경우 유효하지 않은 토큰으로 판단하여 알림을 전송하지 않음
     */
    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * 토큰을 비활성화합니다.
     * FCM 전송 실패 시 호출되어 더 이상 해당 토큰으로 알림을 보내지 않도록 합니다.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * 토큰을 활성화합니다.
     * 재등록 시 기존 토큰이 존재하면 다시 활성화합니다.
     */
    public void activate() {
        this.active = true;
    }
}
