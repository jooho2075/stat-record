package io.github.jooho2075.statrecord.game.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameDomainTest {

    @Test
    @DisplayName("종료 이닝이 1~12를 벗어나면 경기를 종료할 수 없다")
    void finishOutOfRange() {
        Game game = Game.create(null, "상대팀", Instant.now());

        assertThatThrownBy(() -> game.finish((short) 13))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("경기를 종료하면 상태가 종료로 바뀌고 종료 이닝이 저장된다")
    void finish() {
        Game game = Game.create(null, "상대팀", Instant.now());

        game.finish((short) 7);

        assertThat(game.getStatus()).isEqualTo(GameStatus.FINISHED);
        assertThat(game.getEndedInning()).isEqualTo((short) 7);
    }

    @Test
    @DisplayName("이미 확정된 기록은 다시 확정할 수 없다")
    void confirmTwice() {
        Lineup lineup = Lineup.create(null, null, (short) 1, "SS");
        lineup.confirm();

        assertThatThrownBy(lineup::confirm)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("감독이 확정을 해제하면 다시 수정 가능한 상태가 된다")
    void unconfirm() {
        Lineup lineup = Lineup.create(null, null, (short) 1, "SS");
        lineup.confirm();

        lineup.unconfirm();

        assertThat(lineup.isConfirmed()).isFalse();
    }

    @Test
    @DisplayName("자책점이 실점보다 크면 수비 타석을 만들 수 없다")
    void earnedRunsGreaterThanRuns() {
        assertThatThrownBy(() -> PlateAppearance.defense(
                null, (short) 1, 1, null, ResultCode.SINGLE, (short) 0, (short) 1, (short) 2))
                .isInstanceOf(IllegalArgumentException.class);
    }
}