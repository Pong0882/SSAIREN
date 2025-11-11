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
     * 특정 병원의 환자 전체 개수 조회 (필터 + 기간)
     *
     * @param hospitalId 병원 ID
     * @param statuses 필터링할 상태 목록
     * @param startDateTime 시작 날짜 (null이면 전체 기간)
     */
    @Query(value = "SELECT COUNT(*) FROM hospital_selection hs " +
            "WHERE hs.hospital_id = :hospitalId " +
            "AND hs.status IN (:statuses) " +
            "AND hs.response_at >= COALESCE(:startDateTime, '1900-01-01'::timestamp)",
            nativeQuery = true)
    long countPatientsByHospitalIdAndStatusesAndDateRange(
            @Param("hospitalId") Integer hospitalId,
            @Param("statuses") List<String> statuses,
            @Param("startDateTime") LocalDateTime startDateTime
    );

    /**
     * 특정 병원의 환자 목록 조회 (페이지네이션 + 필터 + 기간)
     * EmergencyReport Fetch Join은 별도 쿼리로 처리
     *
     * @param hospitalId 병원 ID
     * @param statuses 필터링할 상태 목록 (ACCEPTED, ARRIVED)
     * @param startDateTime 시작 날짜 (null이면 전체 기간)
     * @param offset 시작 위치 (페이지 * 크기)
     * @param limit 페이지 크기
     */
    @Query(value = "SELECT * FROM hospital_selection hs " +
            "WHERE hs.hospital_id = :hospitalId " +
            "AND hs.status IN (:statuses) " +
            "AND hs.response_at >= COALESCE(:startDateTime, '1900-01-01'::timestamp) " +
            "ORDER BY hs.response_at DESC " +
            "LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<HospitalSelection> findPatientsByHospitalIdWithPaginationAndDateRange(
            @Param("hospitalId") Integer hospitalId,
            @Param("statuses") List<String> statuses,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("offset") int offset,
            @Param("limit") int limit
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

    // ===== 통계 관련 쿼리 =====

    /**
     * 요일별 환자 수용 건수 조회 (ACCEPTED 상태만)
     * 0 = 일요일, 1 = 월요일, ..., 6 = 토요일
     *
     * @param hospitalId 병원 ID
     * @param startDateTime 시작 날짜
     * @param endDateTime 종료 날짜
     * @return 요일별 건수 (0~6)
     */
    @Query(value = "SELECT EXTRACT(DOW FROM hs.response_at) AS day_of_week, COUNT(*) " +
            "FROM hospital_selection hs " +
            "WHERE hs.hospital_id = :hospitalId " +
            "AND hs.status = 'ACCEPTED' " +
            "AND hs.response_at >= :startDateTime " +
            "AND hs.response_at < :endDateTime " +
            "GROUP BY day_of_week " +
            "ORDER BY day_of_week",
            nativeQuery = true)
    List<Object[]> countByDayOfWeek(
            @Param("hospitalId") Integer hospitalId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * 시간대별 환자 수용 건수 조회 (ACCEPTED 상태만)
     * 0 ~ 23시
     *
     * @param hospitalId 병원 ID
     * @param startDateTime 시작 날짜
     * @param endDateTime 종료 날짜
     * @return 시간대별 건수 (0~23)
     */
    @Query(value = "SELECT EXTRACT(HOUR FROM hs.response_at) AS hour, COUNT(*) " +
            "FROM hospital_selection hs " +
            "WHERE hs.hospital_id = :hospitalId " +
            "AND hs.status = 'ACCEPTED' " +
            "AND hs.response_at >= :startDateTime " +
            "AND hs.response_at < :endDateTime " +
            "GROUP BY hour " +
            "ORDER BY hour",
            nativeQuery = true)
    List<Object[]> countByHour(
            @Param("hospitalId") Integer hospitalId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * 기간 내 총 수용 건수 조회 (ACCEPTED 상태만)
     *
     * @param hospitalId 병원 ID
     * @param startDateTime 시작 날짜
     * @param endDateTime 종료 날짜
     * @return 총 건수
     */
    @Query("SELECT COUNT(hs) FROM HospitalSelection hs " +
            "WHERE hs.hospital.id = :hospitalId " +
            "AND hs.status = 'ACCEPTED' " +
            "AND hs.responseAt >= :startDateTime " +
            "AND hs.responseAt < :endDateTime")
    long countByHospitalIdAndPeriod(
            @Param("hospitalId") Integer hospitalId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    // ===== 환자 통계 관련 쿼리 =====

    /**
     * 성별별 환자 수용 건수 조회 (ACCEPTED 상태만)
     *
     * @param hospitalId 병원 ID
     * @param startDateTime 시작 날짜
     * @param endDateTime 종료 날짜
     * @return 성별별 건수 (M, F)
     */
    @Query(value = "SELECT pi.gender, COUNT(*) " +
            "FROM hospital_selection hs " +
            "INNER JOIN emergency_reports er ON hs.emergency_report_id = er.id " +
            "INNER JOIN patient_info pi ON er.id = pi.emergency_report_id " +
            "WHERE hs.hospital_id = :hospitalId " +
            "AND hs.status = 'ACCEPTED' " +
            "AND hs.response_at >= :startDateTime " +
            "AND hs.response_at < :endDateTime " +
            "GROUP BY pi.gender",
            nativeQuery = true)
    List<Object[]> countByGender(
            @Param("hospitalId") Integer hospitalId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * 연령대별 환자 수용 건수 조회 (ACCEPTED 상태만)
     * 10년 단위: 0-9, 10-19, 20-29, ..., 80+
     *
     * @param hospitalId 병원 ID
     * @param startDateTime 시작 날짜
     * @param endDateTime 종료 날짜
     * @return 연령대별 건수
     */
    @Query(value = "SELECT " +
            "  CASE " +
            "    WHEN pi.age < 10 THEN '0-9' " +
            "    WHEN pi.age < 20 THEN '10-19' " +
            "    WHEN pi.age < 30 THEN '20-29' " +
            "    WHEN pi.age < 40 THEN '30-39' " +
            "    WHEN pi.age < 50 THEN '40-49' " +
            "    WHEN pi.age < 60 THEN '50-59' " +
            "    WHEN pi.age < 70 THEN '60-69' " +
            "    WHEN pi.age < 80 THEN '70-79' " +
            "    ELSE '80+' " +
            "  END AS age_group, " +
            "  COUNT(*) " +
            "FROM hospital_selection hs " +
            "INNER JOIN emergency_reports er ON hs.emergency_report_id = er.id " +
            "INNER JOIN patient_info pi ON er.id = pi.emergency_report_id " +
            "WHERE hs.hospital_id = :hospitalId " +
            "AND hs.status = 'ACCEPTED' " +
            "AND hs.response_at >= :startDateTime " +
            "AND hs.response_at < :endDateTime " +
            "GROUP BY age_group " +
            "ORDER BY age_group",
            nativeQuery = true)
    List<Object[]> countByAgeGroup(
            @Param("hospitalId") Integer hospitalId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );

    /**
     * 의식 상태별 환자 수용 건수 조회 (ACCEPTED 상태만)
     *
     * @param hospitalId 병원 ID
     * @param startDateTime 시작 날짜
     * @param endDateTime 종료 날짜
     * @return 의식 상태별 건수 (ALERT, VERBAL, PAIN, UNRESPONSIVE)
     */
    @Query(value = "SELECT pi.mental_status, COUNT(*) " +
            "FROM hospital_selection hs " +
            "INNER JOIN emergency_reports er ON hs.emergency_report_id = er.id " +
            "INNER JOIN patient_info pi ON er.id = pi.emergency_report_id " +
            "WHERE hs.hospital_id = :hospitalId " +
            "AND hs.status = 'ACCEPTED' " +
            "AND hs.response_at >= :startDateTime " +
            "AND hs.response_at < :endDateTime " +
            "GROUP BY pi.mental_status",
            nativeQuery = true)
    List<Object[]> countByMentalStatus(
            @Param("hospitalId") Integer hospitalId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
