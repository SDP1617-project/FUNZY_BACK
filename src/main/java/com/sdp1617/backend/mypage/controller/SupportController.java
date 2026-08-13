package com.sdp1617.backend.mypage.controller;

import com.sdp1617.backend.global.common.response.ApiResponse;
import com.sdp1617.backend.mypage.dto.TermsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage/support")
@Tag(name = "마이페이지 - 고객 지원", description = "약관/정책 링크, 앱 버전 조회 API")
public class SupportController {

    @Value("${app.support.terms-of-service-url}")
    private String termsOfServiceUrl;

    @Value("${app.support.privacy-policy-url}")
    private String privacyPolicyUrl;

    @GetMapping("/terms")
    @Operation(summary = "이용약관/개인정보처리방침 링크 조회")
    public ApiResponse<TermsResponse> getTerms() {
        return ApiResponse.ok("약관/정책 링크를 조회했습니다.", new TermsResponse(termsOfServiceUrl, privacyPolicyUrl));
    }
}
