package com.ssairen.domain.hospital.entity;

import com.ssairen.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

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

    @Column(name = "password", nullable = false, length = 100)
    private String password;

//    @Column(name = "type", nullable = false, length = 50)
//    private String type;
//
//    @Column(name = "address", nullable = false, length = 255)
//    private String address;
//
//    @Column(name = "phone", nullable = false, length = 20)
//    private String phone;
//
//    @Column(name = "latitude", nullable = false, precision = 10, scale = 8)
//    private BigDecimal latitude;
//
//    @Column(name = "longitude", nullable = false, precision = 11, scale = 8)
//    private BigDecimal longitude;
}
