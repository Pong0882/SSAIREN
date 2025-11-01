package com.ssairen.domain.emergency.repository;

import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 구급일지 Repository
 */
@Repository
public interface EmergencyReportRepository extends JpaRepository<EmergencyReport, Long> {

    /**
     * 특정 출동지령에 대한 구급일지 존재 여부 확인
     */
    boolean existsByDispatch(Dispatch dispatch);

    /**
     * 특정 구급대원의 모든 구급일지 조회
     */
    @Query("SELECT er FROM EmergencyReport er " +
           "JOIN FETCH er.paramedic p " +
           "JOIN FETCH er.dispatch d " +
           "JOIN FETCH d.fireState " +
           "JOIN FETCH p.fireState " +
           "WHERE p.id = :paramedicId")
    List<EmergencyReport> findByParamedicIdWithFetchJoin(@Param("paramedicId") Integer paramedicId);
}
