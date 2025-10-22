package com.ssairen.common.validator;

/**
 * Validation 그룹 인터페이스
 * @Valid 사용 시 groups 속성으로 특정 시점의 유효성 검증 구분
 */
public class ValidationGroups {

    /**
     * 생성 시 유효성 검증
     */
    public interface Create {}

    /**
     * 수정 시 유효성 검증
     */
    public interface Update {}

    /**
     * 삭제 시 유효성 검증
     */
    public interface Delete {}
}
