package org.project.karto.infrastructure.client;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class UPPaymentProcessor {

    @ConfigProperty(name = "up.login.url")
    String loginURL;

    @ConfigProperty(name = "up.auth.email")
    String registrationEmail;

    @ConfigProperty(name = "up.auth.password")
    String registrationPassword;

    private String authToken;

    private final Vertx vertx;

    private final WebClient webClient;

    UPPaymentProcessor(Vertx vertx) {
        this.vertx = vertx;
        this.webClient = WebClient.create(vertx);
        this.authToken = login()
                .await()
                .indefinitely();
    }

    private Uni<String> login() {
        return webClient.postAbs(loginURL)
                .putHeader("Content-Type", "application/json")
                .sendJsonObject(loginForm())
                .onItem().transform(response -> {
                    if (response.statusCode() == 200) return response.bodyAsString();
                    throw new IllegalStateException("Cannot login into UP.");
                });
    }

    private JsonObject loginForm() {
        return new JsonObject()
                .put("email", registrationEmail)
                .put("password", registrationPassword);
    }
}
