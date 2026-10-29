package io.github.jooho2075.statrecord.game.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ResultCode {
    SINGLE("1B", true, true),
    DOUBLE("2B", true, true),
    TRIPLE("3B", true, true),
    HOME_RUN("HR", true, true),
    WALK("BB", false, false),
    INTENTIONAL_WALK("IBB", false, false),
    HIT_BY_PITCH("HBP", false, false),
    STRIKEOUT("SO", false, true),
    SACRIFICE_BUNT("SH", false, false),
    SACRIFICE_FLY("SF", false, false),
    GROUND_OUT("GO", false, true),
    FLY_OUT("FO", false, true),
    LINE_OUT("LO", false, true),
    REACHED_ON_ERROR("ROE", false, true),
    FIELDERS_CHOICE("FC", false, true);

    private final String code;
    private final boolean hit; // 안타 여부
    private final boolean atBat; // 타수 포함 여부

    ResultCode(String code, boolean hit, boolean atBat) {
        this.code = code;
        this.hit = hit;
        this.atBat = atBat;
    }

    public static ResultCode fromCode(String code) {
        return Arrays.stream(values())
                .filter(r -> r.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 타석 결과 코드 : " + code));
    }
}
