package com.ssairen.global.exception;

import lombok.Getter;

/**
 * 커스텀 예외 클래스
 */
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detailMessage;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detailMessage = null;
    }

    public CustomException(ErrorCode errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    public CustomException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.detailMessage = null;
    }

    public CustomException(ErrorCode errorCode, String detailMessage, Throwable cause) {
        super(detailMessage, cause);
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }
}