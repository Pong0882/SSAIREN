package com.ssairen.domain.emergency.validation;

import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 출동 지령 생성 요청 검증 클래스
 * DTO의 @Valid로 처리되지 않는 추가 검증 수행
 */
@Component
@RequiredArgsConstructor
public class DispatchValidator {

    private final FireStateRepository fireStateRepository;

    public void validateFireStateExists(Integer fireStateId) {
        if (!fireStateRepository.existsById(fireStateId)) {
            throw new CustomException(ErrorCode.FIRE_STATE_NOT_FOUND);
        }
    }
}
