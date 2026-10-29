package io.github.jooho2075.statrecord.game.domain;

import io.github.jooho2075.statrecord.global.entity.BaseTimeEntity;
import io.github.jooho2075.statrecord.team.domain.Team;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "game")
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Game extends BaseTimeEntity {

    private static final short DEFAULT_REGULATION_INNINGS = 9;
    private static final short MAX_INNING = 12;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false)
    private String opponentName;

    @Column(nullable = false)
    private Instant playedAt;

    @Column(nullable = false)
    private  Short regulationInnings;

    private Short endedInning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status;

    public static Game create(Team team, String opponentName, Instant playedAt) {
        Game game = new Game();
        game.team = team;
        game.playedAt = playedAt;
        game.regulationInnings = DEFAULT_REGULATION_INNINGS;
        game.status = GameStatus.IN_PROGRESS;
        return game;
    }

    /** 경기 종료: 조기 종료나 연장 포함 실제 종료 이닝을 기록 */
    public void finish(short endedInning) {
        if(endedInning < 1 || endedInning > MAX_INNING) {
            throw new IllegalArgumentException("종료 이닝은 1~12 사이여야 합니다.");
        }
        this.endedInning = endedInning;
        this.status = GameStatus.FINISHED;
    }
}
