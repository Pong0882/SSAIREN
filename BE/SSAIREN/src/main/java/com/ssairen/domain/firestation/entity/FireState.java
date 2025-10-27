package com.ssairen.domain.firestation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fire_states")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FireState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 20)
    private String name;
}