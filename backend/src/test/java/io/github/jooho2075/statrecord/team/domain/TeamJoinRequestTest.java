package io.github.jooho2075.statrecord.team.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jooho2075.statrecord.member.domain.Member;
import io.github.jooho2075.statrecord.notification.domain.Notification;
import io.github.jooho2075.statrecord.notification.domain.NotificationType;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TeamJoinRequestTest {

    Member manager = Member.create("manager@test.com", "감독", "encoded");
    Member player = Member.create("player@test.com", "선수", "encoded");

    @Test
    @DisplayName("가입 신청은 대기 상태로 생성된다")
    void createPending() {
        TeamJoinRequest request = TeamJoinRequest.create(null, player);

        assertThat(request.isPending()).isTrue();
        assertThat(request.getProcessedBy()).isNull();
    }

    @Test
    @DisplayName("승인하면 상태, 처리한 감독, 처리 시각이 저장된다")
    void approve() {
        TeamJoinRequest request = TeamJoinRequest.create(null, player);

        request.approve(manager);

        assertThat(request.getStatus()).isEqualTo(JoinRequestStatus.APPROVED);
        assertThat(request.getProcessedBy()).isEqualTo(manager);
        assertThat(request.getProcessedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 처리된 신청은 다시 승인·거절할 수 없다")
    void processTwice() {
        TeamJoinRequest request = TeamJoinRequest.create(null, player);
        request.reject(manager);

        assertThatThrownBy(() -> request.approve(manager))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("알림을 다시 읽어도 처음 읽은 시각이 유지된다")
    void readNotificationTwice() {
        Notification notification = Notification.create(
                player, NotificationType.JOIN_APPROVED, "가입이 승인되었습니다.", 1L);
        notification.read();
        Instant firstReadAt = notification.getReadAt();

        notification.read();

        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isEqualTo(firstReadAt);
    }
}