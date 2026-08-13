package com.sdp1617.backend.auth.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoUserInfoProvider implements SocialUserInfoProvider {

    private final RestClient restClient;

    public KakaoUserInfoProvider(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://kapi.kakao.com").build();
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.KAKAO;
    }

    @Override
    public SocialUserInfo fetchUserInfo(String accessToken) {
        try {
            KakaoUserResponse response = restClient.get()
                    .uri("/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoUserResponse.class);

            if (response == null || response.id() == null) {
                throw new CustomException(ErrorCode.AUTH_013);
            }

            KakaoAccount account = response.kakaoAccount();
            String email = (account != null && Boolean.TRUE.equals(account.isEmailVerified()))
                    ? account.email()
                    : null;
            return new SocialUserInfo(String.valueOf(response.id()), email);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.AUTH_013);
        }
    }

    private record KakaoUserResponse(
            Long id,
            @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {
    }

    private record KakaoAccount(
            String email,
            @JsonProperty("is_email_verified") Boolean isEmailVerified
    ) {
    }
}
