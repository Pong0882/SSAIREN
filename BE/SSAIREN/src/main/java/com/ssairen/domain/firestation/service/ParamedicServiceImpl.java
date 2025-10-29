package com.ssairen.domain.firestation.service;

import com.ssairen.domain.firestation.dto.ParamedicInfo;
import com.ssairen.domain.firestation.dto.ParamedicLoginRequest;
import com.ssairen.domain.firestation.dto.ParamedicLoginResponse;
import com.ssairen.domain.firestation.dto.ParamedicRegisterRequest;
import com.ssairen.domain.firestation.dto.ParamedicRegisterResponse;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.enums.ParamedicRank;
import com.ssairen.domain.firestation.enums.ParamedicStatus;
import com.ssairen.domain.firestation.mapper.ParamedicMapper;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구급대원 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParamedicServiceImpl implements ParamedicService {

    private static final String LOG_PREFIX = "[ParamedicService] ";

    private final ParamedicRepository paramedicRepository;
    private final FireStateRepository fireStateRepository;
    private final ParamedicMapper paramedicMapper;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * 구급대원 회원가입
     */
    @Override
    @Transactional
    public ParamedicRegisterResponse register(ParamedicRegisterRequest request) {
        // 1. 학번 중복 체크
        if (paramedicRepository.existsByStudentNumber(request.studentNumber())) {
            throw new CustomException(ErrorCode.PARAMEDIC_ALREADY_EXISTS);
        }

        // 2. 소방서 조회
        FireState fireState = fireStateRepository.findById(request.fireStateId())
                .orElseThrow(() -> new CustomException(ErrorCode.FIRE_STATION_NOT_FOUND));

        // 3. 계급 enum 변환
        ParamedicRank rank;
        try {
            rank = ParamedicRank.valueOf(request.rank());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE,
                    "유효하지 않은 계급입니다: " + request.rank());
        }

        // 4. Paramedic 엔티티 생성 및 저장
        Paramedic paramedic = Paramedic.builder()
                .studentNumber(request.studentNumber())
                .password(passwordEncoder.encode(request.password()))  // BCrypt 암호화
                .name(request.name())
                .rank(rank)
                .status(ParamedicStatus.ACTIVE)  // 기본값: ACTIVE
                .fireState(fireState)
                .build();

        Paramedic savedParamedic = paramedicRepository.save(paramedic);

        log.info(LOG_PREFIX + "구급대원 회원가입 완료 - 학번: {}, 이름: {}",
                savedParamedic.getStudentNumber(), savedParamedic.getName());

        // 5. 응답 DTO 생성
        return ParamedicRegisterResponse.from(savedParamedic);
    }

    /**
     * 구급대원 로그인
     * JWT 없이 학번/비밀번호 검증만 수행
     */
    @Override
    public ParamedicLoginResponse login(ParamedicLoginRequest request) {
        // 학번으로 구급대원 조회
        Paramedic paramedic = paramedicRepository.findByStudentNumber(request.studentNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 검증 (BCrypt 비교)
        if (!passwordEncoder.matches(request.password(), paramedic.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 응답 DTO 생성
        return ParamedicLoginResponse.from(paramedic);
    }

    /**
     * 전체 구급대원 조회
     *
     * @return 전체 구급대원 목록
     */
    @Override
    public List<ParamedicInfo> getAllParamedics() {
        List<Paramedic> paramedics = paramedicRepository.findAll();

        log.info(LOG_PREFIX + "전체 구급대원 조회 완료 - 조회된 구급대원 수: {}명", paramedics.size());

        return paramedicMapper.toParamedicInfoList(paramedics);
    }
}
