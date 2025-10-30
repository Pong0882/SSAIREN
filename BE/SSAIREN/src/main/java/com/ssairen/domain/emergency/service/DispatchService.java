package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import com.ssairen.domain.emergency.dto.DispatchCreateResponse;
import com.ssairen.domain.emergency.dto.DispatchListQueryRequest;
import com.ssairen.domain.emergency.dto.DispatchListResponse;

public interface DispatchService {

    /**
     * 출동 지령 생성
     *
     * @param request 출동 지령 생성 요청 DTO
     * @return 생성된 출동 지령 응답 DTO
     */
    DispatchCreateResponse createDispatch(DispatchCreateRequest request);

    /**
     * 소방서 전체 출동 목록 조회
     *
     * @param fireStateId 소방서 ID
     * @param request     조회 조건 (커서, 페이지 크기)
     * @return 출동 목록 응답 DTO
     */
    DispatchListResponse getDispatchList(Integer fireStateId, DispatchListQueryRequest request);
}
