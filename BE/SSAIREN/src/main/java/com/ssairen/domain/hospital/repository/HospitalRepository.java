package com.ssairen.domain.hospital.repository;

import com.ssairen.domain.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 병원 Repository
 */
@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Integer> {

    /**
     * 병원 이름으로 병원 조회
     *
     * @param name 병원 이름
     * @return Optional<Hospital>
     */
    Optional<Hospital> findByName(String name);
}
