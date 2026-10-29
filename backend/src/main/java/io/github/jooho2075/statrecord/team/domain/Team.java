package io.github.jooho2075.statrecord.team.domain;

import io.github.jooho2075.statrecord.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "team")
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Team extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    public static Team create(String name) {
        Team team = new Team();
        team.name = name;
        return team;
    }
}
