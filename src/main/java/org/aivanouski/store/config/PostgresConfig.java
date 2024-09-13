package org.aivanouski.store.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

import static org.aivanouski.store.config.PropertiesConfig.PROPERTIES;

public class PostgresConfig {

    private static final Logger log = LoggerFactory.getLogger(PostgresConfig.class);

    private HikariDataSource dataSource;

    private static class PostgresConfigHelper {
        private static final PostgresConfig INSTANCE = new PostgresConfig();
    }

    public static PostgresConfig getInstance() {
        return PostgresConfigHelper.INSTANCE;
    }

    private PostgresConfig() {}

    public void init() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(PROPERTIES.getProperty("db.url"));
        config.setUsername(PROPERTIES.getProperty("db.username"));
        config.setPassword(PROPERTIES.getProperty("db.password"));

        config.setMaximumPoolSize(Integer.parseInt(PROPERTIES.getProperty("hikari.pool-size")));
        config.setMinimumIdle(Integer.parseInt(PROPERTIES.getProperty("hikari.min-idle")));
        config.setIdleTimeout(Integer.parseInt(PROPERTIES.getProperty("hikari.idle-timout")));
        config.setMaxLifetime(Integer.parseInt(PROPERTIES.getProperty("hikari.max-lifetime")));
        config.setConnectionTimeout(Integer.parseInt(PROPERTIES.getProperty("hikari.connection-timeout")));

        // PostgreSQL specific settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
        log.info("Initialized Postgres config");
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
            log.info("Closed Postgres database");
        }
    }

    public void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(
                        PROPERTIES.getProperty("db.url"),
                        PROPERTIES.getProperty("db.username"),
                        PROPERTIES.getProperty("db.password")
                )
                .load();
        flyway.migrate();
    }
}
