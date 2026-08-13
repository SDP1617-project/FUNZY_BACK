package com.sdp1617.backend.auth.social;

import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GoogleUserInfoProviderTest {

    private static final String EXPECTED_CLIENT_ID = "test-client-id.apps.googleusercontent.com";

    @Test
    void 구글_유저정보를_정상적으로_파싱한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GoogleUserInfoProvider provider = new GoogleUserInfoProvider(builder, EXPECTED_CLIENT_ID);

        server.expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=test-id-token"))
                .andRespond(withSuccess(
                        "{\"sub\":\"98765\",\"email\":\"test@gmail.com\",\"aud\":\"" + EXPECTED_CLIENT_ID + "\"}",
                        MediaType.APPLICATION_JSON
                ));

        SocialUserInfo info = provider.fetchUserInfo("test-id-token");

        assertEquals("98765", info.externalId());
        assertEquals("test@gmail.com", info.email());
    }

    @Test
    void 구글_API_호출이_실패하면_AUTH_013_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GoogleUserInfoProvider provider = new GoogleUserInfoProvider(builder, EXPECTED_CLIENT_ID);

        server.expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=bad-token"))
                .andRespond(withServerError());

        CustomException exception = assertThrows(CustomException.class, () -> provider.fetchUserInfo("bad-token"));

        assertEquals(ErrorCode.AUTH_013, exception.getErrorCode());
    }

    @Test
    void aud가_우리_클라이언트_ID와_다르면_AUTH_013_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GoogleUserInfoProvider provider = new GoogleUserInfoProvider(builder, EXPECTED_CLIENT_ID);

        server.expect(requestTo("https://oauth2.googleapis.com/tokeninfo?id_token=other-app-token"))
                .andRespond(withSuccess(
                        "{\"sub\":\"98765\",\"email\":\"test@gmail.com\",\"aud\":\"other-app.apps.googleusercontent.com\"}",
                        MediaType.APPLICATION_JSON
                ));

        CustomException exception = assertThrows(
                CustomException.class, () -> provider.fetchUserInfo("other-app-token"));

        assertEquals(ErrorCode.AUTH_013, exception.getErrorCode());
    }
}
