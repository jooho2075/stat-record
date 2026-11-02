package io.github.jooho2075.statrecord.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        // 운영과 같은 pgvector 이미지 사용
        DockerImageName image = DockerImageName.parse("pgvector/pgvector:pg17")
                .asCompatibleSubstituteFor("postgres");
        return new PostgreSQLContainer(image);
    }
}
