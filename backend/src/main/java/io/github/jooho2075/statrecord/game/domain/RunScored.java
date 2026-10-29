package io.github.jooho2075.statrecord.game.domain;

import io.github.jooho2075.statrecord.global.entity.BasedAuditEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "run_scored")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunScored extends BasedAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plate_appearance_id", nullable = false)
    private PlateAppearance plateAppearance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "runner_lineup_id", nullable = false)
    private Lineup runnerLineup;

    public static RunScored create(PlateAppearance plateAppearance, Lineup runner) {
        RunScored runScored = new RunScored();
        runScored.plateAppearance = plateAppearance;
        return runScored;
    }
}
