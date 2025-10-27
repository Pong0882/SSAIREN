package com.ssairen.domain.vitalSign.entity;

import com.ssairen.domain.common.entity.BaseEntity;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "HR")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HR extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_report_id", nullable = false)
    private EmergencyReport emergencyReport;

    @Column(name = "HR", nullable = false)
    private Integer hr;
}