package com.sdp1617.backend.mypage.controller;

import com.sdp1617.backend.mypage.dto.ConnectedAccountResponse;
import com.sdp1617.backend.mypage.dto.LogoutRequest;
import com.sdp1617.backend.mypage.dto.PasswordChangeRequest;
import com.sdp1617.backend.mypage.service.AccountSettingsService;
import com.sdp1617.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "연결 계정 조회", description = """
            현재 로그인 방식(LOCAL/KAKAO/GOOGLE/NAVER)과 비밀번호 보유 여부를 조회합니다.
            - 소셜 전용 계정은 hasPassword가 false이며, 이 경우 비밀번호 변경 메뉴를 노출하지 않아야 합니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ConnectedAccountResponse.class),
                            examples = {
                            @ExampleObject(name = "이메일 가입 계정", value = """
                                    {
                                      "success": true,
                                      "code": "200",
                                      "message": "연결 계정을 조회했습니다.",
                                      "data": { "provider": "LOCAL", "hasPassword": true }
                                    }
                                    """),
                            @ExampleObject(name = "소셜 가입 계정", value = """
                                    {
                                      "success": true,
                                      "code": "200",
                                      "message": "연결 계정을 조회했습니다.",
                                      "data": { "provider": "KAKAO", "hasPassword": false }
                                    }
                                    """)
                    }))
    })
    @GetMapping("/connected")
    public ApiResponse<ConnectedAccountResponse> getConnectedAccount(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        return ApiResponse.ok("연결 계정을 조회했습니다.", accountSettingsService.getConnectedAccount(memberId));
    }

    @Operation(summary = "비밀번호 변경", description = """
            현재 비밀번호 확인 후 새 비밀번호로 변경합니다.
            - 소셜 전용 계정(비밀번호 없음)은 변경할 수 없습니다.
            - 변경 성공 시 로그인되어 있던 모든 기기의 세션이 종료됩니다(현재 요청에 사용된 access token 자체는 만료 전까지 유효).
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "변경 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "code": "200",
                              "message": "비밀번호가 변경되었습니다.",
                              "data": null
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "소셜 전용 계정 / 현재 비밀번호 불일치 / 새 비밀번호 확인 불일치",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "소셜 전용 계정", value = """
                                    {
                                      "success": false,
                                      "code": "AUTH_014",
                                      "message": "소셜 전용 계정은 비밀번호를 변경할 수 없습니다.",
                                      "data": null
                                    }
                                    """),
                            @ExampleObject(name = "현재 비밀번호 불일치", value = """
                                    {
                                      "success": false,
                                      "code": "AUTH_015",
                                      "message": "현재 비밀번호가 일치하지 않습니다.",
                                      "data": null
                                    }
                                    """),
                            @ExampleObject(name = "새 비밀번호 확인 불일치", value = """
                                    {
                                      "success": false,
                                      "code": "AUTH_008",
                                      "message": "비밀번호가 일치하지 않습니다.",
                                      "data": null
                                    }
                                    """)
                    }))
    })
    @PatchMapping("/password")
    public ApiResponse<Void> changePassword(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PasswordChangeRequest request
    ) {
        accountSettingsService.changePassword(
                memberId, request.currentPassword(), request.newPassword(), request.newPasswordConfirm());
        return ApiResponse.ok("비밀번호가 변경되었습니다.", null);
    }

    @Operation(summary = "로그아웃", description = """
            요청 바디로 전달한 refreshToken에 해당하는 세션만 종료합니다.
            - 다른 기기에서 로그인된 세션에는 영향을 주지 않습니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "code": "200",
                              "message": "로그아웃되었습니다.",
                              "data": null
                            }
                            """)))
    })
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody LogoutRequest request
    ) {
        accountSettingsService.logout(memberId, request.refreshToken());
        return ApiResponse.ok("로그아웃되었습니다.", null);
    }

    @Operation(summary = "회원 탈퇴", description = """
            계정을 삭제하고 서비스를 탈퇴합니다.
            - 삭제 후 데이터 복구가 불가능합니다.
            - 현재는 회원 계정만 삭제하며, 아카이브 등 연관 도메인 데이터의 삭제 범위는 기획 확정 후 별도 처리됩니다.
            - 탈퇴 성공 시 로그인되어 있던 모든 기기의 세션이 종료됩니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "code": "200",
                              "message": "회원 탈퇴가 완료되었습니다.",
                              "data": null
                            }
                            """)))
    })
    @DeleteMapping
    public ApiResponse<Void> withdraw(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        accountSettingsService.withdraw(memberId);
        return ApiResponse.ok("회원 탈퇴가 완료되었습니다.", null);
    }
}
