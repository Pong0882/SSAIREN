package com.ssairen.global.security.converter;

import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.enums.ParamedicRank;
import com.ssairen.domain.firestation.enums.ParamedicStatus;
import com.ssairen.domain.hospital.entity.Hospital;
import com.ssairen.global.security.dto.TokenResponse;
import com.ssairen.global.security.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenResponseConverterTest {

    private TokenResponseConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TokenResponseConverter();
    }

    @Test
    @DisplayName("기본 TokenResponse 생성 - 성공")
    void toTokenResponse_success() {
        // given
        String accessToken = "access-token-123";
        String refreshToken = "refresh-token-456";
        UserType userType = UserType.PARAMEDIC;
        Integer userId = 1;
        String username = "paramedic1";

        // when
        TokenResponse response = converter.toTokenResponse(
                accessToken, refreshToken, userType, userId, username
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        assertThat(response.userType()).isEqualTo(userType);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.username()).isEqualTo(username);
    }

    @Test
    @DisplayName("기본 TokenResponse 생성 - HOSPITAL 타입")
    void toTokenResponse_hospital() {
        // given
        String accessToken = "hospital-access-token";
        String refreshToken = "hospital-refresh-token";
        UserType userType = UserType.HOSPITAL;
        Integer userId = 10;
        String username = "서울대병원";

        // when
        TokenResponse response = converter.toTokenResponse(
                accessToken, refreshToken, userType, userId, username
        );

        // then
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.userType()).isEqualTo(UserType.HOSPITAL);
        assertThat(response.userId()).isEqualTo(10);
        assertThat(response.username()).isEqualTo("서울대병원");
    }

    @Test
    @DisplayName("구급대원 상세 정보 TokenResponse 생성 - 성공")
    void toTokenResponseWithParamedic_success() {
        // given
        String accessToken = "access-token-paramedic";
        String refreshToken = "refresh-token-paramedic";

        FireState fireState = FireState.builder()
                .id(1)
                .name("서울소방서")
                .build();

        Paramedic paramedic = Paramedic.builder()
                .id(1)
                .name("홍길동")
                .studentNumber("20230001")
                .password("password")
                .rank(ParamedicRank.소방사)
                .status(ParamedicStatus.ACTIVE)
                .fireState(fireState)
                .build();

        // when
        TokenResponse response = converter.toTokenResponseWithParamedic(
                accessToken, refreshToken, paramedic
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        assertThat(response.userType()).isEqualTo(UserType.PARAMEDIC);
        assertThat(response.userId()).isEqualTo(1);
        assertThat(response.username()).isEqualTo("20230001");
    }

    @Test
    @DisplayName("구급대원 상세 정보 TokenResponse 생성 - 다양한 rank와 status")
    void toTokenResponseWithParamedic_variousRankAndStatus() {
        // given
        FireState fireState = FireState.builder()
                .id(2)
                .name("부산소방서")
                .build();

        Paramedic captain = Paramedic.builder()
                .id(2)
                .name("김대장")
                .studentNumber("20220001")
                .password("password")
                .rank(ParamedicRank.소방장)
                .status(ParamedicStatus.ON_DUTY)
                .fireState(fireState)
                .build();

        // when
        TokenResponse response = converter.toTokenResponseWithParamedic(
                "token1", "token2", captain
        );

        // then
        assertThat(response.userId()).isEqualTo(2);
        assertThat(response.username()).isEqualTo("20220001");
    }

    @Test
    @DisplayName("병원 상세 정보 TokenResponse 생성 - 성공")
    void toTokenResponseWithHospital_success() {
        // given
        String accessToken = "access-token-hospital";
        String refreshToken = "refresh-token-hospital";

        Hospital hospital = Hospital.builder()
                .id(1)
                .name("서울대병원")
                .officialName("hospital1")
                .password("password")
                .build();

        // when
        TokenResponse response = converter.toTokenResponseWithHospital(
                accessToken, refreshToken, hospital
        );

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(accessToken);
        assertThat(response.refreshToken()).isEqualTo(refreshToken);
        assertThat(response.userType()).isEqualTo(UserType.HOSPITAL);
        assertThat(response.userId()).isEqualTo(1);
        assertThat(response.username()).isEqualTo("서울대병원");
    }

    @Test
    @DisplayName("병원 상세 정보 TokenResponse 생성 - 여러 병원")
    void toTokenResponseWithHospital_multipleHospitals() {
        // given
        Hospital hospital1 = Hospital.builder()
                .id(1)
                .name("서울대병원")
                .officialName("hospital1")
                .password("password")
                .build();

        Hospital hospital2 = Hospital.builder()
                .id(2)
                .name("연세대병원")
                .officialName("hospital2")
                .password("password")
                .build();

        // when
        TokenResponse response1 = converter.toTokenResponseWithHospital(
                "token1", "refresh1", hospital1
        );
        TokenResponse response2 = converter.toTokenResponseWithHospital(
                "token2", "refresh2", hospital2
        );

        // then
        assertThat(response1.userId()).isEqualTo(1);
        assertThat(response1.username()).isEqualTo("서울대병원");
        assertThat(response2.userId()).isEqualTo(2);
        assertThat(response2.username()).isEqualTo("연세대병원");
    }

    @Test
    @DisplayName("긴 토큰 문자열 처리")
    void toTokenResponse_longTokens() {
        // given
        String longAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String longRefreshToken = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c_extra_long_part";

        // when
        TokenResponse response = converter.toTokenResponse(
                longAccessToken, longRefreshToken, UserType.PARAMEDIC, 1, "user1"
        );

        // then
        assertThat(response.accessToken()).isEqualTo(longAccessToken);
        assertThat(response.refreshToken()).isEqualTo(longRefreshToken);
    }
}
