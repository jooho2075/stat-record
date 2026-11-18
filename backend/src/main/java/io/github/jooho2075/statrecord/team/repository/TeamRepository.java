package io.github.jooho2075.statrecord.team.repository;

import io.github.jooho2075.statrecord.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    /** 이름에 검색어가 포함된 팀, 최대 20개*/
    List<Team> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);

}
