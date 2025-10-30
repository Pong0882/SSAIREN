package com.ssairen.domain.hospital.repository;

import com.ssairen.domain.hospital.entity.HospitalSelection;
import com.ssairen.domain.hospital.enums.HospitalSelectionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 병원 선택 Repository
 */
@Repository
public interface HospitalSelectionRepository extends JpaRepository<HospitalSelection, Integer> {

    /**
     * ID로 HospitalSelection 조회 (Hospital, EmergencyReport Fetch Join)
     */
    @Query("SELECT hs FROM HospitalSelection hs " +
            "JOIN FETCH hs.hospital " +
            "JOIN FETCH hs.emergencyReport " +
            "WHERE hs.id = :id")
    Optional<HospitalSelection> findByIdWithHospitalAndEmergencyReport(@Param("id") Integer id);

    /**
     * 같은 EmergencyReport를 가진 다른 HospitalSelection들을 COMPLETED로 업데이트
     */
    @Modifying
    @Query("UPDATE HospitalSelection hs " +
            "SET hs.status = :status, hs.responseAt = :responseAt " +
            "WHERE hs.emergencyReport.id = :emergencyReportId " +
            "AND hs.id != :excludeId " +
            "AND hs.status = 'PENDING'")
    int updateOtherSelectionsToCompleted(
            @Param("emergencyReportId") Long emergencyReportId,
            @Param("excludeId") Integer excludeId,
            @Param("status") HospitalSelectionStatus status,
            @Param("responseAt") LocalDateTime responseAt
    );

    /**
     * EmergencyReport ID로 모든 HospitalSelection 조회
     */
    List<HospitalSelection> findByEmergencyReportId(Long emergencyReportId);
}
