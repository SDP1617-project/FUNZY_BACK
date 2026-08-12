package com.sdp1617.backend.auth.dto;

public final class PasswordPolicy {

    public static final String REGEXP = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";
    public static final String MESSAGE = "비밀번호는 8자 이상, 영문+숫자+특수문자 조합이어야 합니다.";
    public static final int MAX_BYTES = 72;
    public static final String MAX_BYTES_MESSAGE = "비밀번호는 72바이트를 초과할 수 없습니다.";

    private PasswordPolicy() {
    }
}
