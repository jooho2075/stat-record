package io.github.jooho2075.statrecord.team.controller;

import io.github.jooho2075.statrecord.global.exception.BusinessException;
import io.github.jooho2075.statrecord.global.exception.ErrorCode;
import io.github.jooho2075.statrecord.global.security.SecurityUtil;
import io.github.jooho2075.statrecord.team.dto.TeamCreateRequest;
import io.github.jooho2075.statrecord.team.dto.TeamCreateResponse;
import io.github.jooho2075.statrecord.team.dto.TeamSearchResponse;
import io.github.jooho2075.statrecord.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "팀", description = "팀 생성, 검색")
@Validated
@RestController
@RequestMapping("/api/v1/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @Operation(summary = "팀 생성", description = "팀을 만든 회원이 감독이 됩니다")
    @PostMapping
    public ResponseEntity<TeamCreateResponse> createTeam(@Valid @RequestBody TeamCreateRequest request) {
        Long memberId = SecurityUtil.currentMemberId()
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(memberId, request.name()));
    }

    @Operation(summary = "팀 검색", description = "로그인 없이 사용할 수 있으며, 감독 이름과 인원 수를 함께 보여줍니다.")
    @GetMapping
    public ResponseEntity<List<TeamSearchResponse>> searchTeams(@RequestParam @NotBlank String name) {
        return ResponseEntity.ok(teamService.searchTeams(name));
    }
}
