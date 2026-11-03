package io.github.jooho2075.statrecord.notification.domain;

public enum NotificationType {
    JOIN_REQUESTED, // 감독에게: 가입 신청 도착
    JOIN_APPROVED, // 신청자에게: 가입 승인
    JOIN_REJECTED //신청자에게: 가입 거절
}
