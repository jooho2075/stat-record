package io.github.jooho2075.statrecord.game.domain;

import io.github.jooho2075.statrecord.global.entity.BasedAuditEntity;
import io.github.jooho2075.statrecord.team.domain.TeamMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "lineup")
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Lineup extends BasedAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_member_id", nullable = false)
    private TeamMember teamMember;

    private Short battingOrder;

    @Column(length = 10)
    private String position;

    private Instant confirmedAt;

    public static Lineup create(Game game, TeamMember teamMember, Short battingOrder, String position) {
        Lineup lineup = new Lineup();
        lineup.game = game;
        lineup.teamMember = teamMember;
        lineup.battingOrder = battingOrder;
        lineup.position = position;

        return lineup;
    }

    public boolean isConfirmed() {
        return confirmedAt != null;
    }

    /** 선수 본인이 이 경기 기록을 확정 */
    public void confirm() {
        if(isConfirmed()) {
            throw new IllegalStateException("이미 확정된 기록입니다.");
        }
        this.confirmedAt = Instant.now();
    }

    /** 감독이 확정을 해제(권한 확인은 서비스 계층에서 진행) */
    public void unconfirm() {
        this.confirmedAt = null;
    }
}
