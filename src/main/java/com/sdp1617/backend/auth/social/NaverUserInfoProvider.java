package com.sdp1617.backend.auth.social;

import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class NaverUserInfoProvider implements SocialUserInfoProvider {

    private static final String SUCCESS_RESULT_CODE = "00";

    private final RestClient restClient;

    public NaverUserInfoProvider(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.baseUrl("https://openapi.naver.com").build();
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.NAVER;
    }

    @Override
    public SocialUserInfo fetchUserInfo(String accessToken) {
        try {
            NaverUserResponse response = restClient.get()
                    .uri("/v1/nid/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(NaverUserResponse.class);

            if (response == null
                    || !SUCCESS_RESULT_CODE.equals(response.resultcode())
                    || response.response() == null
                    || response.response().id() == null) {
                throw new CustomException(ErrorCode.AUTH_013);
            }

            return new SocialUserInfo(response.response().id(), response.response().email());
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.AUTH_013);
        }
    }

    private record NaverUserResponse(
            String resultcode,
            NaverProfile response
    ) {
    }

    private record NaverProfile(
            String id,
            String email
    ) {
    }
}
