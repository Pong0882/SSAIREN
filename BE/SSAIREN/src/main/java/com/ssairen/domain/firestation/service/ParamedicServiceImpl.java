package com.ssairen.domain.firestation.service;

import com.ssairen.domain.firestation.dto.ParamedicInfo;
import com.ssairen.domain.firestation.dto.ParamedicLoginRequest;
import com.ssairen.domain.firestation.dto.ParamedicLoginResponse;
import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.mapper.ParamedicMapper;
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
    private final ParamedicMapper paramedicMapper;

    /**
     * 구급대원 로그인
     * JWT 없이 학번/비밀번호 검증만 수행
     */
    @Override
    public ParamedicLoginResponse login(ParamedicLoginRequest request) {
        // 학번으로 구급대원 조회
        Paramedic paramedic = paramedicRepository.findByStudentNumber(request.studentNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 검증 (평문 비교 - 추후 암호화 추가 예정)
        if (!paramedic.getPassword().equals(request.password())) {
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
