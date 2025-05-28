package org.project.karto.features.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.project.karto.application.dto.auth.LateVerificationForm;
import org.project.karto.util.DBManagementUtils;
import org.project.karto.util.TestDataGenerator;

import static io.restassured.RestAssured.given;

@QuarkusTest
@Disabled("Due to technical reasons")
public class OIDCAuthTest {

    private final DBManagementUtils dbManagementUtils;

    static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    OIDCAuthTest(DBManagementUtils dbManagementUtils) {
        this.dbManagementUtils = dbManagementUtils;
    }

    @Test
    void validOpenIDTest() {
        oidcRegistration();
        removeUser();
    }

    @Test
    void validLateVerification() throws JsonProcessingException {
        oidcRegistration();
        LateVerificationForm lvForm = new LateVerificationForm("alice@keycloak.org", TestDataGenerator.generatePhone().phoneNumber());

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(lvForm))
                .when()
                .patch("/karto/auth/late-verification")
                .then()
                .assertThat()
                .statusCode(jakarta.ws.rs.core.Response.Status.ACCEPTED.getStatusCode());

        removeUser();
    }

    private static void oidcRegistration() {
        final String idToken = idToken();

        given()
                .header("X-ID-TOKEN", idToken)
                .when()
                .post("/karto/auth/oidc")
                .then()
                .assertThat()
                .statusCode(jakarta.ws.rs.core.Response.Status.OK.getStatusCode());
    }

    private void removeUser() {
        dbManagementUtils.removeUser("alice@keycloak.org");
    }

    private static String idToken() {
        final String tokenEndpoint = String
                .format("%s/realms/%s/protocol/openid-connect/token", "http://localhost:7080", "karto-realm");
        final Response response = given()
                .contentType(ContentType.URLENC)
                .formParam("grant_type", "password")
                .formParam("client_id", "karto")
                .formParam("username", "alice")
                .formParam("password", "alice")
                .formParam("client_secret", "secret")
                .formParam("scope", "openid")
                .post(tokenEndpoint);

        if (response.statusCode() != 200)
            throw new IllegalStateException("Failed to get idToken: %s.".formatted(response.getBody().asString()));
        return response.jsonPath().getString("id_token");
    }
}
