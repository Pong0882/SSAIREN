package com.ssairen.domain.hospital.dto;

import com.ssairen.domain.hospital.entity.PatientInfo;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환자 정보 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientInfoDto {

    private Long emergencyReportId;     // 구급일지 ID (PK)
    private String gender;              // 성별: M/F
    private Integer age;                // 나이
    private LocalDateTime recordTime;   // 기록 시간
    private String mentalStatus;        // 의식 상태
    private String chiefComplaint;      // 주 호소
    private Integer hr;                 // 심박수
    private String bp;                  // 혈압
    private Integer spo2;               // 산소포화도
    private Integer rr;                 // 호흡수
    private BigDecimal bt;              // 체온
    private Boolean hasGuardian;        // 보호자 유무
    private String hx;                  // 과거력
    private LocalDateTime onsetTime;    // 발병 시간
    private LocalDateTime lnt;          // LNT

    /**
     * PatientInfo 엔티티를 DTO로 변환
     */
    public static PatientInfoDto from(PatientInfo patientInfo) {
        if (patientInfo == null) {
            return null;
        }

        return PatientInfoDto.builder()
                .emergencyReportId(patientInfo.getEmergencyReportId())
                .gender(patientInfo.getGender().name())
                .age(patientInfo.getAge())
                .recordTime(patientInfo.getRecordTime())
                .mentalStatus(patientInfo.getMentalStatus().name())
                .chiefComplaint(patientInfo.getChiefComplaint())
                .hr(patientInfo.getHr())
                .bp(patientInfo.getBp())
                .spo2(patientInfo.getSpo2())
                .rr(patientInfo.getRr())
                .bt(patientInfo.getBt())
                .hasGuardian(patientInfo.getHasGuardian())
                .hx(patientInfo.getHx())
                .onsetTime(patientInfo.getOnsetTime())
                .lnt(patientInfo.getLnt())
                .build();
    }
}
