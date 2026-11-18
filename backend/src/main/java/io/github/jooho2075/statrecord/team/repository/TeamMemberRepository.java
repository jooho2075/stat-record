package io.github.jooho2075.statrecord.team.repository;

import io.github.jooho2075.statrecord.team.domain.TeamMember;
import io.github.jooho2075.statrecord.team.domain.TeamRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    Optional<TeamMember> findByTeamIdAndMemberId(Long teamId, Long memberId);

    boolean existsByTeamIdAndMemberId(Long teamId, Long memberId);

    /** 여러 팀의 감독을 회원 정보와 함께 한 번에 조회*/
    @Query("""
        select tm from TeamMember tm
        join fetch tm.member
        where tm.team.id in :teamIds and tm.role = :role
        """)
    List<TeamMember> findByTeamIdsAndRoleWithMember(@Param("teamIds") List<Long> teamIds,
                                                    @Param("role") TeamRole role);

    /** 여러 팀의 인원 수를 한 번에 조회*/
    @Query("""
            select tm.team.id as teamId, count(tm) as memberCount
            from TeamMember tm
            where tm.team.id in :teamIds
            group by tm.team.id
        """)
    List<TeamMemberCount> countByTeamIds(@Param("teamIds") List<Long> teamIds);

}
