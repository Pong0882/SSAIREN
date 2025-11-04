package com.ssairen.domain.emergency.validation;

import com.ssairen.domain.firestation.entity.FireState;
import com.ssairen.domain.firestation.repository.FireStateRepository;
import com.ssairen.global.exception.CustomException;
import com.ssairen.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 구급일지 관련 입력 값 검증
 */
@Component
@RequiredArgsConstructor
public class EmergencyReportValidator {

    private final FireStateRepository fireStateRepository;

    public FireState validateFireStateExists(Integer fireStateId) {
        return fireStateRepository.findById(fireStateId)
                .orElseThrow(() -> new CustomException(ErrorCode.FIRE_STATE_NOT_FOUND));
    }
}
