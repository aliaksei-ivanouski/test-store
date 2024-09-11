package org.aivanouski.store.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesConfig {

    private static final Logger log = LoggerFactory.getLogger(PropertiesConfig.class);

    public static final Properties PROPERTIES = new Properties();

    private static class PropertiesConfigHelper {
        private static final PropertiesConfig INSTANCE = new PropertiesConfig();
    }

    public static PropertiesConfig getInstance() {
        return PropertiesConfigHelper.INSTANCE;
    }

    private PropertiesConfig() {}

    public void init() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            // load a properties file
            PROPERTIES.load(input);
            // take from docker env
            System.getenv().forEach((key, value) -> {
                if (PROPERTIES.getProperty(key) != null) {
                    PROPERTIES.setProperty(key, value);
                }
            });
            log.info("Loaded application properties");
        } catch (IOException e) {
            log.error("Could not load application.properties", e);
        }
    }
}
