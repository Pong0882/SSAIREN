package com.ssairen.domain.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.entity.ReportSection;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import com.ssairen.domain.emergency.repository.ReportSectionRepository;
import com.ssairen.domain.emergency.util.JsonMergeUtil;
import com.ssairen.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiResponseToReportSectionServiceTest {

    @Mock
    private ReportSectionRepository reportSectionRepository;

    @Mock
    private JsonMergeUtil jsonMergeUtil;

    @InjectMocks
    private AiResponseToReportSectionService service;

    private ObjectMapper objectMapper;
    private EmergencyReport mockEmergencyReport;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Inject real ObjectMapper to service using reflection
        try {
            var field = AiResponseToReportSectionService.class.getDeclaredField("objectMapper");
            field.setAccessible(true);
            field.set(service, objectMapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mockEmergencyReport = EmergencyReport.builder()
                .id(1L)
                .isCompleted(false)
                .build();
    }

    @Test
    @DisplayName("AI 응답 저장 - 성공")
    void saveAiResponseToReportSections_success() throws Exception {
        // given
        String aiResponseJson = """
                {
                    "ReportSectionType": {
                        "patientInfo": {
                            "name": "홍길동",
                            "age": 45
                        },
                        "dispatch": {
                            "location": "서울시 강남구"
                        }
                    }
                }
                """;

        Object aiResponse = objectMapper.readTree(aiResponseJson);

        when(reportSectionRepository.findByEmergencyReportAndType(any(), any()))
                .thenReturn(Optional.empty());
        when(jsonMergeUtil.mergePreservingExisting(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        // when
        int savedCount = service.saveAiResponseToReportSections(aiResponse, mockEmergencyReport);

        // then
        assertThat(savedCount).isEqualTo(2);
        verify(reportSectionRepository, times(2)).save(any(ReportSection.class));
    }

    @Test
    @DisplayName("AI 응답 저장 - ReportSectionType 없음")
    void saveAiResponseToReportSections_noReportSectionType() throws Exception {
        // given
        String aiResponseJson = """
                {
                    "data": {
                        "patientInfo": {
                            "name": "홍길동"
                        }
                    }
                }
                """;

        Object aiResponse = objectMapper.readTree(aiResponseJson);

        // when
        int savedCount = service.saveAiResponseToReportSections(aiResponse, mockEmergencyReport);

        // then
        assertThat(savedCount).isEqualTo(0);
        verify(reportSectionRepository, never()).save(any());
    }

    @Test
    @DisplayName("AI 응답 저장 - 빈 섹션 데이터는 저장하지 않음")
    void saveAiResponseToReportSections_emptySection() throws Exception {
        // given
        String aiResponseJson = """
                {
                    "ReportSectionType": {
                        "patientInfo": {},
                        "dispatch": {
                            "location": "서울시 강남구"
                        }
                    }
                }
                """;

        Object aiResponse = objectMapper.readTree(aiResponseJson);

        when(reportSectionRepository.findByEmergencyReportAndType(any(), any()))
                .thenReturn(Optional.empty());
        when(jsonMergeUtil.mergePreservingExisting(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        // when
        int savedCount = service.saveAiResponseToReportSections(aiResponse, mockEmergencyReport);

        // then
        assertThat(savedCount).isEqualTo(1);
        verify(reportSectionRepository, times(1)).save(any(ReportSection.class));
    }

    @Test
    @DisplayName("AI 응답 저장 - 기존 섹션이 있으면 업데이트")
    void saveAiResponseToReportSections_updateExisting() throws Exception {
        // given
        String aiResponseJson = """
                {
                    "ReportSectionType": {
                        "patientInfo": {
                            "name": "홍길동",
                            "age": 45
                        }
                    }
                }
                """;

        Object aiResponse = objectMapper.readTree(aiResponseJson);

        JsonNode existingData = objectMapper.readTree("""
                {
                    "schemaVersion": 1,
                    "patientInfo": {
                        "name": "김철수"
                    }
                }
                """);

        ReportSection existingSection = ReportSection.builder()
                .id(1L)
                .emergencyReport(mockEmergencyReport)
                .type(ReportSectionType.PATIENT_INFO)
                .data(existingData)
                .version(1)
                .build();

        when(reportSectionRepository.findByEmergencyReportAndType(
                eq(mockEmergencyReport), eq(ReportSectionType.PATIENT_INFO)))
                .thenReturn(Optional.of(existingSection));
        when(jsonMergeUtil.mergePreservingExisting(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        // when
        int savedCount = service.saveAiResponseToReportSections(aiResponse, mockEmergencyReport);

        // then
        assertThat(savedCount).isEqualTo(1);
        verify(reportSectionRepository, times(1)).save(eq(existingSection));
    }

    @Test
    @DisplayName("AI 응답 저장 - 예외 발생 시 CustomException")
    void saveAiResponseToReportSections_exception() throws Exception {
        // given
        String aiResponseJson = """
                {
                    "ReportSectionType": {
                        "patientInfo": {
                            "name": "홍길동"
                        }
                    }
                }
                """;

        Object aiResponse = objectMapper.readTree(aiResponseJson);

        when(reportSectionRepository.findByEmergencyReportAndType(any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        // when & then
        assertThatThrownBy(() -> service.saveAiResponseToReportSections(aiResponse, mockEmergencyReport))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("AI 응답을 ReportSection으로 변환하는 중 오류가 발생했습니다");
    }

    @Test
    @DisplayName("DISPATCH 데이터 변환 - disease를 pain으로 변환")
    void transformDispatchData_diseaseToPain() throws Exception {
        // given
        String aiResponseJson = """
                {
                    "ReportSectionType": {
                        "dispatch": {
                            "symptoms": {
                                "disease": [
                                    {"name": "복통", "value": "심함"},
                                    {"name": "두통", "value": null}
                                ]
                            }
                        }
                    }
                }
                """;

        Object aiResponse = objectMapper.readTree(aiResponseJson);

        when(reportSectionRepository.findByEmergencyReportAndType(any(), any()))
                .thenReturn(Optional.empty());
        when(jsonMergeUtil.mergePreservingExisting(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        // when
        int savedCount = service.saveAiResponseToReportSections(aiResponse, mockEmergencyReport);

        // then
        assertThat(savedCount).isEqualTo(1);
        verify(reportSectionRepository).save(argThat(section -> {
            JsonNode data = section.getData();
            JsonNode dispatch = data.get("dispatch");
            return dispatch != null &&
                   dispatch.has("symptoms") &&
                   dispatch.get("symptoms").has("pain") &&
                   !dispatch.get("symptoms").has("disease");
        }));
    }

    @Test
    @DisplayName("INCIDENT_TYPE 데이터 변환 - subCategory를 적절한 필드로 변환")
    void transformIncidentTypeData_subCategoryMapping() throws Exception {
        // given
        String aiResponseJson = """
                {
                    "ReportSectionType": {
                        "incidentType": {
                            "category": "교통사고",
                            "subCategory": {
                                "type": "교통사고",
                                "detail": "승용차 추돌"
                            }
                        }
                    }
                }
                """;

        Object aiResponse = objectMapper.readTree(aiResponseJson);

        when(reportSectionRepository.findByEmergencyReportAndType(any(), any()))
                .thenReturn(Optional.empty());
        when(jsonMergeUtil.mergePreservingExisting(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        // when
        int savedCount = service.saveAiResponseToReportSections(aiResponse, mockEmergencyReport);

        // then
        assertThat(savedCount).isEqualTo(1);
        verify(reportSectionRepository).save(argThat(section -> {
            JsonNode data = section.getData();
            JsonNode incidentType = data.get("incidentType");
            return incidentType != null &&
                   incidentType.has("subCategory_traffic") &&
                   !incidentType.has("subCategory");
        }));
    }

    @Test
    @DisplayName("모든 섹션 타입 저장 - 성공")
    void saveAiResponseToReportSections_allSectionTypes() throws Exception {
        // given
        String aiResponseJson = """
                {
                    "ReportSectionType": {
                        "patientInfo": {"name": "홍길동"},
                        "dispatch": {"location": "서울"},
                        "incidentType": {"category": "질병"},
                        "assessment": {"vital": "정상"},
                        "treatment": {"medication": "진통제"},
                        "medicalGuidance": {"advice": "안정"},
                        "transport": {"destination": "서울대병원"},
                        "detailReport": {"summary": "상세내용"}
                    }
                }
                """;

        Object aiResponse = objectMapper.readTree(aiResponseJson);

        when(reportSectionRepository.findByEmergencyReportAndType(any(), any()))
                .thenReturn(Optional.empty());
        when(jsonMergeUtil.mergePreservingExisting(any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(1));

        // when
        int savedCount = service.saveAiResponseToReportSections(aiResponse, mockEmergencyReport);

        // then
        assertThat(savedCount).isEqualTo(8);
        verify(reportSectionRepository, times(8)).save(any(ReportSection.class));
    }
}
