package io.github.jooho2075.statrecord.support;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@TestConfiguration
@EnableJpaAuditing
public class TestAuditingConfig {
    private static final AtomicReference<Long> CURRENT_AUDITOR = new AtomicReference<>();

    /** 테스트에서 저장한 회원 id를 작성자로 지정 */
    public static void setCurrentAuditor(Long memberId) {
        CURRENT_AUDITOR.set(memberId);
    }

    @Bean
    public AuditorAware<Long> auditorAware() {
        return () -> Optional.ofNullable(CURRENT_AUDITOR.get());
    }
}
