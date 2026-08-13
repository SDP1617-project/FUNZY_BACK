package com.sdp1617.backend.auth.social;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class GoogleAudienceValidator implements OAuth2TokenValidator<Jwt> {

    private static final String ERROR_CODE = "invalid_token";

    private final String expectedClientId;

    public GoogleAudienceValidator(String expectedClientId) {
        this.expectedClientId = expectedClientId;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience() != null && jwt.getAudience().contains(expectedClientId)) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error(ERROR_CODE, "이 토큰은 우리 앱을 위해 발급되지 않았습니다.", null));
    }
}
