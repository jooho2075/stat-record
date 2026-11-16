package io.github.jooho2075.statrecord.global.security;

// Redis에 저장하고 꺼내는 일만 담당.
// 만약 저장소를 바꾸더라도 해당 클래스만 수정하면 됨

import io.github.jooho2075.statrecord.global.exception.BusinessException;
import io.github.jooho2075.statrecord.global.exception.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh_token:";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    /** 새 Refresh Token 발급: 원문은 사용자에게, 해시는 Redis에 저장 */
    public String issue(Long memberId) {
        String token = generateToken();
        redisTemplate.opsForValue().set(
                key(token), String.valueOf(memberId), jwtProperties.refreshTokenExpiration());
        return token;
    }

    /** 토큰을 꺼내면서 동시에 삭제 (한 번 쓴 토큰은 다시 사용 불가) */
    public Long consume(String token) {
        String memberId = redisTemplate.opsForValue().getAndDelete(key(token));
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return Long.valueOf(memberId);
    }

    /** 로그아웃: 토큰 삭제 */
    public void revoke(String token) {
        redisTemplate.delete(key(token));
    }

    public long getExpiresIn() {
        return jwtProperties.refreshTokenExpiration().toSeconds();
    }

    private String key(String token) {
        return KEY_PREFIX + sha256(token);
    }

    /** 추측할 수 없는 32바이트 무작위 값을 URL에 안전한 문자열로 변환 */
    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", e);
        }
    }
}