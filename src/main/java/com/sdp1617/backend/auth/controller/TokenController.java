package com.sdp1617.backend.auth.controller;

import com.sdp1617.backend.auth.dto.AccessTokenResponse;
import com.sdp1617.backend.auth.dto.TokenReissueRequest;
import com.sdp1617.backend.auth.service.TokenService;
import com.sdp1617.backend.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/api/auth/token/reissue")
    public ApiResponse<AccessTokenResponse> reissue(@Valid @RequestBody TokenReissueRequest request) {
        AccessTokenResponse response = tokenService.reissue(request.refreshToken());
        return ApiResponse.ok("토큰이 재발급되었습니다.", response);
    }
}
