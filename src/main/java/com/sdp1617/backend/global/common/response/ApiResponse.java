package com.sdp1617.backend.global.common.response;

import org.springframework.http.HttpStatus;

public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return of(HttpStatus.OK, message, data);
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return of(HttpStatus.CREATED, message, data);
    }

    public static <T> ApiResponse<T> of(HttpStatus httpStatus, String message, T data) {
        return new ApiResponse<>(true, String.valueOf(httpStatus.value()), message, data);
    }
}
