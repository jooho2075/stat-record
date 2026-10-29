package io.github.jooho2075.statrecord.game.domain;

import io.github.jooho2075.statrecord.global.entity.BasedAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "plate_appearance")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlateAppearance extends BasedAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private Short inning;

    @Column(name = "is_offense", nullable = false)
    private boolean offense;

    @Column(nullable = false)
    private Integer sequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batter_lineup_id")
    private Lineup batterLineup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pitcher_lineup_id")
    private Lineup pitcherLineup;

    @Convert(converter = ResultCodeConverter.class)
    @Column(nullable = false, length = 5)
    private ResultCode resultCode;

    @Column(nullable = false)
    private Short rbi;

    @Column(nullable = false)
    private Short outsRecorded;

    @Column(nullable = false)
    private Short runsAllowed;

    @Column(nullable = false)
    private Short earnedRuns;

    /** 우리 팀 공격 타석: 타자 본인이 입력 */
    public static PlateAppearance offense(Game game, short inning, int sequence, Lineup batter,
                                          ResultCode resultCode, short rbi, short outsRecorded) {
        PlateAppearance pa = base(game, inning, sequence, resultCode, outsRecorded);
        pa.offense = true;
        pa.batterLineup = batter;
        pa.rbi = rbi;
        pa.runsAllowed = 0;
        pa.earnedRuns = 0;
        return pa;
    }

    /** 우리 팀 수비 타석: 투수 본인이 입력 */
    public static PlateAppearance defense(Game game, short inning, int sequence, Lineup pitcher,
                                          ResultCode resultCode, short outsRecorded,
                                          short runsAllowed, short earnedRuns) {
        if (earnedRuns > runsAllowed) {
            throw new IllegalArgumentException("자책점은 실점보다 클 수 없습니다.");
        }
        PlateAppearance pa = base(game, inning, sequence, resultCode, outsRecorded);
        pa.offense = false;
        pa.pitcherLineup = pitcher;
        pa.rbi = 0;
        pa.runsAllowed = runsAllowed;
        pa.earnedRuns = earnedRuns;
        return pa;
    }

    private static PlateAppearance base(Game game, short inning, int sequence,
                                        ResultCode resultCode, short outsRecorded) {
        PlateAppearance pa = new PlateAppearance();
        pa.game = game;
        pa.inning = inning;
        pa.sequence = sequence;
        pa.resultCode = resultCode;
        pa.outsRecorded = outsRecorded;
        return pa;
    }
}