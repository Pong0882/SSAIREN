package com.ssairen.global.security.service;

import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
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
 * 구급대원 UserDetailsService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ParamedicUserDetailsService implements UserDetailsService {

    private final ParamedicRepository paramedicRepository;

    /**
     * 학번으로 구급대원 조회
     *
     * @param studentNumber 학번
     * @return CustomUserPrincipal
     * @throws UsernameNotFoundException 구급대원을 찾을 수 없는 경우
     */
    @Override
    public UserDetails loadUserByUsername(String studentNumber) throws UsernameNotFoundException {
        log.debug("Loading paramedic by studentNumber: {}", studentNumber);

        Paramedic paramedic = paramedicRepository.findByStudentNumber(studentNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.PARAMEDIC_NOT_FOUND,
                        String.format("학번 '%s'에 해당하는 구급대원을 찾을 수 없습니다", studentNumber)));

        return CustomUserPrincipal.from(paramedic);
    }

    /**
     * ID로 구급대원 조회
     *
     * @param id 구급대원 ID
     * @return CustomUserPrincipal
     * @throws CustomException 구급대원을 찾을 수 없는 경우
     */
    public UserDetails loadUserById(Integer id) {
        log.debug("Loading paramedic by id: {}", id);

        Paramedic paramedic = paramedicRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.PARAMEDIC_NOT_FOUND,
                        String.format("ID '%d'에 해당하는 구급대원을 찾을 수 없습니다", id)));

        return CustomUserPrincipal.from(paramedic);
    }
}
