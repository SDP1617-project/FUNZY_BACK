package com.sdp1617.backend.auth.dto;

import com.sdp1617.backend.auth.entity.AuthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SocialLoginRequest(
        @NotNull(message = "provider는 필수입니다.")
        AuthProvider provider,

        @NotBlank(message = "token은 필수입니다.")
        String token
) {
}
