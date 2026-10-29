-- =========================================
-- V1: 초기 스키마 생성 (docs/erd.md 기준)
-- =========================================

-- 회원
CREATE TABLE member (
    id          BIGSERIAL    PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(50)  NOT NULL,
    password    VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 팀
CREATE TABLE team (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- 팀 소속 (감독 / 일반 회원)
CREATE TABLE team_member (
    id           BIGSERIAL   PRIMARY KEY,
    team_id      BIGINT      NOT NULL REFERENCES team (id),
    member_id    BIGINT      NOT NULL REFERENCES member (id),
    role         VARCHAR(20) NOT NULL,
    back_number  SMALLINT,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_team_member UNIQUE (team_id, member_id),
    CONSTRAINT ck_team_member_role CHECK (role IN ('MANAGER', 'PLAYER'))
);

CREATE INDEX idx_team_member_member ON team_member (member_id);

-- 경기
CREATE TABLE game (
    id                  BIGSERIAL    PRIMARY KEY,
    team_id             BIGINT       NOT NULL REFERENCES team (id),
    opponent_name       VARCHAR(100) NOT NULL,
    played_at           TIMESTAMPTZ  NOT NULL,
    regulation_innings  SMALLINT     NOT NULL DEFAULT 9,
    ended_inning        SMALLINT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'IN_PROGRESS',
    created_by          BIGINT       NOT NULL REFERENCES member (id),
    updated_by          BIGINT       NOT NULL REFERENCES member (id),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_game_regulation_innings CHECK (regulation_innings > 0),
    CONSTRAINT ck_game_ended_inning CHECK (ended_inning BETWEEN 1 AND 12),
    CONSTRAINT ck_game_status CHECK (status IN ('IN_PROGRESS', 'FINISHED'))
);

CREATE INDEX idx_game_team ON game (team_id);

-- 경기별 출전 선수 (선수별 기록 확정 여부 포함)
CREATE TABLE lineup (
    id              BIGSERIAL   PRIMARY KEY,
    game_id         BIGINT      NOT NULL REFERENCES game (id),
    team_member_id  BIGINT      NOT NULL REFERENCES team_member (id),
    batting_order   SMALLINT,
    position        VARCHAR(10),
    confirmed_at    TIMESTAMPTZ,
    created_by      BIGINT      NOT NULL REFERENCES member (id),
    updated_by      BIGINT      NOT NULL REFERENCES member (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_lineup UNIQUE (game_id, team_member_id),
    CONSTRAINT ck_lineup_batting_order CHECK (batting_order BETWEEN 1 AND 20)
);

CREATE INDEX idx_lineup_team_member ON lineup (team_member_id);

-- 타석
CREATE TABLE plate_appearance (
    id                 BIGSERIAL   PRIMARY KEY,
    game_id            BIGINT      NOT NULL REFERENCES game (id),
    inning             SMALLINT    NOT NULL,
    is_offense         BOOLEAN     NOT NULL,
    sequence           INT         NOT NULL,
    batter_lineup_id   BIGINT      REFERENCES lineup (id),
    pitcher_lineup_id  BIGINT      REFERENCES lineup (id),
    result_code        VARCHAR(5)  NOT NULL,
    rbi                SMALLINT    NOT NULL DEFAULT 0,
    outs_recorded      SMALLINT    NOT NULL DEFAULT 0,
    runs_allowed       SMALLINT    NOT NULL DEFAULT 0,
    earned_runs        SMALLINT    NOT NULL DEFAULT 0,
    created_by         BIGINT      NOT NULL REFERENCES member (id),
    updated_by         BIGINT      NOT NULL REFERENCES member (id),
    created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_plate_appearance_sequence UNIQUE (game_id, sequence),
    CONSTRAINT ck_pa_inning CHECK (inning BETWEEN 1 AND 12),
    CONSTRAINT ck_pa_result_code CHECK (result_code IN (
        '1B', '2B', '3B', 'HR', 'BB', 'IBB', 'HBP', 'SO',
        'SH', 'SF', 'GO', 'FO', 'LO', 'ROE', 'FC'
    )),
    CONSTRAINT ck_pa_rbi CHECK (rbi BETWEEN 0 AND 4),
    CONSTRAINT ck_pa_outs CHECK (outs_recorded BETWEEN 0 AND 3),
    CONSTRAINT ck_pa_runs CHECK (runs_allowed BETWEEN 0 AND 4),
    CONSTRAINT ck_pa_earned_runs CHECK (earned_runs BETWEEN 0 AND runs_allowed),
    -- 공격 타석: 우리 팀 타자만 / 수비 타석: 우리 팀 투수만
    CONSTRAINT ck_pa_offense_player CHECK (
        (is_offense = TRUE  AND batter_lineup_id IS NOT NULL AND pitcher_lineup_id IS NULL)
     OR (is_offense = FALSE AND pitcher_lineup_id IS NOT NULL AND batter_lineup_id IS NULL)
    )
);

CREATE INDEX idx_pa_batter ON plate_appearance (batter_lineup_id);
CREATE INDEX idx_pa_pitcher ON plate_appearance (pitcher_lineup_id);

-- 우리 팀 득점 (득점한 주자 본인이 입력)
CREATE TABLE run_scored (
    id                   BIGSERIAL   PRIMARY KEY,
    plate_appearance_id  BIGINT      NOT NULL REFERENCES plate_appearance (id),
    runner_lineup_id     BIGINT      NOT NULL REFERENCES lineup (id),
    created_by           BIGINT      NOT NULL REFERENCES member (id),
    updated_by           BIGINT      NOT NULL REFERENCES member (id),
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_run_scored UNIQUE (plate_appearance_id, runner_lineup_id)
);

CREATE INDEX idx_run_scored_runner ON run_scored (runner_lineup_id);

-- 도루 / 도루실패
CREATE TABLE base_running_event (
    id          BIGSERIAL   PRIMARY KEY,
    game_id     BIGINT      NOT NULL REFERENCES game (id),
    lineup_id   BIGINT      NOT NULL REFERENCES lineup (id),
    inning      SMALLINT    NOT NULL,
    event_type  VARCHAR(10) NOT NULL,
    created_by  BIGINT      NOT NULL REFERENCES member (id),
    updated_by  BIGINT      NOT NULL REFERENCES member (id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_bre_inning CHECK (inning BETWEEN 1 AND 12),
    CONSTRAINT ck_bre_event_type CHECK (event_type IN ('SB', 'CS'))
);

CREATE INDEX idx_bre_game ON base_running_event (game_id);
CREATE INDEX idx_bre_lineup ON base_running_event (lineup_id);

-- 수정 이력
CREATE TABLE record_history (
    id           BIGSERIAL   PRIMARY KEY,
    table_name   VARCHAR(50) NOT NULL,
    record_id    BIGINT      NOT NULL,
    action       VARCHAR(20) NOT NULL,
    changed_by   BIGINT      NOT NULL REFERENCES member (id),
    changed_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    before_data  JSONB,
    after_data   JSONB,
    CONSTRAINT ck_history_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'CONFIRM', 'UNCONFIRM'))
);

CREATE INDEX idx_history_target ON record_history (table_name, record_id);
