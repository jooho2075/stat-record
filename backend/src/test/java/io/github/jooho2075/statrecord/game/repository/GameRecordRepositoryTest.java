package io.github.jooho2075.statrecord.game.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jooho2075.statrecord.game.domain.Game;
import io.github.jooho2075.statrecord.game.domain.GameStatus;
import io.github.jooho2075.statrecord.game.domain.Lineup;
import io.github.jooho2075.statrecord.game.domain.PlateAppearance;
import io.github.jooho2075.statrecord.game.domain.ResultCode;
import io.github.jooho2075.statrecord.game.domain.RunScored;
import io.github.jooho2075.statrecord.member.domain.Member;
import io.github.jooho2075.statrecord.member.repository.MemberRepository;
import io.github.jooho2075.statrecord.support.TestAuditingConfig;
import io.github.jooho2075.statrecord.support.TestContainersConfig;
import io.github.jooho2075.statrecord.team.domain.Team;
import io.github.jooho2075.statrecord.team.domain.TeamMember;
import io.github.jooho2075.statrecord.team.domain.TeamRole;
import io.github.jooho2075.statrecord.team.repository.TeamMemberRepository;
import io.github.jooho2075.statrecord.team.repository.TeamRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestContainersConfig.class, TestAuditingConfig.class})
class GameRecordRepositoryTest {
    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired TeamMemberRepository teamMemberRepository;
    @Autowired GameRepository gameRepository;
    @Autowired LineupRepository lineupRepository;
    @Autowired PlateAppearanceRepository plateAppearanceRepository;
    @Autowired RunScoredRepository runScoredRepository;
    @Autowired EntityManager em;

    Long memberId;
    Game game;
    Lineup lineup;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(Member.create("player@test.com", "선수", "encoded"));
        memberId = member.getId();
        TestAuditingConfig.setCurrentAuditor(memberId);

        Team team = teamRepository.save(Team.create("테스트팀"));
        TeamMember teamMember = teamMemberRepository.save(
                TeamMember.create(team, member, TeamRole.PLAYER, (short) 7));
        game = gameRepository.save(Game.create(team, "상대팀", Instant.now()));
        lineup = lineupRepository.save(Lineup.create(game, teamMember, (short) 1, "SS"));
    }

    @Test
    @DisplayName("경기를 저장하면 작성자와 생성 시각이 자동으로 채워진다")
    void auditing() {
        flushAndClear();

        Game found = gameRepository.findById(game.getId()).orElseThrow();

        assertThat(found.getCreatedBy()).isEqualTo(memberId);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("타석 결과는 DB에 코드(1B)로 저장되고, 조회 시 enum으로 변환된다")
    void resultCodeConverter() {
        PlateAppearance pa = plateAppearanceRepository.save(PlateAppearance.offense(
                game, (short) 1, 1, lineup, ResultCode.SINGLE, (short) 0, (short) 0));
        flushAndClear();

        Object rawCode = em.createNativeQuery(
                        "select result_code from plate_appearance where id = :id")
                .setParameter("id", pa.getId())
                .getSingleResult();
        PlateAppearance found = plateAppearanceRepository.findById(pa.getId()).orElseThrow();

        assertThat(rawCode).isEqualTo("1B");
        assertThat(found.getResultCode()).isEqualTo(ResultCode.SINGLE);
        assertThat(found.isOffense()).isTrue();
        assertThat(found.getPitcherLineup()).isNull();
    }

    @Test
    @DisplayName("같은 경기에서 타석 순서가 중복되면 저장할 수 없다")
    void duplicateSequence() {
        plateAppearanceRepository.saveAndFlush(PlateAppearance.offense(
                game, (short) 1, 1, lineup, ResultCode.SINGLE, (short) 0, (short) 0));

        assertThatThrownBy(() -> plateAppearanceRepository.saveAndFlush(PlateAppearance.offense(
                game, (short) 1, 1, lineup, ResultCode.STRIKEOUT, (short) 0, (short) 1)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("득점 기록은 득점이 발생한 타석과 주자에 연결되어 저장된다")
    void runScored() {
        PlateAppearance pa = plateAppearanceRepository.save(PlateAppearance.offense(
                game, (short) 1, 1, lineup, ResultCode.HOME_RUN, (short) 1, (short) 0));
        RunScored run = runScoredRepository.save(RunScored.create(pa, lineup));
        flushAndClear();

        RunScored found = runScoredRepository.findById(run.getId()).orElseThrow();

        assertThat(found.getPlateAppearance().getId()).isEqualTo(pa.getId());
        assertThat(found.getRunnerLineup().getId()).isEqualTo(lineup.getId());
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }
}