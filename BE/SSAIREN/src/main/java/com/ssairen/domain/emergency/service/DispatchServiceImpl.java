package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.*;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.mapper.DispatchMapper;
import com.ssairen.domain.emergency.repository.DispatchRepository;
import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.entity.Paramedic;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.domain.firestation.repository.ParamedicRepository;
import com.ssairen.domain.firestation.service.FcmService;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import com.ssairen.global.utils.CursorUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchServiceImpl implements DispatchService {

    private static final String LOG_PREFIX = "[DispatchService] ";

    private final DispatchRepository dispatchRepository;
    private final FireStateRepository fireStateRepository;
    private final ParamedicRepository paramedicRepository;
    private final DispatchMapper dispatchMapper;
    private final FcmService fcmService;

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

        // 해당 소방서 소속 구급대원들에게 FCM 알림 전송
        sendDispatchNotificationToParamedics(fireState, savedDispatch);

        return dispatchMapper.toResponse(savedDispatch);
    }

    /**
     * 출동 지령이 생성되면 해당 소방서 소속 구급대원 전체에게 푸시 알림 전송
     *
     * @param fireState 소방서
     * @param dispatch  출동 지령
     */
    private void sendDispatchNotificationToParamedics(FireState fireState, Dispatch dispatch) {
        try {
            // 해당 소방서 소속 구급대원 전체 조회
            List<Paramedic> paramedics = paramedicRepository.findAll().stream()
                    .filter(p -> p.getFireState().getId().equals(fireState.getId()))
                    .toList();

            log.info(LOG_PREFIX + "FCM 알림 대상 구급대원 수: {} (소방서: {})", paramedics.size(), fireState.getName());

            // 각 구급대원에게 알림 전송
            for (Paramedic paramedic : paramedics) {
                Map<String, String> data = new HashMap<>();
                data.put("type", "DISPATCH");
                data.put("dispatchId", dispatch.getId().toString());
                data.put("disasterType", dispatch.getDisasterType());
                data.put("locationAddress", dispatch.getLocationAddress());

                fcmService.sendNotification(
                        paramedic.getId(),
                        "🚨 출동 지령",
                        String.format("[%s] %s - %s",
                                dispatch.getDisasterType(),
                                dispatch.getLocationAddress(),
                                dispatch.getIncidentDescription() != null ? dispatch.getIncidentDescription() : ""),
                        data
                );
            }

            log.info(LOG_PREFIX + "FCM 알림 전송 완료 - 출동 ID: {}", dispatch.getId());

        } catch (Exception e) {
            // FCM 전송 실패가 출동 지령 생성을 방해하지 않도록 예외를 로그만 남김
            log.error(LOG_PREFIX + "FCM 알림 전송 실패 - 출동 ID: {}, 에러: {}", dispatch.getId(), e.getMessage(), e);
        }
    }

    /**
     * 소방서 전체 출동 목록 조회
     *
     * @param paramedicId 구급대원 ID
     * @param request     조회 조건 (커서, 페이지 크기)
     * @return 출동 목록 응답 DTO
     */
    @Override
    public DispatchListResponse getDispatchList(Integer paramedicId, DispatchListQueryRequest request) {
        // 구급대원 조회
        Paramedic paramedic = paramedicRepository.findById(paramedicId)
                .orElseThrow(() -> new CustomException(ErrorCode.PARAMEDIC_NOT_FOUND));

        // 구급대원이 소속된 소방서 조회
        FireState fireState = fireStateRepository.findById(paramedic.getFireState().getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FIRE_STATE_NOT_FOUND));

        // 커서 디코딩
        Long cursorId = CursorUtils.decodeCursor(request.cursor());

        // 데이터 조회 (limit + 1개 조회하여 다음 페이지 존재 여부 확인)
        List<Dispatch> dispatches = dispatchRepository.findByFireStateIdWithFilters(
                fireState.getId(),
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
