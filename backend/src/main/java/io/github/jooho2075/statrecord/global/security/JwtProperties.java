package io.github.jooho2075.statrecord.global.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String issuer, String secret, Duration accessTokenExpiration) {
}