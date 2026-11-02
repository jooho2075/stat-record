package io.github.jooho2075.statrecord.game.repository;

import io.github.jooho2075.statrecord.game.domain.BaseRunningEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BaseRunningEventRepository extends JpaRepository<BaseRunningEvent, Long> {

}
