package com.ssairen.domain.emergency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssairen.domain.emergency.dto.ReportSectionCreateResponse;
import com.ssairen.domain.emergency.dto.ReportSectionUpdateRequest;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.entity.ReportSection;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.mapper.ReportSectionMapper;
import com.ssairen.domain.emergency.repository.ReportSectionRepository;
import com.ssairen.domain.emergency.util.JsonMergeUtil;
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
    private final JsonMergeUtil jsonMergeUtil;

    /**
     * 구급일지 섹션 생성
     *
     * @param emergencyReportId 구급일지 ID
     * @param type              섹션 타입
     * @param paramedicId       구급대원 ID (권한 검증용)
     * @return 생성된 섹션 정보 (스켈레톤 JSON 포함)
     */
    @Override
    @Transactional
    public ReportSectionCreateResponse createReportSection(Long emergencyReportId, ReportSectionType type, Integer paramedicId) {
        log.info("Creating report section - Emergency Report ID: {}, Type: {}, Paramedic ID: {}",
                emergencyReportId, type, paramedicId);

        // 1. 구급일지 존재 여부 검증
        EmergencyReport emergencyReport = reportSectionValidator
                .validateEmergencyReportExists(emergencyReportId);

        // 2. 권한 검증: 해당 구급일지를 작성한 구급대원인지 확인
        if (!emergencyReport.getParamedic().getId().equals(paramedicId)) {
            log.warn("Unauthorized section creation attempt - Emergency Report ID: {}, Paramedic ID: {}, Report Owner: {}",
                    emergencyReportId, paramedicId, emergencyReport.getParamedic().getId());
            throw new CustomException(ErrorCode.ACCESS_DENIED, "본인이 작성한 구급일지에 대해서만 섹션을 생성할 수 있습니다.");
        }

        // 3. 섹션 타입 유효성 검증
        reportSectionValidator.validateSectionType(type);

        // 3. 중복 생성 방지
        if (reportSectionRepository.existsByEmergencyReportAndType(emergencyReport, type)) {
            throw new CustomException(ErrorCode.REPORT_SECTION_ALREADY_EXISTS);
        }

        // 4. 타입에 맞는 스켈레톤 JSON 생성
        JsonNode skeletonData;
        if (type == ReportSectionType.DISPATCH) {
            // DISPATCH 타입은 실제 Dispatch 데이터로 초기값 설정
            skeletonData = ReportSectionTemplate.getTemplateForDispatch(emergencyReport.getDispatch());
        } else {
            // 다른 타입은 기본 템플릿 사용 (모든 값 null)
            skeletonData = ReportSectionTemplate.getTemplate(type);
        }

        // 5. 섹션 엔티티 생성 및 저장
        ReportSection section = ReportSection.builder()
                .emergencyReport(emergencyReport)
                .type(type)
                .data(skeletonData)
                .version(1)
                .build();

        ReportSection savedSection = reportSectionRepository.save(section);

        log.info("구급일지 섹션 생성 완료 - 섹션 ID: {}, 구급일지 ID: {}, 타입: {}",
                savedSection.getId(), emergencyReport.getId(), type);

        // 6. 응답 DTO 변환
        return reportSectionMapper.toCreateResponse(savedSection);
    }

    /**
     * 구급일지 특정 섹션 조회
     *
     * @param emergencyReportId 구급일지 ID
     * @param type              섹션 타입
     * @param paramedicId       구급대원 ID (권한 검증용)
     * @return 섹션 정보 (JSON 데이터 포함)
     */
    @Override
    @Transactional(readOnly = true)
    public ReportSectionCreateResponse getReportSection(Long emergencyReportId, ReportSectionType type, Integer paramedicId) {
        log.info("Fetching report section - Emergency Report ID: {}, Type: {}, Paramedic ID: {}",
                emergencyReportId, type, paramedicId);

        // 1. 구급일지 존재 여부 검증
        EmergencyReport emergencyReport = reportSectionValidator
                .validateEmergencyReportExists(emergencyReportId);

        // 2. 권한 검증: 해당 구급일지를 작성한 구급대원인지 확인
        if (!emergencyReport.getParamedic().getId().equals(paramedicId)) {
            log.warn("Unauthorized section access attempt - Emergency Report ID: {}, Paramedic ID: {}, Report Owner: {}",
                    emergencyReportId, paramedicId, emergencyReport.getParamedic().getId());
            throw new CustomException(ErrorCode.ACCESS_DENIED, "본인이 작성한 구급일지의 섹션만 조회할 수 있습니다.");
        }

        // 3. 섹션 타입 유효성 검증
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

    /**
     * 구급일지 섹션 수정
     *
     * @param emergencyReportId 구급일지 ID
     * @param type              섹션 타입
     * @param request           수정할 데이터
     * @param paramedicId       구급대원 ID (권한 검증용)
     * @return 수정된 섹션 정보
     */
    @Override
    @Transactional
    public ReportSectionCreateResponse updateReportSection(Long emergencyReportId, ReportSectionType type, ReportSectionUpdateRequest request, Integer paramedicId) {
        log.info("Updating report section - Emergency Report ID: {}, Type: {}, Paramedic ID: {}",
                emergencyReportId, type, paramedicId);

        // 1. 구급일지 존재 여부 검증
        EmergencyReport emergencyReport = reportSectionValidator
                .validateEmergencyReportExists(emergencyReportId);

        // 2. 권한 검증: 해당 구급일지를 작성한 구급대원인지 확인
        if (!emergencyReport.getParamedic().getId().equals(paramedicId)) {
            log.warn("Unauthorized section update attempt - Emergency Report ID: {}, Paramedic ID: {}, Report Owner: {}",
                    emergencyReportId, paramedicId, emergencyReport.getParamedic().getId());
            throw new CustomException(ErrorCode.ACCESS_DENIED, "본인이 작성한 구급일지의 섹션만 수정할 수 있습니다.");
        }

        // 3. 섹션 타입 유효성 검증
        reportSectionValidator.validateSectionType(type);

        // 3. 섹션 조회
        ReportSection section = reportSectionRepository
                .findByEmergencyReportAndType(emergencyReport, type)
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_SECTION_NOT_FOUND));

        // 4. 기존 데이터와 새로운 데이터 병합 (null/빈값 무시)
        JsonNode mergedData = jsonMergeUtil.mergeIgnoringNulls(section.getData(), request.data());

        // 5. 엔티티 업데이트 (version도 자동으로 +1)
        section.updateData(mergedData);

        // 6. 저장 (영속성 컨텍스트에 의해 자동으로 DB 반영)
        ReportSection updatedSection = reportSectionRepository.save(section);

        log.info("구급일지 섹션 수정 완료 - 섹션 ID: {}, 구급일지 ID: {}, 타입: {}, 버전: {}",
                updatedSection.getId(), emergencyReportId, type, updatedSection.getVersion());

        // 7. 응답 DTO 변환
        return reportSectionMapper.toCreateResponse(updatedSection);
    }
}
