package com.ssairen.domain.hospital.repository;

import com.ssairen.domain.hospital.entity.PatientInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 환자 정보 Repository
 */
@Repository
public interface PatientInfoRepository extends JpaRepository<PatientInfo, Long> {

    /**
     * 구급일지 ID로 환자 정보 조회
     */
    Optional<PatientInfo> findByEmergencyReportId_Id(Long emergencyReportId);
}
