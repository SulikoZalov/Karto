package org.project.karto.features.auth;

import com.aayushatharva.brotli4j.common.annotations.Local;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class OIDCAuthTest {

    static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    void validOpenIDTest() {
        final String idToken = idToken("http://localhost:7080", "karto-realm", "karto", "alice", "alice");

        given()
                .header("X-ID-TOKEN", idToken)
                .when()
                .post("/karto/auth/oidc")
                .then()
                .assertThat()
                .statusCode(jakarta.ws.rs.core.Response.Status.OK.getStatusCode());
    }

    private static String idToken(
            final String keycloakUrl,
            final String realm,
            final String clientId,
            final String username,
            final String password) {

        final String tokenEndpoint = String.format("%s/realms/%s/protocol/openid-connect/token", keycloakUrl, realm);
        final Response response = given()
                .contentType(ContentType.URLENC)
                .formParam("grant_type", "password")
                .formParam("client_id", clientId)
                .formParam("username", username)
                .formParam("password", password)
                .formParam("client_secret", "secret")
                .formParam("scope", "openid")
                .post(tokenEndpoint);

        if (response.statusCode() != 200)
            throw new RuntimeException("Failed to get idToken: " + response.getBody().asString());
        return response.jsonPath().getString("id_token");
    }

    public static String formatJson(String json) {
        try {
            return objectMapper.writeValueAsString(objectMapper.readValue(json, Object.class));
        } catch (Exception e) {
            Log.error("Can`t read json. ", e);
            return json;
        }
    }
}
