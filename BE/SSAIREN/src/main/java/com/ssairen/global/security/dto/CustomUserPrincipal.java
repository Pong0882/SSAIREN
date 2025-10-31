package com.ssairen.global.security.dto;

import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.hospital.entity.Hospital;
import com.ssairen.global.security.enums.UserType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security UserDetails 구현체
 * Paramedic과 Hospital을 공통으로 표현
 */
@Getter
@RequiredArgsConstructor
public class CustomUserPrincipal implements UserDetails {

    private final Integer id;
    private final String username;
    private final String password;
    private final UserType userType;
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Paramedic → CustomUserPrincipal 변환
     */
    public static CustomUserPrincipal from(Paramedic paramedic) {
        return new CustomUserPrincipal(
                paramedic.getId(),
                paramedic.getStudentNumber(),
                paramedic.getPassword(),
                UserType.PARAMEDIC,
                List.of(new SimpleGrantedAuthority("ROLE_PARAMEDIC"))
        );
    }

    /**
     * Hospital → CustomUserPrincipal 변환
     */
    public static CustomUserPrincipal from(Hospital hospital) {
        return new CustomUserPrincipal(
                hospital.getId(),
                hospital.getName(),  // username으로 병원명 사용 (로그인 시 입력값과 일치)
                hospital.getPassword(),
                UserType.HOSPITAL,
                List.of(new SimpleGrantedAuthority("ROLE_HOSPITAL"))
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
