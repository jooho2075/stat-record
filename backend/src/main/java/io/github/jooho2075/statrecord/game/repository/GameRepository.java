package io.github.jooho2075.statrecord.game.repository;

import io.github.jooho2075.statrecord.game.domain.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
