package io.github.jooho2075.statrecord.notification.domain;

import io.github.jooho2075.statrecord.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Entity
@Table(name = "notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @Enumerated(EnumType.STRING)
    @JoinColumn(name = "receiver_id", nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    private Long relatedId;

    private Instant readAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static Notification create(Member receiver, NotificationType type,
                                      String message, Long relatedId) {
        Notification notification = new Notification();
        notification.receiver = receiver;
        notification.type = type;
        notification.message = message;
        notification.relatedId = relatedId;
        notification.readAt = Instant.now();
        return notification;
    }

    public boolean isRead() {
        return readAt != null;
    }

    // 이미 읽은 알림은 처음 읽은 시각을 유지
    public void read() {
        if(!isRead()) {
            this.readAt = Instant.now();
        }
    }
}
