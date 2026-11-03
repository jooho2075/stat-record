package io.github.jooho2075.statrecord.team.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jooho2075.statrecord.member.domain.Member;
import io.github.jooho2075.statrecord.member.repository.MemberRepository;
import io.github.jooho2075.statrecord.notification.domain.Notification;
import io.github.jooho2075.statrecord.notification.domain.NotificationType;
import io.github.jooho2075.statrecord.notification.repository.NotificationRepository;
import io.github.jooho2075.statrecord.support.TestAuditingConfig;
import io.github.jooho2075.statrecord.support.TestContainersConfig;
import io.github.jooho2075.statrecord.team.domain.JoinRequestStatus;
import io.github.jooho2075.statrecord.team.domain.Team;
import io.github.jooho2075.statrecord.team.domain.TeamJoinRequest;
import java.util.List;
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
class TeamJoinRequestRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired TeamJoinRequestRepository joinRequestRepository;
    @Autowired NotificationRepository notificationRepository;

    Member manager;
    Member player;
    Team team;

    @BeforeEach
    void setUp() {
        manager = memberRepository.save(Member.create("manager@test.com", "감독", "encoded"));
        player = memberRepository.save(Member.create("player@test.com", "선수", "encoded"));
        team = teamRepository.save(Team.create("테스트팀"));
    }

    @Test
    @DisplayName("같은 팀에 대기 중인 가입 신청이 있으면 다시 신청할 수 없다")
    void duplicatePendingRequest() {
        joinRequestRepository.saveAndFlush(TeamJoinRequest.create(team, player));

        assertThatThrownBy(() -> joinRequestRepository.saveAndFlush(TeamJoinRequest.create(team, player)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("거절된 뒤에는 같은 팀에 다시 신청할 수 있다")
    void reapplyAfterRejected() {
        TeamJoinRequest first = joinRequestRepository.saveAndFlush(TeamJoinRequest.create(team, player));
        first.reject(manager);
        joinRequestRepository.saveAndFlush(first);

        joinRequestRepository.saveAndFlush(TeamJoinRequest.create(team, player));

        assertThat(joinRequestRepository.findByMemberIdOrderByCreatedAtDesc(player.getId())).hasSize(2);
    }

    @Test
    @DisplayName("감독은 팀의 대기 중인 신청만 조회할 수 있다")
    void findPendingByTeam() {
        Member otherPlayer = memberRepository.save(Member.create("other@test.com", "다른선수", "encoded"));
        TeamJoinRequest approved = joinRequestRepository.save(TeamJoinRequest.create(team, otherPlayer));
        approved.approve(manager);
        joinRequestRepository.save(TeamJoinRequest.create(team, player));
        joinRequestRepository.flush();

        List<TeamJoinRequest> pending =
                joinRequestRepository.findByTeamIdAndStatus(team.getId(), JoinRequestStatus.PENDING);

        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).getMember().getId()).isEqualTo(player.getId());
        assertThat(joinRequestRepository.existsByTeamIdAndMemberIdAndStatus(
                team.getId(), player.getId(), JoinRequestStatus.PENDING)).isTrue();
    }

    @Test
    @DisplayName("알림은 받는 사람별로 조회된다")
    void findNotificationsByReceiver() {
        notificationRepository.save(Notification.create(
                manager, NotificationType.JOIN_REQUESTED, "선수님이 가입을 신청했습니다.", 1L));
        notificationRepository.save(Notification.create(
                player, NotificationType.JOIN_APPROVED, "테스트팀 가입이 승인되었습니다.", 1L));
        notificationRepository.flush();

        List<Notification> managerNotifications =
                notificationRepository.findByReceiverIdOrderByCreatedAtDesc(manager.getId());

        assertThat(managerNotifications).hasSize(1);
        assertThat(managerNotifications.get(0).getType()).isEqualTo(NotificationType.JOIN_REQUESTED);
        assertThat(managerNotifications.get(0).isRead()).isFalse();
    }
}