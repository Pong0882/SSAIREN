package com.ssairen.domain.emergency.repository;

import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.entity.EmergencyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 구급일지 Repository
 */
@Repository
public interface EmergencyReportRepository extends JpaRepository<EmergencyReport, Long> {

    /**
     * 특정 출동지령에 대한 구급일지 존재 여부 확인
     */
    boolean existsByDispatch(Dispatch dispatch);
}
