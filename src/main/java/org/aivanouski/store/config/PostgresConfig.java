package org.aivanouski.store.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class PostgresConfig {

    private static final Logger log = LoggerFactory.getLogger(PostgresConfig.class);

    private HikariDataSource dataSource;
    private static final Properties properties = PropertiesConfig.PROPERTIES;

    private static class PostgresConfigHelper {
        private static final PostgresConfig INSTANCE = new PostgresConfig();
    }

    public static PostgresConfig getInstance() {
        return PostgresConfigHelper.INSTANCE;
    }

    private PostgresConfig() {}

    public void init() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(properties.getProperty("db.url"));
        config.setUsername(properties.getProperty("db.username"));
        config.setPassword(properties.getProperty("db.password"));

        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("hikari.pool-size")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("hikari.min-idle")));
        config.setIdleTimeout(Integer.parseInt(properties.getProperty("hikari.idle-timout")));
        config.setMaxLifetime(Integer.parseInt(properties.getProperty("hikari.max-lifetime")));
        config.setConnectionTimeout(Integer.parseInt(properties.getProperty("hikari.connection-timeout")));

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
                        properties.getProperty("db.url"),
                        properties.getProperty("db.username"),
                        properties.getProperty("db.password")
                )
                .load();
        flyway.migrate();
    }
}
