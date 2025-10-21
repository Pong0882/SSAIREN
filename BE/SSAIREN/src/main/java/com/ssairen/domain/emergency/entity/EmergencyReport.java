package com.ssairen.domain.emergency.entity;

import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    @Column(name = "date", nullable = false)
    private LocalDateTime date;
}
