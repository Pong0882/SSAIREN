package com.ssairen.domain.firestation.service;

import com.ssairen.domain.firestation.dto.ParamedicInfo;
import com.ssairen.domain.firestation.dto.ParamedicLoginRequest;
import com.ssairen.domain.firestation.dto.ParamedicLoginResponse;
import com.ssairen.domain.firestation.dto.ParamedicRegisterRequest;
import com.ssairen.domain.firestation.dto.ParamedicRegisterResponse;
import java.util.List;

/**
 * 구급대원 Service 인터페이스
 */
public interface ParamedicService {

    /**
     * 구급대원 회원가입
     */
    ParamedicRegisterResponse register(ParamedicRegisterRequest request);

    /**
     * 구급대원 로그인
     */
    ParamedicLoginResponse login(ParamedicLoginRequest request);

    /**
     * 전체 구급대원 조회
     */
    List<ParamedicInfo> getAllParamedics();
}
