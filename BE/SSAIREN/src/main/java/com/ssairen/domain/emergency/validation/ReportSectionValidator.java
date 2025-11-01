package com.ssairen.domain.emergency.validation;

import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.repository.EmergencyReportRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 구급일지 섹션 관련 입력 값 검증
 */
@Component
@RequiredArgsConstructor
public class ReportSectionValidator {

    private final EmergencyReportRepository emergencyReportRepository;

    /**
     * 구급일지 존재 여부 검증
     *
     * @param emergencyReportId 구급일지 ID
     * @return 구급일지 엔티티
     * @throws CustomException 구급일지가 존재하지 않을 경우
     */
    public EmergencyReport validateEmergencyReportExists(Long emergencyReportId) {
        return emergencyReportRepository.findById(emergencyReportId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMERGENCY_REPORT_NOT_FOUND));
    }

    /**
     * 섹션 타입 유효성 검증
     *
     * @param type 섹션 타입
     * @throws CustomException 유효하지 않은 섹션 타입일 경우
     */
    public void validateSectionType(ReportSectionType type) {
        if (Objects.isNull(type)) {
            throw new CustomException(ErrorCode.INVALID_REPORT_SECTION_TYPE);
        }
        // enum 타입으로 파라미터를 받으므로 ReportSectionType에 정의된 값만 허용됨
    }
}
