package io.github.jooho2075.statrecord.team.service;

import io.github.jooho2075.statrecord.global.exception.BusinessException;
import io.github.jooho2075.statrecord.global.exception.ErrorCode;
import io.github.jooho2075.statrecord.member.domain.Member;
import io.github.jooho2075.statrecord.member.repository.MemberRepository;
import io.github.jooho2075.statrecord.team.domain.Team;
import io.github.jooho2075.statrecord.team.domain.TeamMember;
import io.github.jooho2075.statrecord.team.domain.TeamRole;
import io.github.jooho2075.statrecord.team.dto.TeamCreateResponse;
import io.github.jooho2075.statrecord.team.dto.TeamSearchResponse;
import io.github.jooho2075.statrecord.team.repository.TeamMemberCount;
import io.github.jooho2075.statrecord.team.repository.TeamMemberRepository;
import io.github.jooho2075.statrecord.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;

    /** 팀 생성: 만든 사람이 감독이 됨*/
    @Transactional
    public TeamCreateResponse createTeam(Long memberId, String name) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        Team team = teamRepository.save(Team.create(name));
        teamMemberRepository.save(TeamMember.create(team, member, TeamRole.MANAGER, null));
        return new TeamCreateResponse(team.getId(), team.getName());
    }

    /** 팀 검색: 동명 팀 구분을 위해 감독 이름과 인원 수를 함께 변환*/
    public List<TeamSearchResponse> searchTeams(String name) {
        List<Team> teams = teamRepository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(name);
        if(teams.isEmpty()) {
            return List.of();
        }
        List<Long> teamIds = teams.stream().map(Team::getId).toList();

        Map<Long, List<String>> managerNames = teamMemberRepository
                .findByTeamIdsAndRoleWithMember(teamIds, TeamRole.MANAGER).stream()
                .collect(Collectors.groupingBy(
                        tm -> tm.getTeam().getId(),
                        Collectors.mapping(tm -> tm.getMember().getName(), toList())));

        Map<Long, Long> memberCounts = teamMemberRepository.countByTeamIds(teamIds).stream()
                .collect(Collectors.toMap(TeamMemberCount::getTeamId, TeamMemberCount::getMemberCount));

        return teams.stream()
                .map(team -> new TeamSearchResponse(
                        team.getId(),
                        team.getName(),
                        managerNames.getOrDefault(team.getId(), List.of()),
                        memberCounts.getOrDefault(team.getId(), 0L)))
                .toList();
    }
}
