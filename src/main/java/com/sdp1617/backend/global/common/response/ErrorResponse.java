package com.sdp1617.backend.global.common.response;

import com.sdp1617.backend.global.error.ErrorCode;

public record ErrorResponse(
        boolean success,
        String code,
        String message,
        Object data
) {
    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(false, errorCode.getCode(), message, null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, Object data) {
        return new ErrorResponse(false, errorCode.getCode(), message, data);
    }
}
