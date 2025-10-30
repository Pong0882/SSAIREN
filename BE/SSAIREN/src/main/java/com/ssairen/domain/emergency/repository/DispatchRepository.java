package com.ssairen.domain.emergency.repository;

import com.ssairen.domain.emergency.entity.Dispatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    @Query(value = """
            SELECT * FROM dispatches
            WHERE fire_state_id = :fireStateId
            AND (:cursor IS NULL OR id < :cursor)
            ORDER BY id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Dispatch> findByFireStateIdWithFilters(
            @Param("fireStateId") Integer fireStateId,
            @Param("cursor") Long cursor,
            @Param("limit") int limit
    );
}
