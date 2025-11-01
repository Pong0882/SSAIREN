package com.ssairen.domain.emergency.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssairen.domain.common.entity.BaseEntity;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "report_sections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ReportSection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emergency_report_id", nullable = false)
    private EmergencyReport emergencyReport;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ReportSectionType type;

    @Type(JsonBinaryType.class)
    @Column(name = "data", nullable = false, columnDefinition = "jsonb")
    private JsonNode data;

    @Column(name = "version", nullable = false)
    private Integer version;
}