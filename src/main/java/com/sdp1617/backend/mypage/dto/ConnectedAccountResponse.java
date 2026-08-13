package com.sdp1617.backend.mypage.dto;

import com.sdp1617.backend.auth.entity.AuthProvider;

public record ConnectedAccountResponse(
        AuthProvider provider,
        boolean hasPassword
) {
}
