package com.ssairen.domain.emergency.entity;

import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EmergencyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paramedics_id", nullable = false)
    private Paramedic paramedic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fire_states_id", nullable = false)
    private FireState fireState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispatches_id", nullable = false)
    private Dispatch dispatch;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 비즈니스 메서드
    public void toggleCompleted() {
        this.isCompleted = !this.isCompleted;
    }
}
