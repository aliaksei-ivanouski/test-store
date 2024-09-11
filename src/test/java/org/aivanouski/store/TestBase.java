package org.aivanouski.store;

import org.aivanouski.store.config.GsonConfig;
import org.aivanouski.store.config.PostgresConfig;
import org.aivanouski.store.config.PropertiesConfig;
import org.aivanouski.store.config.RedisConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Properties;

public class TestBase {

    private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    protected static GenericContainer redis = new GenericContainer("redis:latest")
            .withExposedPorts(6379)
            .withReuse(true);

    private static final Properties properties = PropertiesConfig.PROPERTIES;

    @BeforeAll
    protected static void beforeAll() {
        PropertiesConfig.getInstance().init();
        GsonConfig.getInstance().init();

        postgres.start();
        properties.setProperty("db.url", postgres.getJdbcUrl());
        properties.setProperty("db.username", postgres.getUsername());
        properties.setProperty("db.password", postgres.getPassword());
        PostgresConfig.getInstance().init();

        redis.start();
        properties.setProperty("redis.host", redis.getHost());
        properties.setProperty("redis.port", String.valueOf(redis.getMappedPort(6379)));
        RedisConfig.getInstance().init();
    }

    @AfterAll
    protected static void afterAll() {
        postgres.stop();
        redis.stop();
    }
}
