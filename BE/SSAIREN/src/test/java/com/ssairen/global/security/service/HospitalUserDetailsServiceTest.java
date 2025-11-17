package com.ssairen.global.security.service;

import com.ssairen.domain.hospital.entity.Hospital;
import com.ssairen.domain.hospital.repository.HospitalRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospitalUserDetailsServiceTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @InjectMocks
    private HospitalUserDetailsService service;

    private String hospitalName;
    private Integer hospitalId;
    private Hospital hospital;

    @BeforeEach
    void setUp() {
        hospitalName = "서울대병원";
        hospitalId = 1;
        hospital = Hospital.builder()
                .id(hospitalId)
                .name(hospitalName)
                .officialName("hospital1")
                .password("encodedPassword123")
                .build();
    }

    @Test
    @DisplayName("병원 이름으로 병원 조회 - 성공")
    void loadUserByUsername_success() {
        // given
        when(hospitalRepository.findByName(hospitalName))
                .thenReturn(Optional.of(hospital));

        // when
        UserDetails userDetails = service.loadUserByUsername(hospitalName);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUserPrincipal.class);
        CustomUserPrincipal principal = (CustomUserPrincipal) userDetails;
        assertThat(principal.getId()).isEqualTo(hospitalId);
        assertThat(principal.getUsername()).isEqualTo(hospitalName); // Hospital.getName()을 username으로 사용
        assertThat(principal.getPassword()).isEqualTo("encodedPassword123");

        verify(hospitalRepository).findByName(hospitalName);
    }

    @Test
    @DisplayName("병원 이름으로 병원 조회 - 존재하지 않는 병원")
    void loadUserByUsername_notFound() {
        // given
        when(hospitalRepository.findByName(hospitalName))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.loadUserByUsername(hospitalName))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HOSPITAL_NOT_FOUND)
                .hasMessageContaining("이름 '서울대병원'에 해당하는 병원을 찾을 수 없습니다");

        verify(hospitalRepository).findByName(hospitalName);
    }

    @Test
    @DisplayName("병원 이름으로 병원 조회 - 다양한 병원명")
    void loadUserByUsername_variousHospitalNames() {
        // given
        String[] names = {"서울대병원", "연세대병원", "고려대병원"};

        for (int i = 0; i < names.length; i++) {
            Hospital h = Hospital.builder()
                    .id(i + 1)
                    .name(names[i])
                    .officialName("hospital" + (i + 1))
                    .password("password")
                    .build();
            when(hospitalRepository.findByName(names[i]))
                    .thenReturn(Optional.of(h));
        }

        // when & then
        for (String name : names) {
            UserDetails userDetails = service.loadUserByUsername(name);
            assertThat(userDetails).isNotNull();
        }

        verify(hospitalRepository, times(names.length)).findByName(anyString());
    }

    @Test
    @DisplayName("ID로 병원 조회 - 성공")
    void loadUserById_success() {
        // given
        when(hospitalRepository.findById(hospitalId))
                .thenReturn(Optional.of(hospital));

        // when
        UserDetails userDetails = service.loadUserById(hospitalId);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUserPrincipal.class);
        CustomUserPrincipal principal = (CustomUserPrincipal) userDetails;
        assertThat(principal.getId()).isEqualTo(hospitalId);
        assertThat(principal.getUsername()).isEqualTo(hospitalName); // Hospital.getName()을 username으로 사용

        verify(hospitalRepository).findById(hospitalId);
    }

    @Test
    @DisplayName("ID로 병원 조회 - 존재하지 않는 병원")
    void loadUserById_notFound() {
        // given
        when(hospitalRepository.findById(hospitalId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.loadUserById(hospitalId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.HOSPITAL_NOT_FOUND)
                .hasMessageContaining("ID '1'에 해당하는 병원을 찾을 수 없습니다");

        verify(hospitalRepository).findById(hospitalId);
    }

    @Test
    @DisplayName("ID로 병원 조회 - 다양한 ID")
    void loadUserById_variousIds() {
        // given
        Integer[] ids = {1, 2, 3, 10, 100};

        for (Integer id : ids) {
            Hospital h = Hospital.builder()
                    .id(id)
                    .name("병원" + id)
                    .officialName("hospital" + id)
                    .password("password")
                    .build();
            when(hospitalRepository.findById(id))
                    .thenReturn(Optional.of(h));
        }

        // when & then
        for (Integer id : ids) {
            UserDetails userDetails = service.loadUserById(id);
            CustomUserPrincipal principal = (CustomUserPrincipal) userDetails;
            assertThat(principal.getId()).isEqualTo(id);
        }

        verify(hospitalRepository, times(ids.length)).findById(anyInt());
    }

    @Test
    @DisplayName("Repository 호출 확인")
    void verifyRepositoryCalls() {
        // given
        when(hospitalRepository.findByName(hospitalName))
                .thenReturn(Optional.of(hospital));
        when(hospitalRepository.findById(hospitalId))
                .thenReturn(Optional.of(hospital));

        // when
        service.loadUserByUsername(hospitalName);
        service.loadUserById(hospitalId);

        // then
        verify(hospitalRepository, times(1)).findByName(hospitalName);
        verify(hospitalRepository, times(1)).findById(hospitalId);
    }

    @Test
    @DisplayName("같은 병원을 이름과 ID로 각각 조회")
    void loadSameHospitalByNameAndId() {
        // given
        when(hospitalRepository.findByName(hospitalName))
                .thenReturn(Optional.of(hospital));
        when(hospitalRepository.findById(hospitalId))
                .thenReturn(Optional.of(hospital));

        // when
        UserDetails byName = service.loadUserByUsername(hospitalName);
        UserDetails byId = service.loadUserById(hospitalId);

        // then
        assertThat(byName.getUsername()).isEqualTo(byId.getUsername());
        assertThat(((CustomUserPrincipal) byName).getId())
                .isEqualTo(((CustomUserPrincipal) byId).getId());
    }

    @Test
    @DisplayName("병원 조회 - 긴 이름")
    void loadUserByUsername_longName() {
        // given
        String longName = "서울대학교병원 응급의학센터 중증외상센터";
        Hospital longNameHospital = Hospital.builder()
                .id(1)
                .name(longName)
                .officialName("hospital_long")
                .password("password")
                .build();

        when(hospitalRepository.findByName(longName))
                .thenReturn(Optional.of(longNameHospital));

        // when
        UserDetails userDetails = service.loadUserByUsername(longName);

        // then
        assertThat(userDetails).isNotNull();
        verify(hospitalRepository).findByName(longName);
    }
}
