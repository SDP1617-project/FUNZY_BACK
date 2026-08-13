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

class KakaoUserInfoProviderTest {

    @Test
    void 카카오_유저정보를_정상적으로_파싱한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoUserInfoProvider provider = new KakaoUserInfoProvider(builder);

        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andExpect(header("Authorization", "Bearer test-token"))
                .andRespond(withSuccess(
                        "{\"id\":12345,\"kakao_account\":{\"email\":\"test@kakao.com\"}}",
                        MediaType.APPLICATION_JSON
                ));

        SocialUserInfo info = provider.fetchUserInfo("test-token");

        assertEquals("12345", info.externalId());
        assertEquals("test@kakao.com", info.email());
    }

    @Test
    void 이메일_동의를_안했으면_email이_null이다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoUserInfoProvider provider = new KakaoUserInfoProvider(builder);

        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(withSuccess("{\"id\":12345}", MediaType.APPLICATION_JSON));

        SocialUserInfo info = provider.fetchUserInfo("test-token");

        assertEquals("12345", info.externalId());
        assertEquals(null, info.email());
    }

    @Test
    void 응답에_id가_없으면_AUTH_013_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoUserInfoProvider provider = new KakaoUserInfoProvider(builder);

        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        CustomException exception = assertThrows(CustomException.class, () -> provider.fetchUserInfo("test-token"));

        assertEquals(ErrorCode.AUTH_013, exception.getErrorCode());
    }

    @Test
    void 카카오_API_호출이_실패하면_AUTH_013_예외를_던진다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoUserInfoProvider provider = new KakaoUserInfoProvider(builder);

        server.expect(requestTo("https://kapi.kakao.com/v2/user/me"))
                .andRespond(withServerError());

        CustomException exception = assertThrows(CustomException.class, () -> provider.fetchUserInfo("test-token"));

        assertEquals(ErrorCode.AUTH_013, exception.getErrorCode());
    }
}
