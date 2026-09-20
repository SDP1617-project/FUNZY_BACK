package com.sdp1617.backend.global.error;

import java.util.Optional;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * DataIntegrityViolationException의 cause 체인에서 실제로 위반된 DB 유니크 제약 이름을 꺼낸다.
 * 어떤 제약이 위반됐는지에 따라 다른 ErrorCode로 매핑해야 하는 여러 서비스에서 공통으로 쓴다.
 */
public final class ConstraintViolations {

    private ConstraintViolations() {
    }

    public static Optional<String> nameOf(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException constraintViolationException) {
                return Optional.ofNullable(constraintViolationException.getConstraintName());
            }
        }
        return Optional.empty();
    }
}
