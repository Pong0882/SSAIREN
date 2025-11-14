package com.ssairen.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.entity.ReportSection;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.repository.ReportSectionRepository;
import com.ssairen.domain.emergency.util.JsonMergeUtil;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI 응답을 ReportSection으로 변환하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiResponseToReportSectionService {

    private final ReportSectionRepository reportSectionRepository;
    private final ObjectMapper objectMapper;
    private final JsonMergeUtil jsonMergeUtil;

    /**
     * AI 응답 JSON 필드명을 ReportSectionType으로 매핑
     */
    private static final Map<String, ReportSectionType> FIELD_TO_TYPE_MAP = new LinkedHashMap<>();

    static {
        FIELD_TO_TYPE_MAP.put("patientInfo", ReportSectionType.PATIENT_INFO);
        FIELD_TO_TYPE_MAP.put("dispatch", ReportSectionType.DISPATCH);
        FIELD_TO_TYPE_MAP.put("incidentType", ReportSectionType.INCIDENT_TYPE);
        FIELD_TO_TYPE_MAP.put("assessment", ReportSectionType.ASSESSMENT);
        FIELD_TO_TYPE_MAP.put("treatment", ReportSectionType.TREATMENT);
        FIELD_TO_TYPE_MAP.put("medicalGuidance", ReportSectionType.MEDICAL_GUIDANCE);
        FIELD_TO_TYPE_MAP.put("transport", ReportSectionType.TRANSPORT);
        FIELD_TO_TYPE_MAP.put("detailReport", ReportSectionType.DETAIL_REPORT);
    }

    /**
     * AI 응답을 파싱하여 ReportSection에 저장
     *
     * @param aiResponse     AI 서버 응답 (Object 형태)
     * @param emergencyReport 구급일지
     * @return 저장된 섹션 개수
     */
    @Transactional
    public int saveAiResponseToReportSections(Object aiResponse, EmergencyReport emergencyReport) {
        try {
            log.info("AI 응답을 ReportSection으로 변환 시작 - 구급일지 ID: {}", emergencyReport.getId());

            // Object를 JsonNode로 변환
            JsonNode rootNode = objectMapper.valueToTree(aiResponse);

            // ReportSectionType 노드 찾기 (AI 서버 응답은 바로 ReportSectionType부터 시작)
            JsonNode reportSectionTypeNode = rootNode.get("ReportSectionType");
            if (reportSectionTypeNode == null) {
                log.warn("AI 응답에 ReportSectionType이 없습니다. 응답 구조: {}", rootNode.toPrettyString());
                return 0;
            }
            int savedCount = 0;

            // 각 섹션 타입별로 데이터 추출 및 저장
            for (Map.Entry<String, ReportSectionType> entry : FIELD_TO_TYPE_MAP.entrySet()) {
                String fieldName = entry.getKey();
                ReportSectionType sectionType = entry.getValue();

                if (reportSectionTypeNode.has(fieldName)) {
                    JsonNode sectionData = reportSectionTypeNode.get(fieldName);

                    // 빈 객체가 아닌 경우에만 저장
                    if (sectionData != null && !sectionData.isNull() && sectionData.size() > 0) {
                        saveOrUpdateReportSection(emergencyReport, sectionType, sectionData);
                        savedCount++;
                        log.info("섹션 저장 완료 - 타입: {}, 필드명: {}", sectionType, fieldName);
                    }
                }
            }

            log.info("AI 응답 변환 완료 - 구급일지 ID: {}, 저장된 섹션 수: {}",
                    emergencyReport.getId(), savedCount);

            return savedCount;

        } catch (Exception e) {
            log.error("AI 응답 변환 중 오류 발생 - 구급일지 ID: {}", emergencyReport.getId(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "AI 응답을 ReportSection으로 변환하는 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * ReportSection 저장 또는 업데이트
     * - 이미 존재하면 기존 값을 우선하여 병합 (기존 값이 비어있을 때만 새 값으로 채움)
     * - 존재하지 않으면 새로 생성
     *
     * @param emergencyReport 구급일지
     * @param sectionType    섹션 타입
     * @param sectionData    섹션 데이터
     */
    private void saveOrUpdateReportSection(EmergencyReport emergencyReport,
                                          ReportSectionType sectionType,
                                          JsonNode sectionData) {
        // 기존 섹션이 있는지 확인
        reportSectionRepository.findByEmergencyReportAndType(emergencyReport, sectionType)
                .ifPresentOrElse(
                        // 이미 존재하면 기존 값을 우선하여 병합
                        existingSection -> {
                            JsonNode mergedData = jsonMergeUtil.mergePreservingExisting(
                                    existingSection.getData(),
                                    sectionData
                            );
                            existingSection.updateData(mergedData);
                            reportSectionRepository.save(existingSection);
                            log.debug("기존 섹션 병합 완료 (기존 값 우선) - 타입: {}, 버전: {}",
                                    sectionType, existingSection.getVersion());
                        },
                        // 존재하지 않으면 새로 생성
                        () -> {
                            ReportSection newSection = ReportSection.builder()
                                    .emergencyReport(emergencyReport)
                                    .type(sectionType)
                                    .data(sectionData)
                                    .version(1)
                                    .build();
                            reportSectionRepository.save(newSection);
                            log.debug("새 섹션 생성 완료 - 타입: {}", sectionType);
                        }
                );
    }
}