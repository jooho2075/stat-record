package io.github.jooho2075.statrecord.team.repository;

import io.github.jooho2075.statrecord.team.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

}
