package io.github.jooho2075.statrecord.team.domain;

import io.github.jooho2075.statrecord.global.entity.BaseTimeEntity;
import io.github.jooho2075.statrecord.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "team_join_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamJoinRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JoinRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private Member processedBy;

    private Instant processedAt;

    public static TeamJoinRequest create(Team team, Member member) {
        TeamJoinRequest request = new TeamJoinRequest();
        request.team = team;
        request.member = member;
        request.status = JoinRequestStatus.PENDING;
        return request;
    }

    public boolean isPending() {
        return status == JoinRequestStatus.PENDING;
    }

    /** 감독이 승인(감독 권한 확인은 서비스 계층에서) */
    public void approve(Member manager) {
        process(manager, JoinRequestStatus.APPROVED);
    }

    /** 감독이 거절*/
    public void reject(Member manager) {
        process(manager, JoinRequestStatus.REJECTED);
    }

    private void process(Member manager, JoinRequestStatus result) {
        if(!isPending()) {
            throw new IllegalStateException("이미 처리된 가입신청입니다.");
        }
        this.status = result;
        this.processedBy = manager;
        this.processedAt = Instant.now();
    }
}
