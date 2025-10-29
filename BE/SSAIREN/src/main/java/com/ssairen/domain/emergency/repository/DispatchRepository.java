package com.ssairen.domain.emergency.repository;

import com.ssairen.domain.emergency.entity.Dispatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispatchRepository extends JpaRepository<Dispatch, Long> {
}
