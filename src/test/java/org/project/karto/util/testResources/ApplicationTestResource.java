package org.project.karto.util.testResources;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.ComposeContainer;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ApplicationTestResource implements QuarkusTestResourceLifecycleManager {

    ComposeContainer compose;

    @Override
    public Map<String, String> start() {
        File composeFile = Path.of("src/test/resources/docker/test-compose.yaml").toFile();
        compose = new ComposeContainer(composeFile)
                .withExposedService("datasource-1", 5432)
                .withExposedService("keycloak-1", 7080);

        compose.start();

        Map<String, String> config = new HashMap<>();

        {
            String dbServiceHost = compose.getServiceHost("datasource-1", 5432);
            int dbServicePort = compose.getServicePort("datasource-1", 5432);
            String jdbcURL = "jdbc:postgresql://%s:%s/karto".formatted(dbServiceHost, dbServicePort);
            config.put("flyway-url", jdbcURL);
        }
        {
            String keycloakServiceHost = compose.getServiceHost("keycloak-1", 7080);
            int keycloakServicePort = compose.getServicePort("keycloak-1", 7080);
            String keycloakURL = "http://%s:%s/realms/karto-realm".formatted(keycloakServiceHost, keycloakServicePort);
            config.put("keycloak-url", keycloakURL);
        }

        return config;
    }

    @Override
    public void stop() {
        if (compose != null) {
            compose.stop();
            compose = null;
        }
    }
}
