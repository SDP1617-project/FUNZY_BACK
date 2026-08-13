package com.sdp1617.backend.auth.social;

import com.sdp1617.backend.auth.entity.AuthProvider;
import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GoogleUserInfoProvider implements SocialUserInfoProvider {

    private final RestClient restClient;
    private final String expectedClientId;

    public GoogleUserInfoProvider(
            RestClient.Builder restClientBuilder,
            @Value("${google.client-id}") String expectedClientId
    ) {
        this.restClient = restClientBuilder.baseUrl("https://oauth2.googleapis.com").build();
        this.expectedClientId = expectedClientId;
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public SocialUserInfo fetchUserInfo(String idToken) {
        try {
            GoogleTokenInfoResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/tokeninfo").queryParam("id_token", idToken).build())
                    .retrieve()
                    .body(GoogleTokenInfoResponse.class);

            if (response == null || response.sub() == null || !expectedClientId.equals(response.aud())) {
                throw new CustomException(ErrorCode.AUTH_013);
            }

            return new SocialUserInfo(response.sub(), response.email());
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.AUTH_013);
        }
    }

    private record GoogleTokenInfoResponse(
            String sub,
            String email,
            String aud
    ) {
    }
}
