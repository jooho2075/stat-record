package io.github.jooho2075.statrecord.member.dto;

import io.github.jooho2075.statrecord.member.domain.Member;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn) {
    public static LoginResponse bearer(String accessToken, String refreshToken, long expiresIn) {
        return new LoginResponse(accessToken, refreshToken,"Bearer", expiresIn);
    }
}
