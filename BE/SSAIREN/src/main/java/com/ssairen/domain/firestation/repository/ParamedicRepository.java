package com.ssairen.domain.firestation.repository;

import com.ssairen.domain.firestation.entity.Paramedic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 구급대원 Repository
 */
@Repository
public interface ParamedicRepository extends JpaRepository<Paramedic, Integer> {

    /**
     * 학번으로 구급대원 조회
     */
    Optional<Paramedic> findByStudentNumber(String studentNumber);

    /**
     * 학번 존재 여부 확인
     */
    boolean existsByStudentNumber(String studentNumber);
}