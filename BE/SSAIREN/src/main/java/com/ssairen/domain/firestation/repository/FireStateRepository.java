package com.ssairen.domain.firestation.repository;

import com.ssairen.domain.firestation.entity.FireState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 소방서 Repository
 */
@Repository
public interface FireStateRepository extends JpaRepository<FireState, Integer> {
}
