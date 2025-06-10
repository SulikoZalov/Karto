package org.project.karto.util;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.GenericContainer;

import java.util.Map;

public class KeycloakTestResource implements QuarkusTestResourceLifecycleManager {

    GenericContainer<?> keycloakContainer;

    @Override
    public Map<String, String> start() {
        keycloakContainer = new GenericContainer<>("quay.io/keycloak/keycloak:26.2")
                .withExposedPorts(7080)
                .withEnv("REDHAT_FIPS", "false");

        keycloakContainer.start();

        String host = keycloakContainer.getHost();
        int port = keycloakContainer.getMappedPort(7080);
        String keycloakURL = "http://%s:%s/auth/realms/karto-realm".formatted(host, port);
        return Map.of("keycloak-url", keycloakURL);
    }

    @Override
    public void stop() {
        if (keycloakContainer != null) {
            keycloakContainer.stop();
            keycloakContainer = null;
        }
    }
}
