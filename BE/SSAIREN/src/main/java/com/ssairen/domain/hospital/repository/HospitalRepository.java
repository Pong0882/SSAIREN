package com.ssairen.domain.hospital.repository;

import com.ssairen.domain.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 병원 Repository
 */
@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Integer> {
}
