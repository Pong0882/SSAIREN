package com.ssairen.domain.ai.repository;

import com.ssairen.domain.ai.entity.SttTranscript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * STT 녹취록 Repository
 */
@Repository
public interface SttTranscriptRepository extends JpaRepository<SttTranscript, Long> {

    /**
     * 구급일지 ID로 STT 녹취록 조회
     */
    Optional<SttTranscript> findByEmergencyReportId(Long emergencyReportId);

    /**
     * 구급일지 ID로 STT 녹취록 존재 여부 확인
     */
    boolean existsByEmergencyReportId(Long emergencyReportId);

    /**
     * 구급일지 ID 리스트로 STT 녹취록 조회
     */
    List<SttTranscript> findByEmergencyReportIdIn(List<Long> emergencyReportIds);
}
