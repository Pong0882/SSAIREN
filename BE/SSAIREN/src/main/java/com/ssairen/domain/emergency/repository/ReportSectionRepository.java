package com.ssairen.domain.emergency.repository;

import com.ssairen.domain.emergency.entity.EmergencyReport;
import com.ssairen.domain.emergency.entity.ReportSection;
import com.ssairen.domain.emergency.enums.ReportSectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 구급일지 섹션 Repository
 */
@Repository
public interface ReportSectionRepository extends JpaRepository<ReportSection, Long> {

    /**
     * 특정 구급일지의 특정 타입 섹션 존재 여부 확인 (중복 생성 방지용)
     *
     * @param emergencyReport 구급일지
     * @param type 섹션 타입
     * @return 존재 여부
     */
    boolean existsByEmergencyReportAndType(EmergencyReport emergencyReport, ReportSectionType type);
}
