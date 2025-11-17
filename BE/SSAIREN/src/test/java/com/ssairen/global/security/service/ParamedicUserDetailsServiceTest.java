package com.ssairen.global.security.service;

import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
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
class ParamedicUserDetailsServiceTest {

    @Mock
    private ParamedicRepository paramedicRepository;

    @InjectMocks
    private ParamedicUserDetailsService service;

    private String studentNumber;
    private Integer paramedicId;
    private Paramedic paramedic;

    @BeforeEach
    void setUp() {
        studentNumber = "20230001";
        paramedicId = 1;
        paramedic = Paramedic.builder()
                .id(paramedicId)
                .name("홍길동")
                .studentNumber(studentNumber)
                .password("encodedPassword123")
                .build();
    }

    @Test
    @DisplayName("학번으로 구급대원 조회 - 성공")
    void loadUserByUsername_success() {
        // given
        when(paramedicRepository.findByStudentNumber(studentNumber))
                .thenReturn(Optional.of(paramedic));

        // when
        UserDetails userDetails = service.loadUserByUsername(studentNumber);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUserPrincipal.class);
        CustomUserPrincipal principal = (CustomUserPrincipal) userDetails;
        assertThat(principal.getId()).isEqualTo(paramedicId);
        assertThat(principal.getUsername()).isEqualTo(studentNumber);
        assertThat(principal.getPassword()).isEqualTo("encodedPassword123");

        verify(paramedicRepository).findByStudentNumber(studentNumber);
    }

    @Test
    @DisplayName("학번으로 구급대원 조회 - 존재하지 않는 구급대원")
    void loadUserByUsername_notFound() {
        // given
        when(paramedicRepository.findByStudentNumber(studentNumber))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.loadUserByUsername(studentNumber))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARAMEDIC_NOT_FOUND)
                .hasMessageContaining("학번 '20230001'에 해당하는 구급대원을 찾을 수 없습니다");

        verify(paramedicRepository).findByStudentNumber(studentNumber);
    }

    @Test
    @DisplayName("학번으로 구급대원 조회 - 다양한 학번")
    void loadUserByUsername_variousStudentNumbers() {
        // given
        String[] studentNumbers = {"20230001", "20230002", "20230003"};

        for (String sn : studentNumbers) {
            Paramedic p = Paramedic.builder()
                    .id(1)
                    .name("구급대원")
                    .studentNumber(sn)
                    .password("password")
                    .build();
            when(paramedicRepository.findByStudentNumber(sn))
                    .thenReturn(Optional.of(p));
        }

        // when & then
        for (String sn : studentNumbers) {
            UserDetails userDetails = service.loadUserByUsername(sn);
            assertThat(userDetails.getUsername()).isEqualTo(sn);
        }

        verify(paramedicRepository, times(studentNumbers.length)).findByStudentNumber(anyString());
    }

    @Test
    @DisplayName("ID로 구급대원 조회 - 성공")
    void loadUserById_success() {
        // given
        when(paramedicRepository.findById(paramedicId))
                .thenReturn(Optional.of(paramedic));

        // when
        UserDetails userDetails = service.loadUserById(paramedicId);

        // then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails).isInstanceOf(CustomUserPrincipal.class);
        CustomUserPrincipal principal = (CustomUserPrincipal) userDetails;
        assertThat(principal.getId()).isEqualTo(paramedicId);
        assertThat(principal.getUsername()).isEqualTo(studentNumber);

        verify(paramedicRepository).findById(paramedicId);
    }

    @Test
    @DisplayName("ID로 구급대원 조회 - 존재하지 않는 구급대원")
    void loadUserById_notFound() {
        // given
        when(paramedicRepository.findById(paramedicId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.loadUserById(paramedicId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PARAMEDIC_NOT_FOUND)
                .hasMessageContaining("ID '1'에 해당하는 구급대원을 찾을 수 없습니다");

        verify(paramedicRepository).findById(paramedicId);
    }

    @Test
    @DisplayName("ID로 구급대원 조회 - 다양한 ID")
    void loadUserById_variousIds() {
        // given
        Integer[] ids = {1, 2, 3, 10, 100};

        for (Integer id : ids) {
            Paramedic p = Paramedic.builder()
                    .id(id)
                    .name("구급대원" + id)
                    .studentNumber("202300" + id)
                    .password("password")
                    .build();
            when(paramedicRepository.findById(id))
                    .thenReturn(Optional.of(p));
        }

        // when & then
        for (Integer id : ids) {
            UserDetails userDetails = service.loadUserById(id);
            CustomUserPrincipal principal = (CustomUserPrincipal) userDetails;
            assertThat(principal.getId()).isEqualTo(id);
        }

        verify(paramedicRepository, times(ids.length)).findById(anyInt());
    }

    @Test
    @DisplayName("Repository 호출 확인")
    void verifyRepositoryCalls() {
        // given
        when(paramedicRepository.findByStudentNumber(studentNumber))
                .thenReturn(Optional.of(paramedic));
        when(paramedicRepository.findById(paramedicId))
                .thenReturn(Optional.of(paramedic));

        // when
        service.loadUserByUsername(studentNumber);
        service.loadUserById(paramedicId);

        // then
        verify(paramedicRepository, times(1)).findByStudentNumber(studentNumber);
        verify(paramedicRepository, times(1)).findById(paramedicId);
    }

    @Test
    @DisplayName("같은 구급대원을 학번과 ID로 각각 조회")
    void loadSameParamedicByUsernameAndId() {
        // given
        when(paramedicRepository.findByStudentNumber(studentNumber))
                .thenReturn(Optional.of(paramedic));
        when(paramedicRepository.findById(paramedicId))
                .thenReturn(Optional.of(paramedic));

        // when
        UserDetails byUsername = service.loadUserByUsername(studentNumber);
        UserDetails byId = service.loadUserById(paramedicId);

        // then
        assertThat(byUsername.getUsername()).isEqualTo(byId.getUsername());
        assertThat(((CustomUserPrincipal) byUsername).getId())
                .isEqualTo(((CustomUserPrincipal) byId).getId());
    }
}
