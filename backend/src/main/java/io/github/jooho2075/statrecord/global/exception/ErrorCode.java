package io.github.jooho2075.statrecord.global.exception;

// api-spec.md에 정의한 코드를 모아둔 곳

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INVALID_STATE(HttpStatus.CONFLICT, "현재 상태에서는 처리할 수 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 주소를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청방식입니다."),

    // 회원 및 팀
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    INVALID_TEAM_OPTION(HttpStatus.BAD_REQUEST, "팀 가입 신청과 팀 생성은 동시에 불가능합니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "팀을 찾을 수 없습니다."),
    NOT_TEAM_MANAGER(HttpStatus.FORBIDDEN, "팀 감독만 할 수 없습니다."),
    JOIN_REQUEST_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 가입 신청입니다."),

    // 경기 및 기록
    NOT_RECORD_OWNER(HttpStatus.FORBIDDEN, "본인 기록만 입력할 수 있습니다."),
    LINEUP_ALREADY_CONFIRMED(HttpStatus.CONFLICT, "이미 확정된 기록입니다."),
    GAME_FINISHED(HttpStatus.CONFLICT, "종료된 경기입니다."),
    INVALID_INNING(HttpStatus.BAD_REQUEST, "이닝은 1~12 사이여야 합니다."),
    INVALID_EARNED_RUNS(HttpStatus.BAD_REQUEST, "자책점은 실점보다 클 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
