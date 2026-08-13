package com.sdp1617.backend.auth.social;

import java.util.Set;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class GoogleIssuerValidator implements OAuth2TokenValidator<Jwt> {

    // 구글 ID 토큰의 iss 클레임은 두 형태 모두 유효한 것으로 문서화되어 있다
    private static final Set<String> VALID_ISSUERS = Set.of(
            "https://accounts.google.com",
            "accounts.google.com"
    );

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (VALID_ISSUERS.contains(jwt.getClaimAsString("iss"))) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "The iss claim is not valid.", null));
    }
}
