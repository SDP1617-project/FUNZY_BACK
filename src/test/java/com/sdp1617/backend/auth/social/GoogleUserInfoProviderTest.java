package com.sdp1617.backend.auth.social;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GoogleUserInfoProviderTest {

    private static final String CLIENT_ID = "test-client-id.apps.googleusercontent.com";
    private static final String ISSUER = "https://accounts.google.com";

    private static KeyPair keyPair;
    private static KeyPair otherKeyPair;
    private static JwtDecoder decoder;

    @BeforeAll
    static void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        otherKeyPair = generator.generateKeyPair();

        NimbusJwtDecoder nimbusDecoder = NimbusJwtDecoder
                .withPublicKey((RSAPublicKey) keyPair.getPublic())
                .build();
        OAuth2TokenValidator<Jwt> timestampValidator = JwtValidators.createDefault();
        OAuth2TokenValidator<Jwt> issuerValidator = new GoogleIssuerValidator();
        OAuth2TokenValidator<Jwt> audienceValidator = new GoogleAudienceValidator(CLIENT_ID);
        nimbusDecoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(timestampValidator, issuerValidator, audienceValidator));
        decoder = nimbusDecoder;
    }

    private String signedToken(KeyPair signingKey, String issuer, String audience, String subject,
                                String email, Boolean emailVerified, Instant expiry) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject(subject)
                .expirationTime(Date.from(expiry))
                .issueTime(Date.from(Instant.now()));
        if (email != null) {
            claims.claim("email", email);
        }
        if (emailVerified != null) {
            claims.claim("email_verified", emailVerified);
        }

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims.build());
        JWSSigner signer = new RSASSASigner(signingKey.getPrivate());
        signedJWT.sign(signer);
        return signedJWT.serialize();
    }

    private String validToken(String email, Boolean emailVerified) throws Exception {
        return signedToken(keyPair, ISSUER, CLIENT_ID, "98765", email, emailVerified,
                Instant.now().plusSeconds(3600));
    }

    @Test
    void 정상_토큰을_파싱한다() throws Exception {
        GoogleUserInfoProvider provider = new GoogleUserInfoProvider(decoder);

        SocialUserInfo info = provider.fetchUserInfo(validToken("test@gmail.com", true));

        assertEquals("98765", info.externalId());
        assertEquals("test@gmail.com", info.email());
    }

    @Test
    void 이메일이_검증되지_않았으면_email이_null이다() throws Exception {
        GoogleUserInfoProvider provider = new GoogleUserInfoProvider(decoder);

        SocialUserInfo info = provider.fetchUserInfo(validToken("test@gmail.com", false));

        assertNull(info.email());
    }

    @Test
    void 서명이_다른_키로_되어있으면_AUTH_013_예외를_던진다() throws Exception {
        GoogleUserInfoProvider provider = new GoogleUserInfoProvider(decoder);
        String token = signedToken(otherKeyPair, ISSUER, CLIENT_ID, "98765", "test@gmail.com", true,
                Instant.now().plusSeconds(3600));

        CustomException exception = assertThrows(CustomException.class, () -> provider.fetchUserInfo(token));

        assertEquals(ErrorCode.AUTH_013, exception.getErrorCode());
    }

    @Test
    void 만료된_토큰이면_AUTH_013_예외를_던진다() throws Exception {
        GoogleUserInfoProvider provider = new GoogleUserInfoProvider(decoder);
        String token = signedToken(keyPair, ISSUER, CLIENT_ID, "98765", "test@gmail.com", true,
                Instant.now().minusSeconds(3600));

        CustomException exception = assertThrows(CustomException.class, () -> provider.fetchUserInfo(token));

        assertEquals(ErrorCode.AUTH_013, exception.getErrorCode());
    }

    @Test
    void aud가_우리_클라이언트_ID와_다르면_AUTH_013_예외를_던진다() throws Exception {
        GoogleUserInfoProvider provider = new GoogleUserInfoProvider(decoder);
        String token = signedToken(keyPair, ISSUER, "other-app.apps.googleusercontent.com", "98765",
                "test@gmail.com", true, Instant.now().plusSeconds(3600));

        CustomException exception = assertThrows(CustomException.class, () -> provider.fetchUserInfo(token));

        assertEquals(ErrorCode.AUTH_013, exception.getErrorCode());
    }

    @Test
    void issuer가_다르면_AUTH_013_예외를_던진다() throws Exception {
        GoogleUserInfoProvider provider = new GoogleUserInfoProvider(decoder);
        String token = signedToken(keyPair, "https://evil.example.com", CLIENT_ID, "98765",
                "test@gmail.com", true, Instant.now().plusSeconds(3600));

        CustomException exception = assertThrows(CustomException.class, () -> provider.fetchUserInfo(token));

        assertEquals(ErrorCode.AUTH_013, exception.getErrorCode());
    }

    @Test
    void 스킴이_없는_issuer도_허용한다() throws Exception {
        GoogleUserInfoProvider provider = new GoogleUserInfoProvider(decoder);
        String token = signedToken(keyPair, "accounts.google.com", CLIENT_ID, "98765",
                "test@gmail.com", true, Instant.now().plusSeconds(3600));

        SocialUserInfo info = provider.fetchUserInfo(token);

        assertEquals("98765", info.externalId());
    }
}
