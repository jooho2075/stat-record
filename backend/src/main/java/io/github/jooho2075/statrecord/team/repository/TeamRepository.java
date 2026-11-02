package io.github.jooho2075.statrecord.team.repository;

import io.github.jooho2075.statrecord.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

}
