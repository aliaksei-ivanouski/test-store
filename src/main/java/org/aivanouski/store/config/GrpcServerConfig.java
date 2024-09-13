package org.aivanouski.store.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.aivanouski.store.portal.DisciplePortalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.aivanouski.store.config.PropertiesConfig.PROPERTIES;

public class GrpcServerConfig {

    private static final Logger log = LoggerFactory.getLogger(GrpcServerConfig.class);

    private Server server;

    private static class GrpcServerConfigHelper {
        private static final GrpcServerConfig INSTANCE = new GrpcServerConfig();
    }

    public static GrpcServerConfig getInstance() {
        return GrpcServerConfigHelper.INSTANCE;
    }

    private GrpcServerConfig() {}

    public void init() throws IOException, InterruptedException {
        server = ServerBuilder
                .forPort(Integer.parseInt(PROPERTIES.getProperty("server.port")))
                .addService(new DisciplePortalService()).build();

        server.start();
        log.info("Server started, listening on {}", server.getPort());
        server.awaitTermination();
    }

    public void shutdown() {
        log.info("Shutting down server");
        server.shutdown();
    }
}
