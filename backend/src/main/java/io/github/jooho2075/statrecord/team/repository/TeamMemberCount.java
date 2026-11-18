package io.github.jooho2075.statrecord.team.repository;

// 팀별 인원 수를 받는 용도
public interface TeamMemberCount {
    Long getTeamId();
    Long getMemberCount();
}
