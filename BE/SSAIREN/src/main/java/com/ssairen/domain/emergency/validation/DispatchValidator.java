package com.ssairen.domain.emergency.validation;

import com.ssairen.domain.emergency.dto.DispatchCreateRequest;
import org.springframework.stereotype.Component;

/**
 * 출동 지령 생성 요청 검증 클래스
 * DTO의 @Valid로 처리되지 않는 추가 검증 수행
 */
@Component
public class DispatchValidator {

    public void validateCreateRequest(DispatchCreateRequest request) {
        // 현재는 @Valid 어노테이션으로 대부분의 검증이 가능하므로
        // 추가 검증이 필요한 경우에만 여기에 로직 추가
    }
}
