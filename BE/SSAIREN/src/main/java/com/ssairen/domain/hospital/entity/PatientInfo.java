package com.ssairen.domain.hospital.entity;

import com.ssairen.domain.common.entity.BaseEntity;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "patient_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PatientInfo extends BaseEntity {

    @Id
    @Column(name = "emergency_report_id")
    private Long emergencyReportId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_report_id")
    private EmergencyReport emergencyReport;

    // ── 상단 기본 정보 ─────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;             // 성별: M/F

    @Column(name = "age", nullable = false)
    private Integer age;               // 나이

    @Column(name = "record_time")
    private LocalTime recordTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "mental_status", nullable = false, length = 20)
    private MentalStatus mentalStatus;

    @Column(name = "chief_complaint", length = 255)
    private String chiefComplaint;

    @Column(name = "hr", nullable = false)
    private Integer hr;                // 심박수 HR

    @Column(name = "bp", nullable = false, length = 10)
    private String bp;

    @Column(name = "spo2", nullable = false)
    private Integer spo2;              // 산소포화도

    @Column(name = "rr", nullable = false)
    private Integer rr;                // 호흡수

    @Digits(integer = 2, fraction = 1) // 예: 36.5
    @Column(name = "bt", precision = 4, scale = 1)
    private BigDecimal bt;             // 체온

    @Column(name = "has_guardian", nullable = false)
    private Boolean hasGuardian;       // 보호자 유무

    @Column(name = "hx", columnDefinition = "TEXT")
    private String hx;                 // 과거력

    @Column(name = "onset_time")
    private LocalTime onsetTime;       // 발병 시간

    @Column(name = "lnt")
    private LocalTime lnt;  // LNT


    // ── ENUM 정의 ────────────────────────────────────────────────────────────
    public enum Gender { M, F }

    public enum MentalStatus {
        ALERT, VERBAL, PAIN, UNRESPONSIVE
        // 필요 시 AVPU 외 GCS/기타 체계로 확장 가능
    }

    // ── 업데이트 메서드 ──────────────────────────────────────────────────────
    /**
     * 환자 정보 업데이트
     */
    public void updatePatientInfo(
            Gender gender,
            Integer age,
            LocalTime recordTime,
            MentalStatus mentalStatus,
            String chiefComplaint,
            Integer hr,
            String bp,
            Integer spo2,
            Integer rr,
            BigDecimal bt,
            Boolean hasGuardian,
            String hx,
            LocalTime onsetTime,
            LocalTime lnt
    ) {
        this.gender = gender;
        this.age = age;
        this.recordTime = recordTime;
        this.mentalStatus = mentalStatus;
        this.chiefComplaint = chiefComplaint;
        this.hr = hr;
        this.bp = bp;
        this.spo2 = spo2;
        this.rr = rr;
        this.bt = bt;
        this.hasGuardian = hasGuardian;
        this.hx = hx;
        this.onsetTime = onsetTime;
        this.lnt = lnt;
    }
}
