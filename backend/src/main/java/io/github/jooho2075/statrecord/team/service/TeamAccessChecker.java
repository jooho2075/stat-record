package io.github.jooho2075.statrecord.team.service;

// 팀 권한 확인 공통 로직

import io.github.jooho2075.statrecord.global.exception.BusinessException;
import io.github.jooho2075.statrecord.global.exception.ErrorCode;
import io.github.jooho2075.statrecord.team.domain.TeamMember;
import io.github.jooho2075.statrecord.team.repository.TeamMemberRepository;
import io.github.jooho2075.statrecord.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 팀원/감독 여부 확인(팀, 경기, 기록 API에서 공통으로 사용)*/
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamAccessChecker {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;

    /** 해당 팀의 팀원이여야 함*/
    public TeamMember requireMember(Long teamId, Long memberId) {
        if(!teamRepository.existsById(teamId)) {
            throw new BusinessException(ErrorCode.TEAM_NOT_FOUND);
        }
        return teamMemberRepository.findByTeamIdAndMemberId(teamId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_TEAM_MEMBER));
    }

    /** 해당 팀의 감독이어야 함*/
    public TeamMember requireManager(Long teamId, Long memberId) {
        TeamMember teamMember = requireMember(teamId, memberId);
        if(!teamMember.isManager()) {
            throw new BusinessException(ErrorCode.NOT_TEAM_MEMBER);
        }
        return teamMember;
    }
}
