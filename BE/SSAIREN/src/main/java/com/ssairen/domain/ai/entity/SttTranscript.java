package com.ssairen.domain.ai.entity;

import com.ssairen.domain.common.entity.BaseEntity;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stt_transcripts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SttTranscript extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_report_id", nullable = false)
    private EmergencyReport emergencyReport;

    @Column(name = "data", nullable = false, columnDefinition = "TEXT")
    private String data;
}