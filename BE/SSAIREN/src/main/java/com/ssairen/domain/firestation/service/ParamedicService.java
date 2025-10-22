package com.ssairen.domain.firestation.service;

import com.ssairen.domain.firestation.dto.ParamedicLoginRequest;
import com.ssairen.domain.firestation.dto.ParamedicLoginResponse;

/**
 * 구급대원 Service 인터페이스
 */
public interface ParamedicService {

    /**
     * 구급대원 로그인
     */
    ParamedicLoginResponse login(ParamedicLoginRequest request);
}
