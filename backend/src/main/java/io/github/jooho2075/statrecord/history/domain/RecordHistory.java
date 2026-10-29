package io.github.jooho2075.statrecord.history.domain;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "record_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RecordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String tableName;

    @Column(nullable = false)
    private Long recordId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HistoryAction action;

    @Column(nullable = false)
    private Long changedBy;

    @Column(nullable = false)
    private Instant changedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    private String beforeData;

    @JdbcTypeCode(SqlTypes.JSON)
    private String afterData;

    public static RecordHistory create(String tableName, Long recordId, HistoryAction action,
                                       Long changedBy, String beforeData, String afterData) {
        RecordHistory history = new RecordHistory();
        history.tableName = tableName;
        history.recordId = recordId;
        history.action = action;
        history.changedBy = changedBy;
        history.changedAt = Instant.now();
        history.beforeData = beforeData;
        history.afterData = afterData;
        return history;
    }
}