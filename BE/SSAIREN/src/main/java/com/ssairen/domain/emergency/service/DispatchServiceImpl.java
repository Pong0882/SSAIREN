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
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

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

        // 구급대원 존재 여부 확인
        Paramedic paramedic = paramedicRepository.findById(request.paramedicId())
                .orElseThrow(() -> new CustomException(ErrorCode.PARAMEDIC_NOT_FOUND));

        Dispatch dispatch = dispatchMapper.toEntity(request, fireState, paramedic);

        Dispatch savedDispatch = dispatchRepository.save(dispatch);

        log.info(LOG_PREFIX + "출동 지령 생성 완료 - ID: {}, 소방서: {}, 재난분류: {}, 주소: {}",
                savedDispatch.getId(), fireState.getName(), savedDispatch.getDisasterType(), savedDispatch.getLocationAddress());

        // 해당 구급대원에게 FCM 푸시 알림 전송 (모든 출동지령 정보 포함)
        sendDispatchNotificationToParamedic(paramedic.getId(), savedDispatch, request);

        // 해당 구급대원에게 WebSocket 실시간 알림 전송 (모든 출동지령 정보 포함)
        sendWebSocketNotification(paramedic.getId(), request);

        return dispatchMapper.toResponse(savedDispatch);
    }

    /**
     * 출동 지령이 생성되면 지정한 구급대원 1명에게만 푸시 알림 전송
     *
     * @param paramedicId 대상 구급대원 ID
     * @param dispatch    출동 지령
     * @param request     출동 지령 생성 요청 데이터
     */
    private void sendDispatchNotificationToParamedic(Integer paramedicId, Dispatch dispatch, DispatchCreateRequest request) {
        try {
            Paramedic paramedic = paramedicRepository.findById(paramedicId)
                    .orElseThrow(() -> new CustomException(ErrorCode.PARAMEDIC_NOT_FOUND));

            // (선택) 출동의 소방서와 구급대원의 소방서가 다르면 보내지 않음
            if (dispatch.getFireState() != null
                    && paramedic.getFireState() != null
                    && !paramedic.getFireState().getId().equals(dispatch.getFireState().getId())) {
                log.warn("[DISPATCH] FCM 대상 제외 - 서로 다른 소방서. dispatchId={}, paramedicId={}, dispatch.fireState={}, paramedic.fireState={}",
                        dispatch.getId(), paramedicId,
                        dispatch.getFireState().getId(), paramedic.getFireState().getId());
                return;
            }

            // FCM data에 출동지령의 모든 정보 포함
            Map<String, String> data = new HashMap<>();
            data.put("type", "DISPATCH");
            data.put("dispatchId", String.valueOf(dispatch.getId()));
            data.put("fireStateId", String.valueOf(request.fireStateId()));
            data.put("paramedicId", String.valueOf(request.paramedicId()));

            if (request.disasterNumber() != null) {
                data.put("disasterNumber", request.disasterNumber());
            }
            data.put("disasterType", request.disasterType());
            if (request.disasterSubtype() != null) {
                data.put("disasterSubtype", request.disasterSubtype());
            }
            if (request.reporterName() != null) {
                data.put("reporterName", request.reporterName());
            }
            if (request.reporterPhone() != null) {
                data.put("reporterPhone", request.reporterPhone());
            }
            data.put("locationAddress", request.locationAddress());
            if (request.incidentDescription() != null) {
                data.put("incidentDescription", request.incidentDescription());
            }
            if (request.dispatchLevel() != null) {
                data.put("dispatchLevel", request.dispatchLevel());
            }
            if (request.dispatchOrder() != null) {
                data.put("dispatchOrder", String.valueOf(request.dispatchOrder()));
            }
            if (request.dispatchStation() != null) {
                data.put("dispatchStation", request.dispatchStation());
            }
            if (request.date() != null) {
                data.put("date", request.date().toString());
            }

            fcmService.sendNotification(
                    paramedic.getId(),
                    "🚨 출동 지령",
                    String.format("[%s] %s - %s",
                            request.disasterType(),
                            request.locationAddress(),
                            request.incidentDescription() != null ? request.incidentDescription() : ""),
                    data
            );

            log.info("[DISPATCH] FCM 전송 완료 - dispatchId={}, paramedicId={}",
                    dispatch.getId(), paramedicId);

        } catch (Exception e) {
            // FCM 전송 실패가 출동 생성 로직을 막지 않도록 예외는 로깅만
            log.error("[DISPATCH] FCM 전송 실패 - dispatchId={}, paramedicId={}, error={}",
                    dispatch.getId(), paramedicId, e.getMessage(), e);
        }
    }

    /**
     * WebSocket으로 출동지령 정보를 구급대원에게 실시간 전송
     *
     * @param paramedicId 구급대원 ID
     * @param request 출동지령 생성 요청 데이터
     */
    private void sendWebSocketNotification(Integer paramedicId, DispatchCreateRequest request) {
        try {
            String destination = "/topic/paramedic." + paramedicId;

            messagingTemplate.convertAndSend(destination, request);

            log.info("[DISPATCH] WebSocket 전송 완료 - destination={}, paramedicId={}",
                    destination, paramedicId);

        } catch (Exception e) {
            // WebSocket 전송 실패가 출동 생성 로직을 막지 않도록 예외는 로깅만
            log.error("[DISPATCH] WebSocket 전송 실패 - paramedicId={}, error={}",
                    paramedicId, e.getMessage(), e);
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
