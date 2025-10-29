package com.ssairen.domain.emergency.service;

import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import com.ssairen.domain.emergency.dto.DispatchCreateResponse;
import com.ssairen.domain.emergency.entity.Dispatch;
import com.ssairen.domain.emergency.mapper.DispatchMapper;
import com.ssairen.domain.emergency.repository.DispatchRepository;
import com.ssairen.domain.emergency.validation.DispatchValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchServiceImpl implements DispatchService {

    private static final String LOG_PREFIX = "[DispatchService] ";

    private final DispatchRepository dispatchRepository;
    private final DispatchMapper dispatchMapper;
    private final DispatchValidator dispatchValidator;

    /**
     * 출동 지령 생성
     *
     * @param request 출동 지령 생성 요청 DTO
     * @return 생성된 출동 지령 응답 DTO
     */
    @Override
    @Transactional
    public DispatchCreateResponse createDispatch(DispatchCreateRequest request) {
        // 1. 추가 검증 수행
//        dispatchValidator.validateCreateRequest(request);

        // 2. DTO -> Entity 변환
        Dispatch dispatch = dispatchMapper.toEntity(request);

        // 3. 엔티티 저장
        Dispatch savedDispatch = dispatchRepository.save(dispatch);

        log.info(LOG_PREFIX + "출동 지령 생성 완료 - ID: {}, 재난분류: {}, 주소: {}",
                savedDispatch.getId(), savedDispatch.getDisasterType(), savedDispatch.getLocationAddress());

        // 4. Entity -> 응답 DTO 변환 및 반환
        return dispatchMapper.toResponse(savedDispatch);
    }
}
