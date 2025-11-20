package com.ssairen.domain.firestation.entity;

import com.ssairen.domain.common.entity.BaseEntity;
import com.ssairen.domain.firestation.enums.ParamedicRank;
import com.ssairen.domain.firestation.enums.ParamedicStatus;
import com.ssairen.global.annotation.ExcludeFromLogging;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "paramedics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Paramedic extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fire_state_id", nullable = false)
    private FireState fireState;

    @Column(name = "student_number", nullable = false, unique = true, length = 20)
    private String studentNumber;

    @ExcludeFromLogging
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "rank", nullable = false)
    private ParamedicRank rank;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParamedicStatus status;
}
