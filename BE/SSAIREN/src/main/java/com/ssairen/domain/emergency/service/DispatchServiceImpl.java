package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.*;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.mapper.DispatchMapper;
import com.ssairen.domain.emergency.repository.DispatchRepository;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import com.ssairen.global.utils.CursorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchServiceImpl implements DispatchService {

    private static final String LOG_PREFIX = "[DispatchService] ";

    private final DispatchRepository dispatchRepository;
    private final FireStateRepository fireStateRepository;
    private final DispatchMapper dispatchMapper;

    /**
     * 출동 지령 생성
     *
     * @param request 출동 지령 생성 요청 DTO
     * @return 생성된 출동 지령 응답 DTO
     */
    @Override
    @Transactional
    public DispatchCreateResponse createDispatch(DispatchCreateRequest request) {
        // 소방서 존재 여부 확인
        FireState fireState = fireStateRepository.findById(request.fireStateId())
                .orElseThrow(() -> new CustomException(ErrorCode.FIRE_STATE_NOT_FOUND));

        Dispatch dispatch = dispatchMapper.toEntity(request, fireState);

        Dispatch savedDispatch = dispatchRepository.save(dispatch);

        log.info(LOG_PREFIX + "출동 지령 생성 완료 - ID: {}, 소방서: {}, 재난분류: {}, 주소: {}",
                savedDispatch.getId(), fireState.getName(), savedDispatch.getDisasterType(), savedDispatch.getLocationAddress());

        return dispatchMapper.toResponse(savedDispatch);
    }

    /**
     * 소방서 전체 출동 목록 조회
     *
     * @param fireStateId 소방서 ID
     * @param request     조회 조건 (커서, 페이지 크기)
     * @return 출동 목록 응답 DTO
     */
    @Override
    public DispatchListResponse getDispatchList(Integer fireStateId, DispatchListQueryRequest request) {
        // 소방서 존재 여부 확인
        FireState fireState = fireStateRepository.findById(fireStateId)
                .orElseThrow(() -> new CustomException(ErrorCode.FIRE_STATE_NOT_FOUND));

        // 커서 디코딩
        Long cursorId = CursorUtils.decodeCursor(request.cursor());

        // 데이터 조회 (limit + 1개 조회하여 다음 페이지 존재 여부 확인)
        List<Dispatch> dispatches = dispatchRepository.findByFireStateIdWithFilters(
                fireStateId,
                cursorId,
                request.limit() + 1
        );

        // 다음 페이지 존재 여부 확인 및 실제 반환할 데이터 추출
        boolean hasMore = dispatches.size() > request.limit();
        List<Dispatch> actualDispatches = hasMore
                ? dispatches.subList(0, request.limit())
                : dispatches;

        // 다음 커서 생성
        String nextCursor = null;
        if (hasMore && !actualDispatches.isEmpty()) {
            Long lastId = actualDispatches.get(actualDispatches.size() - 1).getId();
            nextCursor = CursorUtils.encodeCursor(lastId);
        }

        log.info(LOG_PREFIX + "출동 목록 조회 완료 - 소방서: {}, 조회 건수: {}, 다음 페이지 존재: {}",
                fireState.getName(), actualDispatches.size(), hasMore);

        return new DispatchListResponse(
                dispatchMapper.toFireStateResponse(fireState),
                dispatchMapper.toResponseList(actualDispatches),
                new PaginationResponse(nextCursor, hasMore)
        );
    }
}
