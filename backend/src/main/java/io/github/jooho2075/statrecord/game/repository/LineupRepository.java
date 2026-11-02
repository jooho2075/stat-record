package io.github.jooho2075.statrecord.game.repository;

import io.github.jooho2075.statrecord.game.domain.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineupRepository extends JpaRepository<Lineup, Long>{
}
