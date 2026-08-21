package com.sdp1617.backend.auth.controller;

import com.sdp1617.backend.auth.dto.AccessTokenResponse;
import com.sdp1617.backend.auth.dto.TokenReissueRequest;
import com.sdp1617.backend.auth.service.TokenService;
import com.sdp1617.backend.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "인증 토큰", description = "access token 재발급 API")
public class TokenController {

    private final TokenService tokenService;

    @Operation(summary = "access token 재발급", description = """
            refresh token으로 새 access token을 발급합니다.
            - refresh token 자체는 갱신되지 않으며, 기존 refresh token을 계속 사용합니다.
            - refresh token이 로그아웃/비밀번호 변경/회원 탈퇴 등으로 이미 폐기된 세션이면 재발급에 실패합니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "재발급 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AccessTokenResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "code": "200",
                              "message": "토큰이 재발급되었습니다.",
                              "data": { "accessToken": "eyJhbGciOiJIUzM4NCJ9..." }
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "refreshToken 누락",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                            {
                              "success": false,
                              "code": "COMMON_002",
                              "message": "refreshToken은 필수입니다.",
                              "data": null
                            }
                            """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 토큰, 또는 폐기된 세션",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "유효하지 않은 토큰", value = """
                                    {
                                      "success": false,
                                      "code": "AUTH_003",
                                      "message": "유효하지 않은 토큰입니다.",
                                      "data": null
                                    }
                                    """),
                            @ExampleObject(name = "만료된 토큰", value = """
                                    {
                                      "success": false,
                                      "code": "AUTH_004",
                                      "message": "만료된 토큰입니다.",
                                      "data": null
                                    }
                                    """),
                            @ExampleObject(name = "폐기된 세션 (로그아웃/비밀번호 변경 등)", value = """
                                    {
                                      "success": false,
                                      "code": "AUTH_005",
                                      "message": "저장된 refresh token을 찾을 수 없습니다.",
                                      "data": null
                                    }
                                    """)
                    }))
    })
    @PostMapping("/api/auth/token/reissue")
    public ApiResponse<AccessTokenResponse> reissue(@Valid @RequestBody TokenReissueRequest request) {
        AccessTokenResponse response = tokenService.reissue(request.refreshToken());
        return ApiResponse.ok("토큰이 재발급되었습니다.", response);
    }
}
