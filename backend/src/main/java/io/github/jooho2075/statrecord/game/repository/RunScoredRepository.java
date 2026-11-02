package io.github.jooho2075.statrecord.game.repository;

import io.github.jooho2075.statrecord.game.domain.RunScored;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunScoredRepository extends JpaRepository<RunScored, Long> {
}
