package com.sdp1617.backend.mypage.controller;

import com.sdp1617.backend.global.common.response.ApiResponse;
import com.sdp1617.backend.mypage.dto.TermsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage/support")
@Tag(name = "마이페이지 - 고객 지원", description = "약관/정책 링크 조회 API")
public class SupportController {

    @Value("${app.support.terms-of-service-url}")
    private String termsOfServiceUrl;

    @Value("${app.support.privacy-policy-url}")
    private String privacyPolicyUrl;

    @Operation(summary = "이용약관/개인정보처리방침 링크 조회", description = """
            이용약관, 개인정보처리방침 문서 링크를 조회합니다.
            - 로그인 여부와 관계없이 호출할 수 있습니다.
            - 클라이언트는 반환된 URL을 웹뷰 또는 외부 브라우저로 열어 보여주면 됩니다.
            """)
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TermsResponse.class),
                            examples = @ExampleObject(value = """
                            {
                              "success": true,
                              "code": "200",
                              "message": "약관/정책 링크를 조회했습니다.",
                              "data": {
                                "termsOfServiceUrl": "https://sdp1617.example.com/terms",
                                "privacyPolicyUrl": "https://sdp1617.example.com/privacy"
                              }
                            }
                            """)))
    })
    @GetMapping("/terms")
    public ApiResponse<TermsResponse> getTerms() {
        return ApiResponse.ok("약관/정책 링크를 조회했습니다.", new TermsResponse(termsOfServiceUrl, privacyPolicyUrl));
    }
}
