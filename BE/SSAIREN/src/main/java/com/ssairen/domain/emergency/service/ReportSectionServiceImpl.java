package com.ssairen.domain.emergency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssairen.domain.emergency.dto.ReportSectionCreateRequest;
import com.ssairen.domain.emergency.dto.ReportSectionCreateResponse;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.entity.ReportSection;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.mapper.ReportSectionMapper;
import com.ssairen.domain.emergency.repository.ReportSectionRepository;
import com.ssairen.domain.emergency.util.ReportSectionTemplate;
import com.ssairen.domain.emergency.validation.ReportSectionValidator;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportSectionServiceImpl implements ReportSectionService {

    private final ReportSectionRepository reportSectionRepository;
    private final ReportSectionValidator reportSectionValidator;
    private final ReportSectionMapper reportSectionMapper;

    /**
     * 구급일지 섹션 생성
     *
     * @param request 섹션 생성 요청 (구급일지 ID, 섹션 타입)
     * @return 생성된 섹션 정보 (스켈레톤 JSON 포함)
     */
    @Override
    @Transactional
    public ReportSectionCreateResponse createReportSection(ReportSectionCreateRequest request) {
        // 1. 구급일지 존재 여부 검증
        EmergencyReport emergencyReport = reportSectionValidator
                .validateEmergencyReportExists(request.emergencyReportId());

        // 2. 섹션 타입 유효성 검증
        reportSectionValidator.validateSectionType(request.type());

        // 3. 중복 생성 방지
        if (reportSectionRepository.existsByEmergencyReportAndType(emergencyReport, request.type())) {
            throw new CustomException(ErrorCode.REPORT_SECTION_ALREADY_EXISTS);
        }

        // 4. 타입에 맞는 스켈레톤 JSON 생성
        JsonNode skeletonData = ReportSectionTemplate.getTemplate(request.type());

        // 5. 섹션 엔티티 생성 및 저장
        ReportSection section = ReportSection.builder()
                .emergencyReport(emergencyReport)
                .type(request.type())
                .data(skeletonData)
                .version(1)
                .build();

        ReportSection savedSection = reportSectionRepository.save(section);

        log.info("구급일지 섹션 생성 완료 - 섹션 ID: {}, 구급일지 ID: {}, 타입: {}",
                savedSection.getId(), emergencyReport.getId(), request.type());

        // 6. 응답 DTO 변환
        return reportSectionMapper.toCreateResponse(savedSection);
    }

    /**
     * 구급일지 특정 섹션 조회
     *
     * @param emergencyReportId 구급일지 ID
     * @param type 섹션 타입
     * @return 섹션 정보 (JSON 데이터 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public ReportSectionCreateResponse getReportSection(Long emergencyReportId, ReportSectionType type) {
        // 1. 구급일지 존재 여부 검증
        EmergencyReport emergencyReport = reportSectionValidator
                .validateEmergencyReportExists(emergencyReportId);

        // 2. 섹션 타입 유효성 검증
        reportSectionValidator.validateSectionType(type);

        // 3. 섹션 조회
        ReportSection section = reportSectionRepository
                .findByEmergencyReportAndType(emergencyReport, type)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_SECTION_NOT_FOUND));

        log.info("구급일지 섹션 조회 완료 - 섹션 ID: {}, 구급일지 ID: {}, 타입: {}",
                section.getId(), emergencyReportId, type);

        // 4. 응답 DTO 변환
        return reportSectionMapper.toCreateResponse(section);
    }
}
