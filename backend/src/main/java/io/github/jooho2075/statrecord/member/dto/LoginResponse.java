package io.github.jooho2075.statrecord.member.dto;

import io.github.jooho2075.statrecord.member.domain.Member;

public record LoginResponse(String accessToken, String tokenType, long expiresIn) {
    public static LoginResponse bearer(String accessToken, long expiresIn) {
        return new LoginResponse(accessToken, "Bearer", expiresIn);
    }
}
