package com.ssairen.domain.ai.entity;

import com.ssairen.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "llm_summaries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LlmSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stt_id", nullable = false)
    private SttTranscript sttTranscript;

    @Column(name = "data", nullable = false, columnDefinition = "TEXT")
    private String data;
}