package com.sdp1617.backend.auth.social;

import com.sdp1617.backend.global.error.CustomException;
import com.sdp1617.backend.global.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class NaverUserInfoProviderTest {

    @Test
    void 네이버_유저정보를_정상적으로_파싱한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        NaverUserInfoProvider provider = new NaverUserInfoProvider(builder);

        server.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess(
                        "{\"resultcode\":\"00\",\"message\":\"success\",\"response\":{\"id\":\"32742776\",\"email\":\"test@naver.com\"}}",
                        MediaType.APPLICATION_JSON
                ));

        SocialUserInfo info = provider.fetchUserInfo("test-token");

        assertEquals("32742776", info.externalId());
        assertEquals("test@naver.com", info.email());
    }

    @Test
    void resultcode가_성공이_아니면_AUTH_013_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        NaverUserInfoProvider provider = new NaverUserInfoProvider(builder);

        server.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
                .andRespond(withSuccess(
                        "{\"resultcode\":\"024\",\"message\":\"Authentication failed (Access Token expired)\"}",
                        MediaType.APPLICATION_JSON
                ));

        CustomException exception = assertThrows(CustomException.class, () -> provider.fetchUserInfo("test-token"));

        assertEquals(ErrorCode.AUTH_013, exception.getErrorCode());
    }

    @Test
    void 응답에_id가_없으면_AUTH_013_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        NaverUserInfoProvider provider = new NaverUserInfoProvider(builder);

        server.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
                .andRespond(withSuccess("{\"resultcode\":\"00\",\"message\":\"success\"}", MediaType.APPLICATION_JSON));

        CustomException exception = assertThrows(CustomException.class, () -> provider.fetchUserInfo("test-token"));

        assertEquals(ErrorCode.AUTH_013, exception.getErrorCode());
    }

    @Test
    void 네이버_API_호출이_실패하면_AUTH_013_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        NaverUserInfoProvider provider = new NaverUserInfoProvider(builder);

        server.expect(requestTo("https://openapi.naver.com/v1/nid/me"))
                .andRespond(withServerError());

        CustomException exception = assertThrows(CustomException.class, () -> provider.fetchUserInfo("test-token"));

        assertEquals(ErrorCode.AUTH_013, exception.getErrorCode());
    }
}
