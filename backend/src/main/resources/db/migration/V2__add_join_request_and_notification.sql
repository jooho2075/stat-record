-- =========================================
-- V2: 팀 가입 신청, 알림 테이블 추가 (docs/api-spec.md 6장 참고)
-- =========================================

-- 팀 가입 신청
CREATE TABLE team_join_request (
                                   id            BIGSERIAL   PRIMARY KEY,
                                   team_id       BIGINT      NOT NULL REFERENCES team (id),
                                   member_id     BIGINT      NOT NULL REFERENCES member (id),
                                   status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                   processed_by  BIGINT      REFERENCES member (id),
                                   processed_at  TIMESTAMPTZ,
                                   created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                   updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                   CONSTRAINT ck_join_request_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    -- 대기 중이면 처리 정보가 없고, 처리됐으면 처리 정보가 있어야 함
                                   CONSTRAINT ck_join_request_processed CHECK (
                                       (status = 'PENDING'  AND processed_by IS NULL     AND processed_at IS NULL)
                                           OR (status <> 'PENDING' AND processed_by IS NOT NULL AND processed_at IS NOT NULL)
                                       )
);

-- 같은 팀·회원에 대기 중인 신청은 하나만 (승인·거절된 이력은 여러 개 가능)
CREATE UNIQUE INDEX uk_join_request_pending
    ON team_join_request (team_id, member_id)
    WHERE status = 'PENDING';

CREATE INDEX idx_join_request_member ON team_join_request (member_id);

-- 앱 내 알림
CREATE TABLE notification (
                              id           BIGSERIAL    PRIMARY KEY,
                              receiver_id  BIGINT       NOT NULL REFERENCES member (id),
                              type         VARCHAR(30)  NOT NULL,
                              message      VARCHAR(255) NOT NULL,
                              related_id   BIGINT,
                              read_at      TIMESTAMPTZ,
                              created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
                              CONSTRAINT ck_notification_type CHECK (type IN ('JOIN_REQUESTED', 'JOIN_APPROVED', 'JOIN_REJECTED'))
);

CREATE INDEX idx_notification_receiver ON notification (receiver_id, created_at DESC);