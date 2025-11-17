package com.ssairen.domain.hospital.dto;

import com.ssairen.domain.hospital.entity.HospitalSelection;
import com.ssairen.domain.hospital.entity.PatientInfo;
import com.ssairen.domain.hospital.enums.HospitalSelectionStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 병원이 수용한 환자 정보 DTO
 * ACCEPTED (내원 대기중) 또는 ARRIVED (내원 완료) 상태의 환자 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcceptedPatientDto {

    private Integer hospitalSelectionId;        // 병원 선택 ID
    private Long emergencyReportId;             // 구급일지 ID

    // 환자 정보
    private String gender;                      // 성별 (M/F)
    private Integer age;                        // 나이
    private LocalDateTime recordTime;               // 기록 시간
    private String chiefComplaint;              // 주 호소
    private String mentalStatus;                // 의식 상태 (ALERT, VERBAL, PAIN, UNRESPONSIVE)

    // 내원 상태
    private HospitalSelectionStatus status;     // ACCEPTED (대기중) or ARRIVED (내원 완료)

    /**
     * HospitalSelection과 PatientInfo로부터 DTO 생성
     */
    public static AcceptedPatientDto from(HospitalSelection selection, PatientInfo patientInfo) {
        return AcceptedPatientDto.builder()
                .hospitalSelectionId(selection.getId())
                .emergencyReportId(selection.getEmergencyReport().getId())
                .gender(patientInfo.getGender().name())
                .age(patientInfo.getAge())
                .recordTime(patientInfo.getRecordTime())
                .chiefComplaint(patientInfo.getChiefComplaint())
                .mentalStatus(patientInfo.getMentalStatus().name())
                .status(selection.getStatus())
                .build();
    }
}
