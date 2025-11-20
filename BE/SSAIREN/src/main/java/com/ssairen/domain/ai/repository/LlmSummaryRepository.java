package com.ssairen.domain.ai.repository;

import com.ssairen.domain.ai.entity.LlmSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LlmSummaryRepository extends JpaRepository<LlmSummary, Long> {
}