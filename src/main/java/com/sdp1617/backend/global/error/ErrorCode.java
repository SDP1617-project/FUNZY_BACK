package com.sdp1617.backend.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // 에러 코드 네이밍 가이드: DOMAIN_nnn (예: AUTH_001, LETTER_002)

    COMMON_001(HttpStatus.NOT_FOUND, "COMMON_001", "요청하신 리소스를 찾을 수 없습니다."),
    COMMON_002(HttpStatus.BAD_REQUEST, "COMMON_002", "요청 값이 올바르지 않습니다."),
    COMMON_003(HttpStatus.UNAUTHORIZED, "COMMON_003", "인증이 필요합니다."),
    COMMON_004(HttpStatus.FORBIDDEN, "COMMON_004", "접근 권한이 없습니다."),
    COMMON_999(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_999", "서버 내부 오류가 발생했습니다."),

    AUTH_001(HttpStatus.UNAUTHORIZED, "AUTH_001", "아이디 또는 비밀번호가 일치하지 않습니다."),
    AUTH_002(HttpStatus.NOT_FOUND, "AUTH_002", "존재하지 않는 회원입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
