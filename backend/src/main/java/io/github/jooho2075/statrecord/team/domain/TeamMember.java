package io.github.jooho2075.statrecord.team.domain;

import io.github.jooho2075.statrecord.global.entity.BaseTimeEntity;
import io.github.jooho2075.statrecord.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "team_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember extends BaseTimeEntity {
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
    private TeamRole role;

    private Short backNumber;

    public static TeamMember create(Team team, Member member, TeamRole role, Short backNumber) {
        TeamMember teamMember = new TeamMember();
        teamMember.team = team;
        teamMember.member = member;
        teamMember.role = role;
        teamMember.backNumber = backNumber;
        return teamMember;
    }

    public boolean isManager() {
        return role == TeamRole.MANAGER;
    }
}
