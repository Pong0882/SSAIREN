package com.ssairen.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.entity.ReportSection;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.repository.ReportSectionRepository;
import com.ssairen.domain.emergency.util.JsonMergeUtil;
import com.ssairen.domain.emergency.util.ReportSectionTemplate;
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
                        // AI 응답을 DB 스키마에 맞게 변환
                        JsonNode transformedData = transformAiResponseToDbSchema(sectionData, sectionType);

                        saveOrUpdateReportSection(emergencyReport, sectionType, transformedData);
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
     * - 존재하지 않으면 템플릿 기반으로 새로 생성
     * - DB 템플릿 구조: {schemaVersion: 1, [sectionName]: {...}}
     * - AI 응답 구조: {reporter: {...}, patient: {...}, ...}
     *
     * @param emergencyReport 구급일지
     * @param sectionType    섹션 타입
     * @param aiResponseData AI 응답 섹션 데이터 (내부 데이터만, 템플릿 구조 제외)
     */
    private void saveOrUpdateReportSection(EmergencyReport emergencyReport,
                                          ReportSectionType sectionType,
                                          JsonNode aiResponseData) {
        // 섹션 타입에 해당하는 필드명 (예: patientInfo, dispatch 등)
        String fieldName = getFieldNameForType(sectionType);

        // 기존 섹션이 있는지 확인
        reportSectionRepository.findByEmergencyReportAndType(emergencyReport, sectionType)
                .ifPresentOrElse(
                        // 이미 존재하면 기존 값을 우선하여 병합
                        existingSection -> {
                            JsonNode existingFullData = existingSection.getData();

                            // DB에서 내부 섹션 데이터 추출 (예: {schemaVersion: 1, patientInfo: {...}}에서 patientInfo 부분)
                            JsonNode existingInnerData = existingFullData.get(fieldName);

                            // AI 응답과 병합 (기존 값 우선)
                            JsonNode mergedInnerData = jsonMergeUtil.mergePreservingExisting(
                                    existingInnerData,
                                    aiResponseData
                            );

                            // 병합 결과를 다시 전체 구조로 감싸기
                            ObjectNode newFullData = (ObjectNode) existingFullData.deepCopy();
                            newFullData.set(fieldName, mergedInnerData);

                            existingSection.updateData(newFullData);
                            reportSectionRepository.save(existingSection);
                            log.debug("기존 섹션 병합 완료 (기존 값 우선) - 타입: {}, 버전: {}",
                                    sectionType, existingSection.getVersion());
                        },
                        // 존재하지 않으면 템플릿 기반으로 새로 생성
                        () -> {
                            // 템플릿 가져오기
                            JsonNode template = ReportSectionTemplate.getTemplate(sectionType);
                            ObjectNode templateNode = (ObjectNode) template.deepCopy();

                            // AI 응답 데이터와 병합 (템플릿의 빈 값들을 AI 응답으로 채움)
                            JsonNode templateInnerData = template.get(fieldName);
                            JsonNode mergedInnerData = jsonMergeUtil.mergePreservingExisting(
                                    templateInnerData,
                                    aiResponseData
                            );

                            // 병합 결과를 템플릿 구조에 설정
                            templateNode.set(fieldName, mergedInnerData);

                            ReportSection newSection = ReportSection.builder()
                                    .emergencyReport(emergencyReport)
                                    .type(sectionType)
                                    .data(templateNode)
                                    .version(1)
                                    .build();
                            reportSectionRepository.save(newSection);
                            log.debug("새 섹션 생성 완료 - 타입: {}", sectionType);
                        }
                );
    }

    /**
     * AI 응답을 DB 스키마에 맞게 변환
     * - DISPATCH: symptoms.disease -> symptoms.pain
     * - INCIDENT_TYPE: subCategory -> subCategory_xxx
     *
     * @param aiData AI 응답 데이터
     * @param sectionType 섹션 타입
     * @return 변환된 데이터
     */
    private JsonNode transformAiResponseToDbSchema(JsonNode aiData, ReportSectionType sectionType) {
        if (!aiData.isObject()) {
            return aiData;
        }

        ObjectNode transformedData = (ObjectNode) aiData.deepCopy();

        switch (sectionType) {
            case DISPATCH -> transformDispatchData(transformedData);
            case INCIDENT_TYPE -> transformIncidentTypeData(transformedData);
            // 나머지 타입은 변환 없이 그대로 반환
            default -> {}
        }

        return transformedData;
    }

    /**
     * DISPATCH 데이터 변환
     * - symptoms.disease -> symptoms.pain
     * - 배열 요소에서 value가 null인 경우 value 필드 제거
     */
    private void transformDispatchData(ObjectNode dispatchData) {
        if (dispatchData.has("symptoms")) {
            ObjectNode symptoms = (ObjectNode) dispatchData.get("symptoms");

            // disease 필드가 있으면 pain으로 이름 변경
            if (symptoms.has("disease")) {
                JsonNode diseaseArray = symptoms.get("disease");
                symptoms.remove("disease");

                // 배열 요소 정리 (value가 null이면 제거)
                JsonNode cleanedArray = cleanSymptomArray(diseaseArray);
                symptoms.set("pain", cleanedArray);
                log.debug("DISPATCH 변환: symptoms.disease -> symptoms.pain");
            }

            // trauma 배열 정리
            if (symptoms.has("trauma")) {
                JsonNode traumaArray = symptoms.get("trauma");
                symptoms.set("trauma", cleanSymptomArray(traumaArray));
            }

            // otherSymptoms 배열 정리
            if (symptoms.has("otherSymptoms")) {
                JsonNode otherArray = symptoms.get("otherSymptoms");
                symptoms.set("otherSymptoms", cleanSymptomArray(otherArray));
            }
        }
    }

    /**
     * 증상 배열 정리 - value가 null이거나 빈 문자열이면 value 필드 제거
     *
     * @param arrayNode 증상 배열
     * @return 정리된 배열
     */
    private JsonNode cleanSymptomArray(JsonNode arrayNode) {
        if (!arrayNode.isArray()) {
            return arrayNode;
        }

        var cleanedArray = objectMapper.createArrayNode();
        for (JsonNode item : arrayNode) {
            if (item.isObject()) {
                ObjectNode cleanedItem = ((ObjectNode) item).deepCopy();

                // value가 null이거나 빈 문자열이면 제거
                if (cleanedItem.has("value")) {
                    JsonNode valueNode = cleanedItem.get("value");
                    if (valueNode.isNull() ||
                        (valueNode.isTextual() && valueNode.asText().trim().isEmpty())) {
                        cleanedItem.remove("value");
                    }
                }

                cleanedArray.add(cleanedItem);
            } else {
                cleanedArray.add(item);
            }
        }

        return cleanedArray;
    }

    /**
     * INCIDENT_TYPE 데이터 변환
     * - subCategory -> 적절한 subCategory_xxx로 매핑
     * - category 값에 따라 다른 subCategory 필드로 저장
     */
    private void transformIncidentTypeData(ObjectNode incidentTypeData) {
        if (!incidentTypeData.has("subCategory")) {
            return;
        }

        JsonNode subCategory = incidentTypeData.get("subCategory");
        String category = incidentTypeData.has("category")
                ? incidentTypeData.get("category").asText("")
                : "";

        // subCategory의 type 필드로 어떤 subCategory_xxx에 매핑할지 결정
        String subCategoryType = subCategory.has("type")
                ? subCategory.get("type").asText("")
                : "";

        // type이 없으면 category로 판단
        if (subCategoryType.isEmpty()) {
            subCategoryType = category;
        }

        // 적절한 필드명 결정
        String targetFieldName = switch (subCategoryType) {
            case "교통사고" -> "subCategory_traffic";
            case "그 외 손상" -> "subCategory_injury";
            case "비외상성 손상" -> "subCategory_nonTrauma";
            case "질병외", "기타" -> "subCategory_other";
            default -> {
                // type을 찾을 수 없으면 기본적으로 subCategory_other 사용
                log.warn("알 수 없는 subCategory type: {}. subCategory_other로 저장합니다.", subCategoryType);
                yield "subCategory_other";
            }
        };

        // 기존 subCategory 제거하고 새 필드로 저장
        incidentTypeData.remove("subCategory");
        incidentTypeData.set(targetFieldName, subCategory);

        log.debug("INCIDENT_TYPE 변환: subCategory -> {} (type: {})", targetFieldName, subCategoryType);
    }

    /**
     * ReportSectionType을 JSON 필드명으로 변환
     *
     * @param type 섹션 타입
     * @return JSON 필드명 (예: PATIENT_INFO -> "patientInfo")
     */
    private String getFieldNameForType(ReportSectionType type) {
        return switch (type) {
            case PATIENT_INFO -> "patientInfo";
            case DISPATCH -> "dispatch";
            case INCIDENT_TYPE -> "incidentType";
            case ASSESSMENT -> "assessment";
            case TREATMENT -> "treatment";
            case MEDICAL_GUIDANCE -> "medicalGuidance";
            case TRANSPORT -> "transport";
            case DETAIL_REPORT -> "detailReport";
        };
    }
}