package com.sdp1617.backend.auth.controller;

import com.sdp1617.backend.auth.dto.AccountUnlockConfirmRequest;
import com.sdp1617.backend.auth.dto.AccountUnlockRequest;
import com.sdp1617.backend.auth.dto.LoginRequest;
import com.sdp1617.backend.auth.dto.NicknameCheckResponse;
import com.sdp1617.backend.auth.dto.PasswordResetConfirmRequest;
import com.sdp1617.backend.auth.dto.PasswordResetRequest;
import com.sdp1617.backend.auth.dto.SignUpRequest;
import com.sdp1617.backend.auth.dto.TokenResponse;
import com.sdp1617.backend.auth.service.AuthService;
import com.sdp1617.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Validated
@Tag(name = "이메일 인증", description = "이메일 회원가입, 로그인, 비밀번호 찾기, 계정 잠금 해제 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @Operation(summary = "이메일 회원가입")
    public ApiResponse<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ApiResponse.created("회원가입이 완료되었습니다.", null);
    }

    @GetMapping("/nickname/check")
    @Operation(summary = "닉네임 중복 확인")
    public ApiResponse<NicknameCheckResponse> checkNickname(
            @RequestParam @NotBlank(message = "닉네임을 입력해주세요.") String nickname
    ) {
        NicknameCheckResponse response = new NicknameCheckResponse(authService.isNicknameAvailable(nickname));
        return ApiResponse.ok("닉네임 사용 가능 여부를 조회했습니다.", response);
    }

    @PostMapping("/login")
    @Operation(summary = "이메일 로그인")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ApiResponse.ok("로그인에 성공하였습니다.", response);
    }

    @PostMapping("/password/reset-request")
    @Operation(summary = "비밀번호 재설정 링크 발송")
    public ApiResponse<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.email());
        return ApiResponse.ok("비밀번호 재설정 링크를 발송했습니다.", null);
    }

    @PostMapping("/password/reset")
    @Operation(summary = "비밀번호 재설정")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.token(), request.newPassword(), request.newPasswordConfirm());
        return ApiResponse.ok("비밀번호가 재설정되었습니다.", null);
    }

    @PostMapping("/account/unlock-request")
    @Operation(summary = "계정 잠금 해제 링크 발송")
    public ApiResponse<Void> requestAccountUnlock(@Valid @RequestBody AccountUnlockRequest request) {
        authService.requestAccountUnlock(request.email());
        return ApiResponse.ok("계정 잠금 해제 링크를 발송했습니다.", null);
    }

    @PostMapping("/account/unlock")
    @Operation(summary = "계정 잠금 해제")
    public ApiResponse<Void> unlockAccount(@Valid @RequestBody AccountUnlockConfirmRequest request) {
        authService.unlockAccount(request.token());
        return ApiResponse.ok("계정 잠금이 해제되었습니다.", null);
    }
}
