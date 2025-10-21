package com.ssairen.domain.hospital.entity;

import com.ssairen.domain.common.entity.BaseEntity;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.hospital.enums.HospitalSelectionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hospital_selection")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class HospitalSelection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_report_id", nullable = false)
    private EmergencyReport emergencyReport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private HospitalSelectionStatus status;

    @Column(name = "response_at")
    private LocalDateTime responseAt;
}