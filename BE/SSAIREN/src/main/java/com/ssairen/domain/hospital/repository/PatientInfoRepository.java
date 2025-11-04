package com.ssairen.domain.hospital.repository;

import com.ssairen.domain.hospital.entity.PatientInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 환자 정보 Repository
 * emergencyReportId가 PK이므로 findById(emergencyReportId) 사용
 */
@Repository
public interface PatientInfoRepository extends JpaRepository<PatientInfo, Long> {
    // emergencyReportId가 PK이므로 기본 findById() 메서드 사용
    // Optional<PatientInfo> findById(Long emergencyReportId);
}
