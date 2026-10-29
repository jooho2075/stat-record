package io.github.jooho2075.statrecord.member.domain;

import io.github.jooho2075.statrecord.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private String password;

    public static Member create(String email, String name, String encodedPassword) {
        Member member = new Member();
        member.email = email;
        member.name = name;
        member.password = encodedPassword;
        return member;
    }
}
