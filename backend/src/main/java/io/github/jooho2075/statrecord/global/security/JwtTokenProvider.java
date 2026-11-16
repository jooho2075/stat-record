package io.github.jooho2075.statrecord.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
// 로그인 시 토큰 생성
public class JwtTokenProvider {
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    /** 토큰의 subject에 회원 id를 담아 발급 */
    public String createAccessToken(Long memberId) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(String.valueOf(memberId))
                .issuedAt(now)
                .expiresAt(now.plus(jwtProperties.accessTokenExpiration()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    /** 응답에 알려줄 만료 시간(초)*/
    public long getAccessTokenExpiresIn() {
        return jwtProperties.accessTokenExpiration().toSeconds();
    }
}
