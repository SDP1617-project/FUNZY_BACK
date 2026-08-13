package com.sdp1617.backend.auth.controller;

import com.sdp1617.backend.auth.dto.SocialAuthResponse;
import com.sdp1617.backend.auth.dto.SocialLoginRequest;
import com.sdp1617.backend.auth.dto.SocialSignUpCompleteRequest;
import com.sdp1617.backend.auth.dto.TokenResponse;
import com.sdp1617.backend.auth.service.SocialAuthService;
import com.sdp1617.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/social")
@Tag(name = "소셜 인증", description = "카카오/구글 소셜 가입 및 로그인 API")
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    @PostMapping("/login")
    @Operation(summary = "소셜 로그인", description = "기존 계정이면 바로 로그인, 신규면 signupToken을 반환합니다.")
    public ApiResponse<SocialAuthResponse> login(@Valid @RequestBody SocialLoginRequest request) {
        SocialAuthResponse response = socialAuthService.login(request.provider(), request.token());
        return ApiResponse.ok("소셜 인증에 성공하였습니다.", response);
    }

    @PostMapping("/signup/complete")
    @Operation(summary = "소셜 회원가입 완료", description = "닉네임/약관 동의를 받아 신규 소셜 회원가입을 완료합니다.")
    public ApiResponse<TokenResponse> completeSignUp(@Valid @RequestBody SocialSignUpCompleteRequest request) {
        TokenResponse response = socialAuthService.completeSignUp(
                request.signupToken(),
                request.nickname(),
                request.termsAgreed()
        );
        return ApiResponse.ok("회원가입이 완료되었습니다.", response);
    }
}
