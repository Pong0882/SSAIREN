package com.ssairen.domain.hospital.dto;

import com.ssairen.domain.hospital.entity.PatientInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 환자 정보 생성 요청 DTO
 */
@Schema(description = "환자 정보 생성 요청")
public record PatientInfoCreateRequest(
        @NotNull(message = "구급일지 ID는 필수 입력 항목입니다.")
        @Positive(message = "구급일지 ID는 양의 정수여야 합니다.")
        @Schema(description = "구급일지 ID", example = "1")
        Long emergencyReportId,

        @NotNull(message = "성별은 필수 입력 항목입니다.")
        @Schema(description = "성별 (M/F)", example = "M", allowableValues = {"M", "F"})
        PatientInfo.Gender gender,

        @NotNull(message = "나이는 필수 입력 항목입니다.")
        @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
        @Max(value = 150, message = "나이는 150 이하여야 합니다.")
        @Schema(description = "나이", example = "45")
        Integer age,

        @Schema(description = "기록 시간", example = "2025-01-15T14:30:00")
        LocalDateTime recordTime,

        @NotNull(message = "의식 상태는 필수 입력 항목입니다.")
        @Schema(description = "의식 상태 (ALERT/VERBAL/PAIN/UNRESPONSIVE)", example = "ALERT")
        PatientInfo.MentalStatus mentalStatus,

        @Schema(description = "주 증상", example = "복통, 구토")
        String chiefComplaint,

        @NotNull(message = "심박수는 필수 입력 항목입니다.")
        @Min(value = 0, message = "심박수는 0 이상이어야 합니다.")
        @Max(value = 300, message = "심박수는 300 이하여야 합니다.")
        @Schema(description = "심박수 (HR)", example = "85")
        Integer hr,

        @NotNull(message = "혈압은 필수 입력 항목입니다.")
        @Schema(description = "혈압 (BP)", example = "120/80")
        String bp,

        @NotNull(message = "산소포화도는 필수 입력 항목입니다.")
        @Min(value = 0, message = "산소포화도는 0 이상이어야 합니다.")
        @Max(value = 100, message = "산소포화도는 100 이하여야 합니다.")
        @Schema(description = "산소포화도 (SpO2)", example = "99")
        Integer spo2,

        @NotNull(message = "호흡수는 필수 입력 항목입니다.")
        @Min(value = 0, message = "호흡수는 0 이상이어야 합니다.")
        @Max(value = 100, message = "호흡수는 100 이하여야 합니다.")
        @Schema(description = "호흡수 (RR)", example = "16")
        Integer rr,

        @Schema(description = "체온 (BT)", example = "36.5")
        BigDecimal bt,

        @Schema(description = "보호자 유무 (null일 경우 기본값: false)", example = "true")
        Boolean hasGuardian,

        @Schema(description = "과거력", example = "고혈압, 당뇨")
        String hx,

        @Schema(description = "발병 시간", example = "2025-01-15T14:00:00")
        LocalDateTime onsetTime,

        @Schema(description = "LNT (Last Normal Time)", example = "2025-01-15T13:30:00")
        LocalDateTime lnt
) {
}
