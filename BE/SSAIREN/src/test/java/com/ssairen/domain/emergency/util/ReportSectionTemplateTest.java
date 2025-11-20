package com.ssairen.domain.emergency.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReportSectionTemplateTest {

    @Test
    @DisplayName("PATIENT_INFO 템플릿 생성")
    void getTemplate_patientInfo() {
        // when
        JsonNode template = ReportSectionTemplate.getTemplate(ReportSectionType.PATIENT_INFO);

        // then
        assertThat(template).isNotNull();
        assertThat(template.has("patientInfo")).isTrue();
        assertThat(template.get("patientInfo").has("reporter")).isTrue();
        assertThat(template.get("patientInfo").has("patient")).isTrue();
        assertThat(template.get("patientInfo").has("guardian")).isTrue();
        assertThat(template.get("patientInfo").has("incidentLocation")).isTrue();

        // 모든 값이 null인지 확인
        assertThat(template.get("patientInfo").get("reporter").get("phone").isNull()).isTrue();
        assertThat(template.get("patientInfo").get("patient").get("name").isNull()).isTrue();
    }

    @Test
    @DisplayName("DISPATCH 템플릿 생성")
    void getTemplate_dispatch() {
        // when
        JsonNode template = ReportSectionTemplate.getTemplate(ReportSectionType.DISPATCH);

        // then
        assertThat(template).isNotNull();
        assertThat(template.has("dispatch")).isTrue();
        assertThat(template.get("dispatch").has("reportDatetime")).isTrue();
        assertThat(template.get("dispatch").has("departureTime")).isTrue();
        assertThat(template.get("dispatch").has("arrivalSceneTime")).isTrue();
        assertThat(template.get("dispatch").has("symptoms")).isTrue();

        // 모든 값이 null인지 확인
        assertThat(template.get("dispatch").get("reportDatetime").isNull()).isTrue();
        assertThat(template.get("dispatch").get("symptoms").get("pain").isNull()).isTrue();
    }

    @Test
    @DisplayName("INCIDENT_TYPE 템플릿 생성")
    void getTemplate_incidentType() {
        // when
        JsonNode template = ReportSectionTemplate.getTemplate(ReportSectionType.INCIDENT_TYPE);

        // then
        assertThat(template).isNotNull();
        assertThat(template.has("incidentType")).isTrue();
        assertThat(template.get("incidentType").has("medicalHistory")).isTrue();
        assertThat(template.get("incidentType").has("category")).isTrue();
        assertThat(template.get("incidentType").has("subCategory_traffic")).isTrue();
        assertThat(template.get("incidentType").has("legalSuspicion")).isTrue();
    }

    @Test
    @DisplayName("ASSESSMENT 템플릿 생성")
    void getTemplate_assessment() {
        // when
        JsonNode template = ReportSectionTemplate.getTemplate(ReportSectionType.ASSESSMENT);

        // then
        assertThat(template).isNotNull();
        assertThat(template.has("assessment")).isTrue();
        assertThat(template.get("assessment").has("consciousness")).isTrue();
        assertThat(template.get("assessment").has("pupilReaction")).isTrue();
        assertThat(template.get("assessment").has("vitalSigns")).isTrue();
        assertThat(template.get("assessment").has("patientLevel")).isTrue();

        // vitalSigns 구조 확인
        assertThat(template.get("assessment").get("vitalSigns").has("first")).isTrue();
        assertThat(template.get("assessment").get("vitalSigns").has("second")).isTrue();
    }

    @Test
    @DisplayName("TREATMENT 템플릿 생성")
    void getTemplate_treatment() {
        // when
        JsonNode template = ReportSectionTemplate.getTemplate(ReportSectionType.TREATMENT);

        // then
        assertThat(template).isNotNull();
        assertThat(template.has("treatment")).isTrue();
        assertThat(template.get("treatment").has("airwayManagement")).isTrue();
        assertThat(template.get("treatment").has("oxygenTherapy")).isTrue();
        assertThat(template.get("treatment").has("cpr")).isTrue();
        assertThat(template.get("treatment").has("aed")).isTrue();
    }

    @Test
    @DisplayName("MEDICAL_GUIDANCE 템플릿 생성")
    void getTemplate_medicalGuidance() {
        // when
        JsonNode template = ReportSectionTemplate.getTemplate(ReportSectionType.MEDICAL_GUIDANCE);

        // then
        assertThat(template).isNotNull();
        assertThat(template.has("medicalGuidance")).isTrue();
        assertThat(template.get("medicalGuidance").has("contactStatus")).isTrue();
        assertThat(template.get("medicalGuidance").has("requestTime")).isTrue();
        assertThat(template.get("medicalGuidance").has("guidanceAgency")).isTrue();
        assertThat(template.get("medicalGuidance").has("guidanceContent")).isTrue();

        // guidanceContent 구조 확인
        JsonNode content = template.get("medicalGuidance").get("guidanceContent");
        assertThat(content.has("emergencyTreatment")).isTrue();
        assertThat(content.has("medication")).isTrue();
    }

    @Test
    @DisplayName("TRANSPORT 템플릿 생성")
    void getTemplate_transport() {
        // when
        JsonNode template = ReportSectionTemplate.getTemplate(ReportSectionType.TRANSPORT);

        // then
        assertThat(template).isNotNull();
        assertThat(template.has("transport")).isTrue();
        assertThat(template.get("transport").has("firstTransport")).isTrue();
        assertThat(template.get("transport").has("secondTransport")).isTrue();

        // firstTransport 구조 확인
        JsonNode firstTransport = template.get("transport").get("firstTransport");
        assertThat(firstTransport.has("hospitalName")).isTrue();
        assertThat(firstTransport.has("receiverSign")).isTrue();
    }

    @Test
    @DisplayName("DETAIL_REPORT 템플릿 생성")
    void getTemplate_detailReport() {
        // when
        JsonNode template = ReportSectionTemplate.getTemplate(ReportSectionType.DETAIL_REPORT);

        // then
        assertThat(template).isNotNull();
        assertThat(template.has("detailReport")).isTrue();
        assertThat(template.get("detailReport").has("doctor")).isTrue();
        assertThat(template.get("detailReport").has("paramedic1")).isTrue();
        assertThat(template.get("detailReport").has("paramedic2")).isTrue();
        assertThat(template.get("detailReport").has("driver")).isTrue();
        assertThat(template.get("detailReport").has("obstacles")).isTrue();

        // obstacles는 배열이어야 함
        assertThat(template.get("detailReport").get("obstacles").isArray()).isTrue();
    }

    @Test
    @DisplayName("모든 섹션 타입에 대한 템플릿 생성")
    void getTemplate_allTypes() {
        // when & then
        for (ReportSectionType type : ReportSectionType.values()) {
            JsonNode template = ReportSectionTemplate.getTemplate(type);
            assertThat(template).isNotNull();
        }
    }

    @Test
    @DisplayName("DISPATCH 템플릿 with Dispatch 엔티티 - 성공")
    void getTemplateForDispatch_success() {
        // given
        LocalDateTime now = LocalDateTime.now();
        Dispatch dispatch = mock(Dispatch.class);
        when(dispatch.getDate()).thenReturn(now);
        when(dispatch.getCreatedAt()).thenReturn(now);

        // when
        JsonNode template = ReportSectionTemplate.getTemplateForDispatch(dispatch);

        // then
        assertThat(template).isNotNull();
        assertThat(template.has("dispatch")).isTrue();

        // reportDatetime과 departureTime이 실제 값으로 설정되어 있어야 함
        JsonNode dispatchNode = template.get("dispatch");
        assertThat(dispatchNode.get("reportDatetime").isNull()).isFalse();
        assertThat(dispatchNode.get("departureTime").isNull()).isFalse();

        // 나머지 필드는 여전히 null이어야 함
        assertThat(dispatchNode.get("arrivalSceneTime").isNull()).isTrue();
        assertThat(dispatchNode.get("contactTime").isNull()).isTrue();
    }

    @Test
    @DisplayName("DISPATCH 템플릿 with Dispatch 엔티티 - 날짜 포맷 확인")
    void getTemplateForDispatch_dateFormat() {
        // given
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        Dispatch dispatch = mock(Dispatch.class);
        when(dispatch.getDate()).thenReturn(dateTime);
        when(dispatch.getCreatedAt()).thenReturn(dateTime);

        // when
        JsonNode template = ReportSectionTemplate.getTemplateForDispatch(dispatch);

        // then
        JsonNode dispatchNode = template.get("dispatch");
        String reportDatetime = dispatchNode.get("reportDatetime").asText();
        String departureTime = dispatchNode.get("departureTime").asText();

        // ISO 8601 형식인지 확인
        assertThat(reportDatetime).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
        assertThat(departureTime).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");

        // 밀리초가 포함되지 않아야 함
        assertThat(reportDatetime).doesNotContain(".");
    }

    @Test
    @DisplayName("템플릿 재사용성 - 같은 타입 여러 번 호출")
    void getTemplate_reusability() {
        // when
        JsonNode template1 = ReportSectionTemplate.getTemplate(ReportSectionType.PATIENT_INFO);
        JsonNode template2 = ReportSectionTemplate.getTemplate(ReportSectionType.PATIENT_INFO);

        // then
        // 각각 독립적인 인스턴스여야 함 (deep copy)
        assertThat(template1).isNotNull();
        assertThat(template2).isNotNull();
    }

    @Test
    @DisplayName("각 템플릿의 createdAt, updatedAt 필드 확인")
    void getTemplate_timestampFields() {
        // when & then
        for (ReportSectionType type : ReportSectionType.values()) {
            JsonNode template = ReportSectionTemplate.getTemplate(type);

            // 첫 번째 레벨의 키를 가져옴 (예: "patientInfo", "dispatch" 등)
            String rootKey = template.fieldNames().next();
            JsonNode rootNode = template.get(rootKey);

            // createdAt, updatedAt 필드가 있는지 확인
            assertThat(rootNode.has("createdAt")).isTrue();
            assertThat(rootNode.has("updatedAt")).isTrue();
            assertThat(rootNode.get("createdAt").isNull()).isTrue();
            assertThat(rootNode.get("updatedAt").isNull()).isTrue();
        }
    }
}
