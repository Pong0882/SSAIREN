package com.ssairen.domain.hospital.dto;

import com.ssairen.domain.hospital.entity.PatientInfo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 환자 정보 응답 DTO
 */
@Schema(description = "환자 정보 응답")
public record PatientInfoResponse(
        @Schema(description = "구급일지 ID", example = "1")
        Long emergencyReportId,

        @Schema(description = "성별", example = "M")
        PatientInfo.Gender gender,

        @Schema(description = "나이", example = "45")
        Integer age,

        @Schema(description = "기록 시간", example = "14:30:00")
        LocalTime recordTime,

        @Schema(description = "의식 상태", example = "ALERT")
        PatientInfo.MentalStatus mentalStatus,

        @Schema(description = "주 증상", example = "복통, 구토")
        String chiefComplaint,

        @Schema(description = "심박수", example = "85")
        Integer hr,

        @Schema(description = "혈압", example = "120/80")
        String bp,

        @Schema(description = "산소포화도", example = "99")
        Integer spo2,

        @Schema(description = "호흡수", example = "16")
        Integer rr,

        @Schema(description = "체온", example = "36.5")
        BigDecimal bt,

        @Schema(description = "보호자 유무", example = "true")
        Boolean hasGuardian,

        @Schema(description = "과거력", example = "고혈압, 당뇨")
        String hx,

        @Schema(description = "발병 시간", example = "14:00:00")
        LocalTime onsetTime,

        @Schema(description = "LNT", example = "13:30:00")
        LocalTime lnt
) {
    public static PatientInfoResponse from(PatientInfo patientInfo) {
        return new PatientInfoResponse(
                patientInfo.getEmergencyReportId(),
                patientInfo.getGender(),
                patientInfo.getAge(),
                patientInfo.getRecordTime(),
                patientInfo.getMentalStatus(),
                patientInfo.getChiefComplaint(),
                patientInfo.getHr(),
                patientInfo.getBp(),
                patientInfo.getSpo2(),
                patientInfo.getRr(),
                patientInfo.getBt(),
                patientInfo.getHasGuardian(),
                patientInfo.getHx(),
                patientInfo.getOnsetTime(),
                patientInfo.getLnt()
        );
    }
}
