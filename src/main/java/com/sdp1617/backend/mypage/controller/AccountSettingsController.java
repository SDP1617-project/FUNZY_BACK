package com.sdp1617.backend.mypage.controller;

import com.sdp1617.backend.mypage.dto.ConnectedAccountResponse;
import com.sdp1617.backend.mypage.dto.LogoutRequest;
import com.sdp1617.backend.mypage.dto.PasswordChangeRequest;
import com.sdp1617.backend.mypage.service.AccountSettingsService;
import com.sdp1617.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/account")
@Tag(name = "마이페이지 - 계정 설정", description = "비밀번호 변경, 연결 계정 조회, 로그아웃, 회원 탈퇴 API")
public class AccountSettingsController {

    private final AccountSettingsService accountSettingsService;

    @GetMapping("/connected")
    @Operation(summary = "연결 계정 조회")
    public ApiResponse<ConnectedAccountResponse> getConnectedAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.ok("연결 계정을 조회했습니다.", accountSettingsService.getConnectedAccount(memberId));
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 변경", description = "소셜 전용 계정은 변경할 수 없습니다.")
    public ApiResponse<Void> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        accountSettingsService.changePassword(
                memberId, request.currentPassword(), request.newPassword(), request.newPasswordConfirm());
        return ApiResponse.ok("비밀번호가 변경되었습니다.", null);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 기기의 세션만 종료합니다.")
    public ApiResponse<Void> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody LogoutRequest request
    ) {
        accountSettingsService.logout(memberId, request.refreshToken());
        return ApiResponse.ok("로그아웃되었습니다.", null);
    }

    @DeleteMapping
    @Operation(summary = "회원 탈퇴", description = "관련 도메인 데이터(아카이브 등) 삭제 범위는 기획 확정 후 별도 처리됩니다.")
    public ApiResponse<Void> withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        accountSettingsService.withdraw(memberId);
        return ApiResponse.ok("회원 탈퇴가 완료되었습니다.", null);
    }
}
