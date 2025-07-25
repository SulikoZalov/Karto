package org.project.karto.infrastructure.client;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.project.karto.application.dto.gift_card.TransactionDTO;
import org.project.karto.domain.common.containers.Result;

@ApplicationScoped
public class UPPaymentProcessor {

    @ConfigProperty(name = "up.login.url")
    String loginURL;

    @ConfigProperty(name = "up.auth.email")
    String registrationEmail;

    @ConfigProperty(name = "up.auth.password")
    String registrationPassword;

    private String authToken;

    private final WebClient webClient;

    UPPaymentProcessor(Vertx vertx) {
        this.webClient = WebClient.create(vertx);
        this.authToken = login()
                .await()
                .indefinitely();
    }

    public Result<URI, Throwable> manualTransaction(TransactionDTO dto) {
        return null;
    }

    public Result<URI, Throwable> transaction() {
        return null; // TODO
    }

    private Uni<String> login() {
        return webClient.postAbs(loginURL)
                .putHeader("Content-Type", "application/json")
                .sendJsonObject(loginForm())
                .onItem().transform(response -> {
                    if (response.statusCode() == 200)
                        return response.bodyAsString();
                    throw new IllegalStateException("Cannot login into UP.");
                });
    }

    private JsonObject loginForm() {
        return new JsonObject()
                .put("email", registrationEmail)
                .put("password", registrationPassword);
    }
}
