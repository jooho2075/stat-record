package io.github.jooho2075.statrecord.game.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "base_running_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseRunningEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lineup_id", nullable = false)
    private  Lineup lineup;

    @Column(nullable = false)
    private Short inning;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BaseRunningType eventType;

    public static BaseRunningEvent create(Game game, Lineup runner, short inning, BaseRunningType type) {
        BaseRunningEvent event = new BaseRunningEvent();
        event.game = game;
        event.lineup = runner;
        event.inning = inning;
        event.eventType = type;
        return event;
    }
}