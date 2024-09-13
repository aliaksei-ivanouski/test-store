package org.aivanouski.store.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPooled;

import java.time.Duration;

import static org.aivanouski.store.config.PropertiesConfig.PROPERTIES;

public class RedisConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisConfig.class);

    private JedisPooled jedis;

    private static class RedisConfigHelper {
        private static final RedisConfig INSTANCE = new RedisConfig();
    }

    public static RedisConfig getInstance() {
        return RedisConfigHelper.INSTANCE;
    }

    private RedisConfig() {}

    public void init() {
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(0);
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setMaxWait(Duration.ofSeconds(1));
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(1));
        jedis = new JedisPooled(
                poolConfig,
                PROPERTIES.getProperty("redis.host"),
                Integer.parseInt(PROPERTIES.getProperty("redis.port"))
        );
    }

    public JedisPooled getJedis() {
        return jedis;
    }

    public void close() {
        if (jedis != null) {
            jedis.close();
            log.info("Closed Redis connection pool");
        }
    }
}
