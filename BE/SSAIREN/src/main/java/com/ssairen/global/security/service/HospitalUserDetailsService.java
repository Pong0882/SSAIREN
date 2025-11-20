package com.ssairen.global.security.service;

import com.ssairen.domain.hospital.entity.Hospital;
import com.ssairen.domain.hospital.repository.HospitalRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import com.ssairen.global.security.dto.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 병원 UserDetailsService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HospitalUserDetailsService implements UserDetailsService {

    private final HospitalRepository hospitalRepository;

    /**
     * 병원 이름으로 병원 조회
     *
     * @param name 병원 이름
     * @return CustomUserPrincipal
     * @throws UsernameNotFoundException 병원을 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        log.debug("Loading hospital by name: {}", name);

        Hospital hospital = hospitalRepository.findByName(name)
                .orElseThrow(() -> new CustomException(ErrorCode.HOSPITAL_NOT_FOUND,
                        String.format("이름 '%s'에 해당하는 병원을 찾을 수 없습니다", name)));

        return CustomUserPrincipal.from(hospital);
    }

    /**
     * ID로 병원 조회
     *
     * @param id 병원 ID
     * @return CustomUserPrincipal
     * @throws CustomException 병원을 찾을 수 없는 경우
     */
    public UserDetails loadUserById(Integer id) {
        log.debug("Loading hospital by id: {}", id);

        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.HOSPITAL_NOT_FOUND,
                        String.format("ID '%d'에 해당하는 병원을 찾을 수 없습니다", id)));

        return CustomUserPrincipal.from(hospital);
    }
}
