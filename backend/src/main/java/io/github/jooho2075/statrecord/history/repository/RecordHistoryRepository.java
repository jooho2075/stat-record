package io.github.jooho2075.statrecord.history.repository;

import io.github.jooho2075.statrecord.history.domain.RecordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RecordHistoryRepository extends JpaRepository<RecordHistory, Long> {

}
