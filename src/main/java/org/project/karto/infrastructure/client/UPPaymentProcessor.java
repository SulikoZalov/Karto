package org.project.karto.infrastructure.client;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;

import java.net.URI;
import java.util.Base64;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.project.karto.application.dto.gift_card.TransactionDTO;
import org.project.karto.domain.common.containers.Result;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
public class UPPaymentProcessor {

    @ConfigProperty(name = "up.login.url")
    String loginURL;

    @ConfigProperty(name = "up.auth.email")
    String registrationEmail;

    @ConfigProperty(name = "up.auth.password")
    String registrationPassword;

    @ConfigProperty(name = "up.checkout")
    String checkoutURL;

    private String authToken;

    private final WebClient webClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    UPPaymentProcessor(Vertx vertx) {
        this.webClient = WebClient.create(vertx);
        this.authToken = login()
                .await()
                .indefinitely();
    }

    public Result<URI, Throwable> initiatePayment(TransactionDTO dto) {
        try {
            var response = webClient.postAbs(checkoutURL)
                    .putHeader("Content-Type", "application/json")
                    .putHeader("Authorization", "Bearer " + authToken)
                    .sendBuffer(Buffer.buffer(encode64(dto)))
                    .await()
                    .indefinitely();

            if (response.statusCode() != 200)
                return Result.failure(new IllegalStateException("Can`t initialize transaction via payment processor."));

            URI paymentURI = URI.create(response.bodyAsJsonObject().getString("url"));
            return Result.success(paymentURI);
        } catch (Throwable t) {
            return Result.failure(t);
        }
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

    private String encode64(TransactionDTO dto) throws JsonProcessingException {
        return Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(dto).getBytes());
    }

    private JsonObject loginForm() {
        return new JsonObject()
                .put("email", registrationEmail)
                .put("password", registrationPassword);
    }
}
