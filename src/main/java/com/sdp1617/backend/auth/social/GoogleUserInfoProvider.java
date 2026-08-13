package com.sdp1617.backend.auth.social;

import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class GoogleUserInfoProvider implements SocialUserInfoProvider {

    private final JwtDecoder googleJwtDecoder;

    public GoogleUserInfoProvider(@Qualifier("googleJwtDecoder") JwtDecoder googleJwtDecoder) {
        this.googleJwtDecoder = googleJwtDecoder;
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public SocialUserInfo fetchUserInfo(String idToken) {
        try {
            Jwt jwt = googleJwtDecoder.decode(idToken);

            String sub = jwt.getSubject();
            if (sub == null) {
                throw new CustomException(ErrorCode.AUTH_013);
            }

            Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
            String email = Boolean.TRUE.equals(emailVerified) ? jwt.getClaimAsString("email") : null;

            return new SocialUserInfo(sub, email);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.AUTH_013);
        }
    }
}
