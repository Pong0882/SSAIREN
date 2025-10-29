package com.ssairen.domain.hospital.entity;

import com.ssairen.domain.common.entity.BaseEntity;
import com.ssairen.global.annotation.ExcludeFromLogging;
import jakarta.persistence.*;
import lombok.*;

/**
 * 병원 엔티티
 */
@Entity
@Table(name = "hospitals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Hospital extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "official_name", nullable = false, length = 100)
    private String officialName;

    @ExcludeFromLogging
    @Column(name = "password", nullable = false, length = 100)
    private String password;
}
