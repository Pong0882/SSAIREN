package com.ssairen.domain.hospital.repository;

import com.ssairen.domain.hospital.entity.HospitalSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 병원 선택 Repository
 */
@Repository
public interface HospitalSelectionRepository extends JpaRepository<HospitalSelection, Integer> {
}
