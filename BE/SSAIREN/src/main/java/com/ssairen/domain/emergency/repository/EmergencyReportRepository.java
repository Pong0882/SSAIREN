package com.ssairen.domain.emergency.repository;

import com.ssairen.domain.emergency.entity.EmergencyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 구급일지 Repository
 */
@Repository
public interface EmergencyReportRepository extends JpaRepository<EmergencyReport, Long> {
}
