package com.ssairen.domain.hospital.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 환자 정보 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePatientInfoRequest {

    @NotNull(message = "성별은 필수입니다.")
    @Pattern(regexp = "^(M|F)$", message = "성별은 M 또는 F만 가능합니다.")
    private String gender;                      // 성별: M/F

    @NotNull(message = "나이는 필수입니다.")
    @Min(value = 0, message = "나이는 0 이상이어야 합니다.")
    @Max(value = 150, message = "나이는 150 이하여야 합니다.")
    private Integer age;                        // 나이

    private LocalTime recordTime;               // 기록 시간

    @NotNull(message = "의식 상태는 필수입니다.")
    @Pattern(regexp = "^(ALERT|VERBAL|PAIN|UNRESPONSIVE)$", message = "의식 상태는 ALERT, VERBAL, PAIN, UNRESPONSIVE 중 하나여야 합니다.")
    private String mentalStatus;                // 의식 상태

    private String chiefComplaint;              // 주 호소

    @NotNull(message = "심박수는 필수입니다.")
    @Min(value = 0, message = "심박수는 0 이상이어야 합니다.")
    private Integer hr;                         // 심박수

    @NotNull(message = "혈압은 필수입니다.")
    private String bp;                          // 혈압

    @NotNull(message = "산소포화도는 필수입니다.")
    @Min(value = 0, message = "산소포화도는 0 이상이어야 합니다.")
    @Max(value = 100, message = "산소포화도는 100 이하여야 합니다.")
    private Integer spo2;                       // 산소포화도

    @NotNull(message = "호흡수는 필수입니다.")
    @Min(value = 0, message = "호흡수는 0 이상이어야 합니다.")
    private Integer rr;                         // 호흡수

    @DecimalMin(value = "0.0", message = "체온은 0 이상이어야 합니다.")
    @DecimalMax(value = "50.0", message = "체온은 50 이하여야 합니다.")
    @Digits(integer = 2, fraction = 1, message = "체온은 소수점 1자리까지 입력 가능합니다.")
    private BigDecimal bt;                      // 체온

    @NotNull(message = "보호자 유무는 필수입니다.")
    private Boolean hasGuardian;                // 보호자 유무

    private String hx;                          // 과거력

    private LocalTime onsetTime;                // 발병 시간

    private LocalTime lnt;                      // LNT
}
