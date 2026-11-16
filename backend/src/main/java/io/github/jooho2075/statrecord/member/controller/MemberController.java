package io.github.jooho2075.statrecord.member.controller;

import io.github.jooho2075.statrecord.global.exception.BusinessException;
import io.github.jooho2075.statrecord.global.exception.ErrorCode;
import io.github.jooho2075.statrecord.global.security.SecurityUtil;
import io.github.jooho2075.statrecord.member.dto.MemberResponse;
import io.github.jooho2075.statrecord.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원", description = "회원 정보")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMe() {
        Long memberId = SecurityUtil.currentMemberId()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return ResponseEntity.ok(memberService.getMe(memberId));
    }
}
