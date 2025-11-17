package com.ssairen.global.security.service;

import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import com.ssairen.global.security.config.JwtProperties;
import com.ssairen.global.security.enums.UserType;
import com.ssairen.global.security.service.RefreshTokenService.RefreshTokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService service;

    private Integer userId;
    private UserType userType;
    private String username;
    private String authorities;
    private String token;

    @BeforeEach
    void setUp() {
        userId = 1;
        userType = UserType.PARAMEDIC;
        username = "paramedic1";
        authorities = "ROLE_PARAMEDIC";
        token = "refresh-token-12345";

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(jwtProperties.getRefreshTokenExpiration()).thenReturn(604800000L); // 7 days in ms
    }

    @Test
    @DisplayName("RefreshToken 저장 - 성공")
    void save_success() {
        // given
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // when
        service.save(userId, userType, username, authorities, token);

        // then
        verify(valueOperations, times(2)).set(anyString(), anyString(), eq(604800L), eq(TimeUnit.SECONDS));
        verify(valueOperations).set(eq("refresh_token:1:PARAMEDIC"), eq(token), anyLong(), any(TimeUnit.class));
        verify(valueOperations).set(eq("token_to_user:refresh-token-12345"), eq("1:PARAMEDIC:paramedic1:ROLE_PARAMEDIC"), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("RefreshToken 저장 - userId가 null인 경우")
    void save_nullUserId() {
        // when & then
        assertThatThrownBy(() -> service.save(null, userType, username, authorities, token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId must be positive");

        verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("RefreshToken 저장 - userId가 0 이하인 경우")
    void save_invalidUserId() {
        // when & then
        assertThatThrownBy(() -> service.save(0, userType, username, authorities, token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId must be positive");

        assertThatThrownBy(() -> service.save(-1, userType, username, authorities, token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserId must be positive");
    }

    @Test
    @DisplayName("RefreshToken 저장 - userType이 null인 경우")
    void save_nullUserType() {
        // when & then
        assertThatThrownBy(() -> service.save(userId, null, username, authorities, token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("UserType must not be null");
    }

    @Test
    @DisplayName("RefreshToken 저장 - username이 비어있는 경우")
    void save_emptyUsername() {
        // when & then
        assertThatThrownBy(() -> service.save(userId, userType, "", authorities, token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username must not be empty");

        assertThatThrownBy(() -> service.save(userId, userType, null, authorities, token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username must not be empty");

        assertThatThrownBy(() -> service.save(userId, userType, "   ", authorities, token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username must not be empty");
    }

    @Test
    @DisplayName("RefreshToken 저장 - authorities가 비어있는 경우")
    void save_emptyAuthorities() {
        // when & then
        assertThatThrownBy(() -> service.save(userId, userType, username, "", token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Authorities must not be empty");

        assertThatThrownBy(() -> service.save(userId, userType, username, null, token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Authorities must not be empty");
    }

    @Test
    @DisplayName("RefreshToken 저장 - token이 비어있는 경우")
    void save_emptyToken() {
        // when & then
        assertThatThrownBy(() -> service.save(userId, userType, username, authorities, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token must not be empty");

        assertThatThrownBy(() -> service.save(userId, userType, username, authorities, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token must not be empty");
    }

    @Test
    @DisplayName("RefreshToken 조회 - 성공")
    void findByToken_success() {
        // given
        String userValue = "1:PARAMEDIC:paramedic1:ROLE_PARAMEDIC";
        when(valueOperations.get("token_to_user:refresh-token-12345")).thenReturn(userValue);

        // when
        RefreshTokenInfo info = service.findByToken(token);

        // then
        assertThat(info).isNotNull();
        assertThat(info.userId()).isEqualTo(1);
        assertThat(info.userType()).isEqualTo(UserType.PARAMEDIC);
        assertThat(info.username()).isEqualTo("paramedic1");
        assertThat(info.authorities()).isEqualTo("ROLE_PARAMEDIC");

        verify(valueOperations).get("token_to_user:refresh-token-12345");
    }

    @Test
    @DisplayName("RefreshToken 조회 - 토큰이 없는 경우 (만료)")
    void findByToken_notFound() {
        // given
        when(valueOperations.get("token_to_user:refresh-token-12345")).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> service.findByToken(token))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("RefreshToken 조회 - 잘못된 형식 (필드 개수 부족)")
    void findByToken_invalidFormat() {
        // given
        String invalidUserValue = "1:PARAMEDIC"; // 필드가 2개만 있음
        when(valueOperations.get("token_to_user:refresh-token-12345")).thenReturn(invalidUserValue);

        // when & then
        assertThatThrownBy(() -> service.findByToken(token))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("RefreshToken 조회 - 잘못된 userId 형식")
    void findByToken_invalidUserId() {
        // given
        String invalidUserValue = "invalid:PARAMEDIC:paramedic1:ROLE_PARAMEDIC";
        when(valueOperations.get("token_to_user:refresh-token-12345")).thenReturn(invalidUserValue);

        // when & then
        assertThatThrownBy(() -> service.findByToken(token))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN)
                .hasMessageContaining("Redis에 저장된 userId 형식이 올바르지 않습니다");
    }

    @Test
    @DisplayName("RefreshToken 조회 - 잘못된 userType")
    void findByToken_invalidUserType() {
        // given
        String invalidUserValue = "1:INVALID_TYPE:paramedic1:ROLE_PARAMEDIC";
        when(valueOperations.get("token_to_user:refresh-token-12345")).thenReturn(invalidUserValue);

        // when & then
        assertThatThrownBy(() -> service.findByToken(token))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN)
                .hasMessageContaining("Redis에 저장된 userType이 올바르지 않습니다");
    }

    @Test
    @DisplayName("RefreshToken 조회 - authorities에 콜론이 포함된 경우")
    void findByToken_authoritiesWithColon() {
        // given
        String userValue = "1:PARAMEDIC:paramedic1:ROLE_PARAMEDIC,ROLE_ADMIN:SPECIAL";
        when(valueOperations.get("token_to_user:refresh-token-12345")).thenReturn(userValue);

        // when
        RefreshTokenInfo info = service.findByToken(token);

        // then
        assertThat(info.authorities()).isEqualTo("ROLE_PARAMEDIC,ROLE_ADMIN:SPECIAL");
    }

    @Test
    @DisplayName("사용자 ID와 타입으로 RefreshToken 삭제 - 성공")
    void deleteByUserIdAndUserType_success() {
        // given
        when(valueOperations.get("refresh_token:1:PARAMEDIC")).thenReturn(token);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // when
        service.deleteByUserIdAndUserType(userId, userType);

        // then
        verify(valueOperations).get("refresh_token:1:PARAMEDIC");
        verify(redisTemplate).delete("refresh_token:1:PARAMEDIC");
        verify(redisTemplate).delete("token_to_user:refresh-token-12345");
    }

    @Test
    @DisplayName("사용자 ID와 타입으로 RefreshToken 삭제 - 토큰이 없는 경우")
    void deleteByUserIdAndUserType_noToken() {
        // given
        when(valueOperations.get("refresh_token:1:PARAMEDIC")).thenReturn(null);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // when
        service.deleteByUserIdAndUserType(userId, userType);

        // then
        verify(redisTemplate).delete("refresh_token:1:PARAMEDIC");
        verify(redisTemplate, times(1)).delete(anyString()); // userKey만 삭제
    }

    @Test
    @DisplayName("토큰으로 RefreshToken 삭제 - 성공")
    void deleteByToken_success() {
        // given
        String userValue = "1:PARAMEDIC:paramedic1:ROLE_PARAMEDIC";
        when(valueOperations.get("token_to_user:refresh-token-12345")).thenReturn(userValue);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // when
        service.deleteByToken(token);

        // then
        verify(valueOperations).get("token_to_user:refresh-token-12345");
        verify(redisTemplate).delete("token_to_user:refresh-token-12345");
        verify(redisTemplate).delete("refresh_token:1:PARAMEDIC");
    }

    @Test
    @DisplayName("토큰으로 RefreshToken 삭제 - 사용자 정보가 없는 경우")
    void deleteByToken_noUserValue() {
        // given
        when(valueOperations.get("token_to_user:refresh-token-12345")).thenReturn(null);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // when
        service.deleteByToken(token);

        // then
        verify(redisTemplate).delete("token_to_user:refresh-token-12345");
        verify(redisTemplate, times(1)).delete(anyString()); // tokenKey만 삭제
    }

    @Test
    @DisplayName("토큰으로 RefreshToken 삭제 - 잘못된 userValue 형식")
    void deleteByToken_invalidUserValue() {
        // given
        String invalidUserValue = "invalid"; // 필드가 부족
        when(valueOperations.get("token_to_user:refresh-token-12345")).thenReturn(invalidUserValue);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        // when
        service.deleteByToken(token);

        // then
        verify(redisTemplate).delete("token_to_user:refresh-token-12345");
        verify(redisTemplate, times(1)).delete(anyString()); // tokenKey만 삭제 (형식 오류로 userKey는 삭제 안됨)
    }

    @Test
    @DisplayName("병원 사용자 RefreshToken 저장 및 조회")
    void save_and_findByToken_hospitalUser() {
        // given
        Integer hospitalUserId = 2;
        UserType hospitalUserType = UserType.HOSPITAL;
        String hospitalUsername = "hospital1";
        String hospitalAuthorities = "ROLE_HOSPITAL";
        String hospitalToken = "hospital-refresh-token";

        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // when - save
        service.save(hospitalUserId, hospitalUserType, hospitalUsername, hospitalAuthorities, hospitalToken);

        // then - save
        verify(valueOperations).set(eq("refresh_token:2:HOSPITAL"), eq(hospitalToken), anyLong(), any(TimeUnit.class));
        verify(valueOperations).set(eq("token_to_user:hospital-refresh-token"), eq("2:HOSPITAL:hospital1:ROLE_HOSPITAL"), anyLong(), any(TimeUnit.class));

        // given - findByToken
        when(valueOperations.get("token_to_user:hospital-refresh-token"))
                .thenReturn("2:HOSPITAL:hospital1:ROLE_HOSPITAL");

        // when - findByToken
        RefreshTokenInfo info = service.findByToken(hospitalToken);

        // then - findByToken
        assertThat(info.userId()).isEqualTo(2);
        assertThat(info.userType()).isEqualTo(UserType.HOSPITAL);
        assertThat(info.username()).isEqualTo("hospital1");
        assertThat(info.authorities()).isEqualTo("ROLE_HOSPITAL");
    }
}
