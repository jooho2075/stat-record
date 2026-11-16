package io.github.jooho2075.statrecord.member.dto;

import io.github.jooho2075.statrecord.member.domain.Member;

public record MemberResponse(Long memberId, String email, String name) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getId(), member.getEmail(), member.getName());
    }
}
