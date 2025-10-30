package com.ssairen.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전역 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum

ErrorCode {

    // ============================================
    // Common Errors (1000번대)
    // ============================================
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "입력 정보가 올바르지 않습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 입력값입니다."),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "INVALID_TYPE_VALUE", "잘못된 타입입니다."),
    MISSING_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST, "MISSING_REQUEST_PARAMETER", "필수 파라미터가 누락되었습니다."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_JSON_FORMAT", "JSON 형식이 올바르지 않습니다."),
    INVALID_CURSOR(HttpStatus.BAD_REQUEST, "INVALID_CURSOR", "유효하지 않은 커서 형식입니다."),

    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "지원하지 않는 HTTP 메소드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "서비스를 일시적으로 사용할 수 없습니다."),

    // ============================================
    // Authentication & Authorization (2000번대)
    // ============================================
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "학번 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_NOT_FOUND", "RefreshToken을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED", "RefreshToken이 만료되었습니다."),
    TOKEN_REISSUE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN_REISSUE_FAILED", "토큰 재발급에 실패했습니다."),

    // ============================================
    // Paramedic (3000번대)
    // ============================================
    PARAMEDIC_NOT_FOUND(HttpStatus.NOT_FOUND, "PARAMEDIC_NOT_FOUND", "구급대원을 찾을 수 없습니다."),
    PARAMEDIC_ALREADY_EXISTS(HttpStatus.CONFLICT, "PARAMEDIC_ALREADY_EXISTS", "이미 존재하는 학번입니다."),
    PARAMEDIC_INACTIVE(HttpStatus.BAD_REQUEST, "PARAMEDIC_INACTIVE", "비활성화된 구급대원입니다."),
    INVALID_STUDENT_NUMBER(HttpStatus.BAD_REQUEST, "INVALID_STUDENT_NUMBER", "학번 형식이 올바르지 않습니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD_FORMAT", "비밀번호는 8자 이상이어야 합니다."),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "WEAK_PASSWORD", "비밀번호가 너무 약합니다. 영문, 숫자, 특수문자를 조합해주세요."),

    // ============================================
    // FireStation (4000번대)
    // ============================================
    FIRE_STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "FIRE_STATION_NOT_FOUND", "소방서를 찾을 수 없습니다."),
    FIRE_STATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "FIRE_STATION_ALREADY_EXISTS", "이미 존재하는 소방서입니다."),
    FIRE_STATE_NOT_FOUND(HttpStatus.NOT_FOUND, "FIRE_STATE_NOT_FOUND", "존재하지 않는 소방서입니다."),

    // ============================================
    // Dispatch (5000번대)
    // ============================================
    DISPATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "DISPATCH_NOT_FOUND", "출동 지령을 찾을 수 없습니다."),
    DISPATCH_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "DISPATCH_ALREADY_ASSIGNED", "이미 배정된 출동입니다."),
    INVALID_DISPATCH_STATUS(HttpStatus.BAD_REQUEST, "INVALID_DISPATCH_STATUS", "유효하지 않은 출동 상태입니다."),
    DISPATCH_CANNOT_BE_MODIFIED(HttpStatus.BAD_REQUEST, "DISPATCH_CANNOT_BE_MODIFIED", "출동 정보를 수정할 수 없습니다."),

    // ============================================
    // Emergency Report (6000번대)
    // ============================================
    EMERGENCY_REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "EMERGENCY_REPORT_NOT_FOUND", "구급일지를 찾을 수 없습니다."),
    EMERGENCY_REPORT_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMERGENCY_REPORT_ALREADY_EXISTS", "해당 출동에 대한 구급일지가 이미 존재합니다."),
    INVALID_REPORT_SECTION_TYPE(HttpStatus.BAD_REQUEST, "INVALID_REPORT_SECTION_TYPE", "유효하지 않은 섹션 타입입니다."),
    REPORT_SECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT_SECTION_NOT_FOUND", "구급일지 섹션을 찾을 수 없습니다."),
    INVALID_JSONB_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_JSONB_FORMAT", "데이터 형식이 올바르지 않습니다."),

    // ============================================
    // Hospital (7000번대)
    // ============================================
    HOSPITAL_NOT_FOUND(HttpStatus.NOT_FOUND, "HOSPITAL_NOT_FOUND", "병원을 찾을 수 없습니다."),
    HOSPITAL_ALREADY_EXISTS(HttpStatus.CONFLICT, "HOSPITAL_ALREADY_EXISTS", "이미 존재하는 병원입니다."),
    HOSPITAL_SELECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "HOSPITAL_SELECTION_NOT_FOUND", "병원 이송 요청을 찾을 수 없습니다."),
    HOSPITAL_SELECTION_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "HOSPITAL_SELECTION_ALREADY_PROCESSED", "이미 처리된 요청입니다."),
    INVALID_HOSPITAL_SELECTION_STATUS(HttpStatus.BAD_REQUEST, "INVALID_HOSPITAL_SELECTION_STATUS", "유효하지 않은 병원 선택 상태입니다."),
    NO_AVAILABLE_HOSPITALS(HttpStatus.NOT_FOUND, "NO_AVAILABLE_HOSPITALS", "이송 가능한 병원이 없습니다."),
    UNAUTHORIZED_HOSPITAL_RESPONSE(HttpStatus.FORBIDDEN, "UNAUTHORIZED_HOSPITAL_RESPONSE", "이 요청에 응답할 권한이 없습니다."),

    // ============================================
    // Vital Signs (8000번대)
    // ============================================
    VITAL_SIGN_NOT_FOUND(HttpStatus.NOT_FOUND, "VITAL_SIGN_NOT_FOUND", "바이탈 사인 기록을 찾을 수 없습니다."),
    INVALID_VITAL_SIGN_VALUE(HttpStatus.BAD_REQUEST, "INVALID_VITAL_SIGN_VALUE", "유효하지 않은 바이탈 사인 값입니다."),
    VITAL_SIGN_OUT_OF_RANGE(HttpStatus.BAD_REQUEST, "VITAL_SIGN_OUT_OF_RANGE", "바이탈 사인 값이 정상 범위를 벗어났습니다."),

    // ============================================
    // AI (9000번대)
    // ============================================
    STT_TRANSCRIPT_NOT_FOUND(HttpStatus.NOT_FOUND, "STT_TRANSCRIPT_NOT_FOUND", "STT 녹취록을 찾을 수 없습니다."),
    STT_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "STT_PROCESSING_FAILED", "음성 인식 처리에 실패했습니다."),
    LLM_SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND, "LLM_SUMMARY_NOT_FOUND", "LLM 요약을 찾을 수 없습니다."),
    LLM_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "LLM_PROCESSING_FAILED", "요약 생성에 실패했습니다."),
    INVALID_AUDIO_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_AUDIO_FORMAT", "지원하지 않는 오디오 형식입니다."),
    AUDIO_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "AUDIO_FILE_TOO_LARGE", "오디오 파일 크기가 너무 큽니다."),

    // ============================================
    // 파일 스토리지 (9100번대)
    // ============================================
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED", "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_DELETE_FAILED", "파일 삭제에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "빈 파일은 업로드할 수 없습니다."),
    INVALID_VIDEO_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_VIDEO_FORMAT", "지원하지 않는 영상 형식입니다."),
    VIDEO_FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "VIDEO_FILE_TOO_LARGE", "영상 파일 크기가 너무 큽니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}