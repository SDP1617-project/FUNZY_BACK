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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "이메일 회원가입", description = """
            이메일/비밀번호로 신규 가입합니다.
            - 비밀번호는 8자 이상, 영문+숫자+특수문자 조합이어야 합니다.
            - 닉네임은 2~20자 이내여야 하며 중복될 수 없습니다.
            - 가입 완료 후 자동 로그인되지 않으며, 로그인 화면으로 이동해 별도로 로그인해야 합니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "가입 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "code": "201",
                              "message": "회원가입이 완료되었습니다.",
                              "data": null
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이메일/닉네임 중복",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "이메일 중복", value = """
                                    {
                                      "success": false,
                                      "code": "AUTH_006",
                                      "message": "이미 가입된 이메일입니다.",
                                      "data": null
                                    }
                                    """),
                            @ExampleObject(name = "닉네임 중복", value = """
                                    {
                                      "success": false,
                                      "code": "AUTH_007",
                                      "message": "이미 사용 중인 닉네임입니다.",
                                      "data": null
                                    }
                                    """)
                    })),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "비밀번호 확인 불일치",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "code": "AUTH_008",
                              "message": "비밀번호가 일치하지 않습니다.",
                              "data": null
                            }
                            """)))
    })
    @PostMapping("/signup")
    public ApiResponse<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ApiResponse.created("회원가입이 완료되었습니다.", null);
    }

    @Operation(summary = "닉네임 중복 확인", description = """
            닉네임 사용 가능 여부를 실시간으로 확인합니다.
            - 회원가입, 소셜 회원가입, 닉네임 변경 화면에서 공통으로 사용합니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = NicknameCheckResponse.class),
                            examples = {
                            @ExampleObject(name = "사용 가능", value = """
                                    {
                                      "success": true,
                                      "code": "200",
                                      "message": "닉네임 사용 가능 여부를 조회했습니다.",
                                      "data": { "available": true }
                                    }
                                    """),
                            @ExampleObject(name = "이미 사용 중", value = """
                                    {
                                      "success": true,
                                      "code": "200",
                                      "message": "닉네임 사용 가능 여부를 조회했습니다.",
                                      "data": { "available": false }
                                    }
                                    """)
                    }))
    })
    @GetMapping("/nickname/check")
    public ApiResponse<NicknameCheckResponse> checkNickname(
            @RequestParam @NotBlank(message = "닉네임을 입력해주세요.") String nickname
    ) {
        NicknameCheckResponse response = new NicknameCheckResponse(authService.isNicknameAvailable(nickname));
        return ApiResponse.ok("닉네임 사용 가능 여부를 조회했습니다.", response);
    }

    @Operation(summary = "이메일 로그인", description = """
            이메일/비밀번호로 로그인합니다.
            - 존재하지 않는 이메일, 비밀번호 불일치, 소셜 전용 계정으로 로그인 시도한 경우 모두 동일한 오류(AUTH_001)로 응답합니다. 계정 존재 여부가 외부에 드러나지 않도록 하기 위한 의도된 동작입니다.
            - 5회 연속 로그인 실패 시 계정이 잠기며, 이메일 인증(계정 잠금 해제)으로만 풀 수 있습니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "code": "200",
                              "message": "로그인에 성공하였습니다.",
                              "data": {
                                "accessToken": "eyJhbGciOiJIUzM4NCJ9...",
                                "refreshToken": "eyJhbGciOiJIUzM4NCJ9..."
                              }
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패 (아이디 없음/비밀번호 불일치/소셜전용 계정 공통)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "code": "AUTH_001",
                              "message": "아이디 또는 비밀번호가 일치하지 않습니다.",
                              "data": null
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "423", description = "5회 로그인 실패로 계정 잠김",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "code": "AUTH_010",
                              "message": "5회 로그인 실패로 계정이 잠겼습니다. 이메일 인증으로 잠금을 해제해주세요.",
                              "data": null
                            }
                            """)))
    })
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(request);
        return ApiResponse.ok("로그인에 성공하였습니다.", response);
    }

    @Operation(summary = "비밀번호 재설정 링크 발송", description = """
            입력한 이메일로 비밀번호 재설정 링크를 발송합니다.
            - 가입되지 않은 이메일이거나 소셜 전용 계정이어도 항상 동일하게 200을 반환합니다(계정 존재 여부 비노출). 실제 메일은 가입된 이메일 계정에만 발송됩니다.
            - 발송된 링크는 15분간 유효합니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 접수 (실제 존재 여부와 무관하게 항상 200)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "code": "200",
                              "message": "비밀번호 재설정 링크를 발송했습니다.",
                              "data": null
                            }
                            """)))
    })
    @PostMapping("/password/reset-request")
    public ApiResponse<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        authService.requestPasswordReset(request.email());
        return ApiResponse.ok("비밀번호 재설정 링크를 발송했습니다.", null);
    }

    @Operation(summary = "비밀번호 재설정", description = """
            이메일로 받은 토큰으로 비밀번호를 재설정합니다.
            - 재설정 성공 시 계정 잠금이 함께 해제되고, 로그인되어 있던 다른 기기의 세션도 모두 종료됩니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재설정 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "code": "200",
                              "message": "비밀번호가 재설정되었습니다.",
                              "data": null
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "새 비밀번호 확인 불일치",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "code": "AUTH_008",
                              "message": "비밀번호가 일치하지 않습니다.",
                              "data": null
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "토큰 만료 또는 유효하지 않음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "code": "AUTH_011",
                              "message": "유효하지 않거나 만료된 링크입니다.",
                              "data": null
                            }
                            """)))
    })
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetConfirmRequest request) {
        authService.resetPassword(request.token(), request.newPassword(), request.newPasswordConfirm());
        return ApiResponse.ok("비밀번호가 재설정되었습니다.", null);
    }

    @Operation(summary = "계정 잠금 해제 링크 발송", description = """
            5회 로그인 실패로 잠긴 계정의 잠금 해제 링크를 이메일로 발송합니다.
            - 가입되지 않은 이메일이어도 항상 동일하게 200을 반환합니다(계정 존재 여부 비노출).
            - 발송된 링크는 15분간 유효합니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "요청 접수 (실제 존재 여부와 무관하게 항상 200)",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "code": "200",
                              "message": "계정 잠금 해제 링크를 발송했습니다.",
                              "data": null
                            }
                            """)))
    })
    @PostMapping("/account/unlock-request")
    public ApiResponse<Void> requestAccountUnlock(@Valid @RequestBody AccountUnlockRequest request) {
        authService.requestAccountUnlock(request.email());
        return ApiResponse.ok("계정 잠금 해제 링크를 발송했습니다.", null);
    }

    @Operation(summary = "계정 잠금 해제", description = """
            이메일로 받은 토큰으로 계정 잠금을 해제합니다.
            - 해제 성공 시 실패 횟수가 초기화되고, 로그인되어 있던 다른 기기의 세션도 모두 종료됩니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "잠금 해제 성공",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "code": "200",
                              "message": "계정 잠금이 해제되었습니다.",
                              "data": null
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "토큰 만료 또는 유효하지 않음",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "code": "AUTH_011",
                              "message": "유효하지 않거나 만료된 링크입니다.",
                              "data": null
                            }
                            """)))
    })
    @PostMapping("/account/unlock")
    public ApiResponse<Void> unlockAccount(@Valid @RequestBody AccountUnlockConfirmRequest request) {
        authService.unlockAccount(request.token());
        return ApiResponse.ok("계정 잠금이 해제되었습니다.", null);
    }
}
