package io.github.jooho2075.statrecord.team.repository;

import io.github.jooho2075.statrecord.team.domain.JoinRequestStatus;
import io.github.jooho2075.statrecord.team.domain.TeamJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamJoinRequestRepository extends JpaRepository<TeamJoinRequest, Long> {

    // 같은 팀에 대기 중인 신청이 있는지(중복 신청 방지)
    boolean existsByTeamIdAndMemberIdAndStatus(long teamId, Long memberId, JoinRequestStatus status);;

    // 감독용: 팀의 가입 신청 목록
    List<TeamJoinRequest> findByTeamIdAndStatus(Long teamId,JoinRequestStatus status);

    // 신청자용: 내 가입 신청 목록
    List<TeamJoinRequest> findByMemberIdOrderByCreatedAtDesc(Long memberId);
}
