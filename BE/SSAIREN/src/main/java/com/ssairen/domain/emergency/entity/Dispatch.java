package com.ssairen.domain.emergency.entity;

import com.ssairen.domain.common.entity.BaseEntity;
import com.ssairen.domain.firestation.entity.FireState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispatches")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Dispatch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "disaster_number", length = 50)
    private String disasterNumber;

    @Column(name = "disaster_type", length = 50)
    private String disasterType;

    @Column(name = "disaster_subtype", length = 50)
    private String disasterSubtype;

    @Column(name = "reporter_name", length = 20)
    private String reporterName;

    @Column(name = "reporter_phone", length = 50)
    private String reporterPhone;

    @Column(name = "location_address", length = 50)
    private String locationAddress;

    @Column(name = "incident_description", length = 100)
    private String incidentDescription;

    @Column(name = "dispatch_level", length = 50)
    private String dispatchLevel;

    @Column(name = "dispatch_order")
    private Integer dispatchOrder;

    @Column(name = "dispatch_station", length = 50)
    private String dispatchStation;

    @Column(name = "date")
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fire_state_id", nullable = false)
    private FireState fireState;
}
