package org.aivanouski.store;

import org.aivanouski.store.config.GrpcServerConfig;
import org.aivanouski.store.config.GsonConfig;
import org.aivanouski.store.config.PostgresConfig;
import org.aivanouski.store.config.PropertiesConfig;
import org.aivanouski.store.config.RedisConfig;
import org.aivanouski.store.order.OrderDeliveryJob;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        PropertiesConfig.getInstance().init();
        PostgresConfig.getInstance().init();
        PostgresConfig.getInstance().migrate();
        RedisConfig.getInstance().init();
        GsonConfig.getInstance().init();
        OrderDeliveryJob.getInstance().init();

        GrpcServerConfig.getInstance().init();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            PostgresConfig.getInstance().close();
            RedisConfig.getInstance().close();
            GrpcServerConfig.getInstance().shutdown();
        }));
    }
}