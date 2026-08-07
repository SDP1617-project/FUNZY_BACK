package com.sdp1617.backend.auth.jwt;

public record JwtClaims(
        Long memberId,
        String tokenId
) {
}
