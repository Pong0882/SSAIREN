package com.ssairen.global.security.service;

import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import com.ssairen.domain.firestation.service.FcmService;
import com.ssairen.domain.hospital.entity.Hospital;
import com.ssairen.domain.hospital.repository.HospitalRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import com.ssairen.global.security.converter.TokenResponseConverter;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import com.ssairen.global.security.dto.LoginRequest;
import com.ssairen.global.security.dto.TokenResponse;
import com.ssairen.global.security.enums.UserType;
import com.ssairen.global.security.jwt.JwtTokenProvider;
import com.ssairen.global.security.service.RefreshTokenService.RefreshTokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ParamedicUserDetailsService paramedicUserDetailsService;

    @Mock
    private HospitalUserDetailsService hospitalUserDetailsService;

    @Mock
    private ParamedicRepository paramedicRepository;

    @Mock
    private HospitalRepository hospitalRepository;

    @Mock
    private TokenResponseConverter tokenResponseConverter;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private FcmService fcmService;

    @InjectMocks
    private AuthService service;

    private LoginRequest paramedicLoginRequest;
    private LoginRequest hospitalLoginRequest;
    private CustomUserPrincipal paramedicPrincipal;
    private CustomUserPrincipal hospitalPrincipal;
    private Paramedic paramedic;
    private Hospital hospital;

    @BeforeEach
    void setUp() {
        // 구급대원 로그인 요청
        paramedicLoginRequest = new LoginRequest(
                UserType.PARAMEDIC,
                "paramedic1",
                "password123",
                "fcm-token-123"
        );

        // 병원 로그인 요청
        hospitalLoginRequest = new LoginRequest(
                UserType.HOSPITAL,
                "hospital1",
                "password123",
                null
        );

        // 구급대원 Principal
        paramedicPrincipal = new CustomUserPrincipal(
                1,
                "paramedic1",
                "encodedPassword",
                UserType.PARAMEDIC,
                Arrays.asList(new SimpleGrantedAuthority("ROLE_PARAMEDIC"))
        );

        // 병원 Principal
        hospitalPrincipal = new CustomUserPrincipal(
                1,
                "hospital1",
                "encodedPassword",
                UserType.HOSPITAL,
                Arrays.asList(new SimpleGrantedAuthority("ROLE_HOSPITAL"))
        );

        // 구급대원 엔티티
        paramedic = Paramedic.builder()
                .id(1)
                .name("홍길동")
                .studentNumber("paramedic1")
                .password("encodedPassword")
                .build();

        // 병원 엔티티
        hospital = Hospital.builder()
                .id(1)
                .name("서울대병원")
                .officialName("hospital1")
                .password("encodedPassword")
                .build();
    }

    @Test
    @DisplayName("구급대원 로그인 - 성공")
    void login_paramedic_success() {
        // given
        when(paramedicUserDetailsService.loadUserByUsername("paramedic1"))
                .thenReturn(paramedicPrincipal);
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyInt(), anyString(), any(), any()))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyInt(), any()))
                .thenReturn("refresh-token");
        when(paramedicRepository.findById(1))
                .thenReturn(Optional.of(paramedic));

        TokenResponse expectedResponse = new TokenResponse(
                "access-token",
                "refresh-token",
                UserType.PARAMEDIC,
                1,
                "paramedic1"
        );
        when(tokenResponseConverter.toTokenResponseWithParamedic(anyString(), anyString(), any()))
                .thenReturn(expectedResponse);

        // when
        TokenResponse response = service.login(paramedicLoginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.userType()).isEqualTo(UserType.PARAMEDIC);

        verify(paramedicUserDetailsService).loadUserByUsername("paramedic1");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(fcmService).registerToken(1, "fcm-token-123");
        verify(refreshTokenService).save(eq(1), eq(UserType.PARAMEDIC), eq("paramedic1"), anyString(), eq("refresh-token"));
    }

    @Test
    @DisplayName("병원 로그인 - 성공")
    void login_hospital_success() {
        // given
        when(hospitalUserDetailsService.loadUserByUsername("hospital1"))
                .thenReturn(hospitalPrincipal);
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyInt(), anyString(), any(), any()))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyInt(), any()))
                .thenReturn("refresh-token");
        when(hospitalRepository.findById(1))
                .thenReturn(Optional.of(hospital));

        TokenResponse expectedResponse = new TokenResponse(
                "access-token",
                "refresh-token",
                UserType.HOSPITAL,
                1,
                "hospital1"
        );
        when(tokenResponseConverter.toTokenResponseWithHospital(anyString(), anyString(), any()))
                .thenReturn(expectedResponse);

        // when
        TokenResponse response = service.login(hospitalLoginRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.userType()).isEqualTo(UserType.HOSPITAL);

        verify(hospitalUserDetailsService).loadUserByUsername("hospital1");
        verify(fcmService, never()).registerToken(anyInt(), anyString());
    }

    @Test
    @DisplayName("로그인 - 잘못된 비밀번호")
    void login_invalidPassword() {
        // given
        when(paramedicUserDetailsService.loadUserByUsername("paramedic1"))
                .thenReturn(paramedicPrincipal);
        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        LoginRequest wrongRequest = new LoginRequest(
                UserType.PARAMEDIC,
                "paramedic1",
                "wrongPassword",
                null
        );

        // when & then
        assertThatThrownBy(() -> service.login(wrongRequest))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);

        verify(paramedicUserDetailsService).loadUserByUsername("paramedic1");
        verify(jwtTokenProvider, never()).generateAccessToken(anyInt(), anyString(), any(), any());
    }

    @Test
    @DisplayName("로그인 - FCM 토큰 없이 구급대원 로그인")
    void login_paramedic_withoutFcmToken() {
        // given
        LoginRequest requestWithoutFcm = new LoginRequest(
                UserType.PARAMEDIC,
                "paramedic1",
                "password123",
                null
        );

        when(paramedicUserDetailsService.loadUserByUsername("paramedic1"))
                .thenReturn(paramedicPrincipal);
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyInt(), anyString(), any(), any()))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyInt(), any()))
                .thenReturn("refresh-token");
        when(paramedicRepository.findById(1))
                .thenReturn(Optional.of(paramedic));
        when(tokenResponseConverter.toTokenResponseWithParamedic(anyString(), anyString(), any()))
                .thenReturn(new TokenResponse("access-token", "refresh-token", UserType.PARAMEDIC, 1, "paramedic1"));

        // when
        service.login(requestWithoutFcm);

        // then
        verify(fcmService, never()).registerToken(anyInt(), anyString());
    }

    @Test
    @DisplayName("토큰 갱신 - 구급대원")
    void refresh_paramedic_success() {
        // given
        String refreshToken = "refresh-token";
        RefreshTokenInfo info = new RefreshTokenInfo(
                1,
                UserType.PARAMEDIC,
                "paramedic1",
                "ROLE_PARAMEDIC"
        );

        when(refreshTokenService.findByToken(refreshToken))
                .thenReturn(info);
        when(jwtTokenProvider.generateAccessToken(anyInt(), anyString(), any(), any()))
                .thenReturn("new-access-token");
        when(paramedicRepository.findById(1))
                .thenReturn(Optional.of(paramedic));

        TokenResponse expectedResponse = new TokenResponse(
                "new-access-token",
                refreshToken,
                UserType.PARAMEDIC,
                1,
                "paramedic1"
        );
        when(tokenResponseConverter.toTokenResponseWithParamedic(anyString(), anyString(), any()))
                .thenReturn(expectedResponse);

        // when
        TokenResponse response = service.refresh(refreshToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo(refreshToken);

        verify(refreshTokenService).findByToken(refreshToken);
        verify(jwtTokenProvider).generateAccessToken(eq(1), eq("paramedic1"), eq(UserType.PARAMEDIC), any());
    }

    @Test
    @DisplayName("토큰 갱신 - 병원")
    void refresh_hospital_success() {
        // given
        String refreshToken = "refresh-token";
        RefreshTokenInfo info = new RefreshTokenInfo(
                1,
                UserType.HOSPITAL,
                "hospital1",
                "ROLE_HOSPITAL"
        );

        when(refreshTokenService.findByToken(refreshToken))
                .thenReturn(info);
        when(jwtTokenProvider.generateAccessToken(anyInt(), anyString(), any(), any()))
                .thenReturn("new-access-token");
        when(hospitalRepository.findById(1))
                .thenReturn(Optional.of(hospital));

        TokenResponse expectedResponse = new TokenResponse(
                "new-access-token",
                refreshToken,
                UserType.HOSPITAL,
                1,
                "hospital1"
        );
        when(tokenResponseConverter.toTokenResponseWithHospital(anyString(), anyString(), any()))
                .thenReturn(expectedResponse);

        // when
        TokenResponse response = service.refresh(refreshToken);

        // then
        assertThat(response).isNotNull();
        assertThat(response.userType()).isEqualTo(UserType.HOSPITAL);

        verify(hospitalRepository).findById(1);
    }

    @Test
    @DisplayName("로그아웃 - 구급대원")
    void logout_paramedic_success() {
        // given
        doNothing().when(refreshTokenService).deleteByUserIdAndUserType(1, UserType.PARAMEDIC);
        doNothing().when(fcmService).deleteAllParamedicTokens(1);

        // when
        service.logout(paramedicPrincipal);

        // then
        verify(refreshTokenService).deleteByUserIdAndUserType(1, UserType.PARAMEDIC);
        verify(fcmService).deleteAllParamedicTokens(1);
    }

    @Test
    @DisplayName("로그아웃 - 병원")
    void logout_hospital_success() {
        // given
        doNothing().when(refreshTokenService).deleteByUserIdAndUserType(1, UserType.HOSPITAL);

        // when
        service.logout(hospitalPrincipal);

        // then
        verify(refreshTokenService).deleteByUserIdAndUserType(1, UserType.HOSPITAL);
        verify(fcmService, never()).deleteAllParamedicTokens(anyInt());
    }

    @Test
    @DisplayName("로그아웃 - FCM 토큰 삭제 실패해도 로그아웃 성공")
    void logout_fcmTokenDeletionFails() {
        // given
        doNothing().when(refreshTokenService).deleteByUserIdAndUserType(1, UserType.PARAMEDIC);
        doThrow(new RuntimeException("FCM error")).when(fcmService).deleteAllParamedicTokens(1);

        // when
        service.logout(paramedicPrincipal);

        // then
        verify(refreshTokenService).deleteByUserIdAndUserType(1, UserType.PARAMEDIC);
        verify(fcmService).deleteAllParamedicTokens(1);
        // 예외가 발생해도 로그아웃은 정상 완료
    }

    @Test
    @DisplayName("로그인 - FCM 토큰 등록 실패해도 로그인 성공")
    void login_fcmRegistrationFails() {
        // given
        when(paramedicUserDetailsService.loadUserByUsername("paramedic1"))
                .thenReturn(paramedicPrincipal);
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyInt(), anyString(), any(), any()))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken(anyInt(), any()))
                .thenReturn("refresh-token");
        when(paramedicRepository.findById(1))
                .thenReturn(Optional.of(paramedic));
        when(tokenResponseConverter.toTokenResponseWithParamedic(anyString(), anyString(), any()))
                .thenReturn(new TokenResponse("access-token", "refresh-token", UserType.PARAMEDIC, 1, "paramedic1"));

        doThrow(new RuntimeException("FCM error")).when(fcmService).registerToken(1, "fcm-token-123");

        // when
        TokenResponse response = service.login(paramedicLoginRequest);

        // then
        assertThat(response).isNotNull();
        verify(fcmService).registerToken(1, "fcm-token-123");
        // 예외가 발생해도 로그인은 정상 완료
    }
}
