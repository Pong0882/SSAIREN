package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import com.ssairen.domain.emergency.dto.DispatchCreateResponse;

public interface DispatchService {

    /**
     * 출동 지령 생성
     *
     * @param request 출동 지령 생성 요청 DTO
     * @return 생성된 출동 지령 응답 DTO
     */
    DispatchCreateResponse createDispatch(DispatchCreateRequest request);
}
