package io.github.jooho2075.statrecord.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    @Bean
    public AuditorAware<Long> auditorAware() {
        // TODO: Spring Security 도입 후 로그인한 회원 id 반환하도록 변경
        return Optional::empty;
    }
}
