package io.github.jooho2075.statrecord.global.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

// 요청에 담긴 토큰에서 로그인한 회원 id를 꺼냄
public final class SecurityUtil {

    private SecurityUtil() {

    }

    public static Optional<Long> currentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.of(Long.valueOf(jwtAuth.getToken().getSubject()));
        }
        return Optional.empty();
    }
}
