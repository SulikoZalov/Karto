package org.project.karto.features.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.project.karto.application.dto.auth.LoginForm;
import org.project.karto.application.dto.auth.RegistrationForm;
import org.project.karto.application.dto.auth.Token;
import org.project.karto.application.dto.auth.Tokens;
import org.project.karto.util.DBManagementUtils;
import org.project.karto.util.TestDataGenerator;
import org.project.karto.util.testResources.ApplicationTestResource;

import static io.restassured.RestAssured.given;
import static io.smallrye.common.constraint.Assert.assertFalse;
import static io.smallrye.common.constraint.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.project.karto.features.auth.RegistrationTest.objectMapper;

@QuarkusTest
@QuarkusTestResource(value = ApplicationTestResource.class, restrictToAnnotatedClass = true)
public class LoginTest {

    private final JWTParser jwtParser;

    private final DBManagementUtils dbManagement;

    LoginTest(JWTParser jwtParser, DBManagementUtils dbManagement) {
        this.jwtParser = jwtParser;
        this.dbManagement = dbManagement;
    }

    @Test
    void validLogin() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagement.saveAndVerifyUser(form);

        Tokens tokens = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(new LoginForm(form.phone(), form.password())))
                .when()
                .post("/karto/auth/login")
                .then()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .as(Tokens.class);

        assertNotNull(tokens);
        assertNotNull(tokens.token());
        assertFalse(tokens.token().isBlank());
        assertDoesNotThrow(() -> jwtParser.parse(tokens.token()));
        assertNotNull(tokens.refreshToken());
        assertFalse(tokens.refreshToken().isBlank());
        assertDoesNotThrow(() -> jwtParser.parse(tokens.refreshToken()));
    }

    @Test
    void loginMismatchPassword() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagement.saveAndVerifyUser(form);

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(new LoginForm(form.phone(), "mismatch password")))
                .when()
                .post("/karto/auth/login")
                .then()
                .assertThat()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void refreshToken() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagement.saveAndVerifyUser(form);

        Tokens tokens = given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(new LoginForm(form.phone(), form.password())))
                .when()
                .post("/karto/auth/login")
                .then()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .as(Tokens.class);

        Token token = given()
                .header("Refresh-Token", tokens.refreshToken())
                .when()
                .patch("/karto/auth/refresh-token")
                .then()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode())
                .contentType(ContentType.JSON)
                .extract()
                .as(Token.class);

        assertNotNull(token);
        assertNotNull(token.token());
        assertFalse(token.token().isEmpty());
        assertDoesNotThrow(() -> jwtParser.parse(token.token()));
    }
}
