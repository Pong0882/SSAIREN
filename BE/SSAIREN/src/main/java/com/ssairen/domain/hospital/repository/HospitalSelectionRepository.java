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

    /**
     * EmergencyReport ID로 모든 HospitalSelection 조회 (Hospital Fetch Join)
     * N+1 문제 방지를 위해 Hospital도 함께 조회
     */
    @Query("SELECT hs FROM HospitalSelection hs " +
            "JOIN FETCH hs.hospital " +
            "WHERE hs.emergencyReport.id = :emergencyReportId " +
            "ORDER BY hs.createdAt ASC")
    List<HospitalSelection> findByEmergencyReportIdWithHospital(@Param("emergencyReportId") Long emergencyReportId);

    /**
     * 특정 병원의 PENDING 상태인 요청 목록 조회 (EmergencyReport Fetch Join)
     */
    @Query("SELECT hs FROM HospitalSelection hs " +
            "JOIN FETCH hs.emergencyReport " +
            "WHERE hs.hospital.id = :hospitalId " +
            "AND hs.status = :status " +
            "ORDER BY hs.createdAt DESC")
    List<HospitalSelection> findByHospitalIdAndStatus(
            @Param("hospitalId") Integer hospitalId,
            @Param("status") HospitalSelectionStatus status
    );

    /**
     * 특정 병원이 수용한 환자 목록 조회 (ACCEPTED, ARRIVED 상태)
     * EmergencyReport Fetch Join
     */
    @Query("SELECT hs FROM HospitalSelection hs " +
            "JOIN FETCH hs.emergencyReport " +
            "WHERE hs.hospital.id = :hospitalId " +
            "AND hs.status IN ('ACCEPTED', 'ARRIVED') " +
            "ORDER BY hs.responseAt DESC")
    List<HospitalSelection> findAcceptedPatientsByHospitalId(
            @Param("hospitalId") Integer hospitalId
    );

    /**
     * 특정 병원의 환자 목록 조회 (페이지네이션 + 필터)
     * EmergencyReport Fetch Join
     *
     * @param hospitalId 병원 ID
     * @param statuses 필터링할 상태 목록 (ACCEPTED, ARRIVED)
     * @param offset 시작 위치 (페이지 * 크기)
     * @param limit 페이지 크기
     */
    @Query("SELECT hs FROM HospitalSelection hs " +
            "JOIN FETCH hs.emergencyReport " +
            "WHERE hs.hospital.id = :hospitalId " +
            "AND hs.status IN :statuses " +
            "ORDER BY hs.responseAt DESC " +
            "LIMIT :limit OFFSET :offset")
    List<HospitalSelection> findPatientsByHospitalIdWithPagination(
            @Param("hospitalId") Integer hospitalId,
            @Param("statuses") List<HospitalSelectionStatus> statuses,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    /**
     * 특정 병원의 환자 전체 개수 조회 (필터)
     *
     * @param hospitalId 병원 ID
     * @param statuses 필터링할 상태 목록
     */
    @Query("SELECT COUNT(hs) FROM HospitalSelection hs " +
            "WHERE hs.hospital.id = :hospitalId " +
            "AND hs.status IN :statuses")
    long countPatientsByHospitalIdAndStatuses(
            @Param("hospitalId") Integer hospitalId,
            @Param("statuses") List<HospitalSelectionStatus> statuses
    );

    /**
     * 병원이 특정 환자를 수용했는지 확인 (ACCEPTED 또는 ARRIVED 상태)
     */
    @Query("SELECT COUNT(hs) > 0 FROM HospitalSelection hs " +
            "WHERE hs.hospital.id = :hospitalId " +
            "AND hs.emergencyReport.id = :emergencyReportId " +
            "AND hs.status IN ('ACCEPTED', 'ARRIVED')")
    boolean existsByHospitalIdAndEmergencyReportIdAndAccepted(
            @Param("hospitalId") Integer hospitalId,
            @Param("emergencyReportId") Long emergencyReportId
    );

    /**
     * 병원 ID, 구급일지 ID, 상태로 HospitalSelection 조회
     */
    @Query("SELECT hs FROM HospitalSelection hs " +
            "WHERE hs.hospital.id = :hospitalId " +
            "AND hs.emergencyReport.id = :emergencyReportId " +
            "AND hs.status = :status")
    Optional<HospitalSelection> findByHospitalIdAndEmergencyReportIdAndStatus(
            @Param("hospitalId") Integer hospitalId,
            @Param("emergencyReportId") Long emergencyReportId,
            @Param("status") HospitalSelectionStatus status
    );

    /**
     * 특정 EmergencyReport의 다른 HospitalSelection들 조회 (PENDING 상태만)
     * ACCEPTED 응답 시 COMPLETED로 변경할 대상들을 조회 (Hospital Fetch Join)
     */
    @Query("SELECT hs FROM HospitalSelection hs " +
            "JOIN FETCH hs.hospital " +
            "WHERE hs.emergencyReport.id = :emergencyReportId " +
            "AND hs.id != :excludeId " +
            "AND hs.status = 'PENDING'")
    List<HospitalSelection> findByEmergencyReportIdAndStatusNotIn(
            @Param("emergencyReportId") Long emergencyReportId,
            @Param("excludeId") Integer excludeId
    );
}
