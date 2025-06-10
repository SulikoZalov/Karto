package org.project.karto.features.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.project.karto.application.dto.auth.RegistrationForm;

import org.project.karto.util.DBManagementUtils;
import org.project.karto.util.PostgresTestResource;
import org.project.karto.util.TestDataGenerator;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class RegistrationTest {

    private final DBManagementUtils dbManagement;

    static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    RegistrationTest(DBManagementUtils dbManagementUtils) {
        this.dbManagement = dbManagementUtils;
    }

    @Test
    void validRegistration() throws JsonProcessingException {
        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(TestDataGenerator.generateRegistrationForm()))
                .when()
                .post("/karto/auth/registration")
                .then()
                .assertThat()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    void registrationFailsWhenFormIsNull() {
        given()
                .contentType(ContentType.JSON)
                .body("")
                .when()
                .post("/karto/auth/registration")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void registrationFailsWhenPasswordsDoNotMatch() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        form = new RegistrationForm(
                form.firstname(),
                form.surname(),
                form.phone(),
                form.password(),
                form.password() + "diff",
                form.email(),
                form.birthDate()
        );

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(form))
                .when()
                .post("/karto/auth/registration")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void registrationFailsWhenEmailAlreadyUsed() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagement.saveUser(form);

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(form))
                .when()
                .post("/karto/auth/registration")
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    void registrationFailsWhenPhoneAlreadyUsed() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagement.saveUser(form);

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(form))
                .when()
                .post("/karto/auth/registration")
                .then()
                .statusCode(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    void registrationFailsWhenPasswordIsWeak() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        form = new RegistrationForm(
                form.firstname(),
                form.surname(),
                form.phone(),
                "123",
                "123",
                form.email(),
                form.birthDate()
        );

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(form))
                .when()
                .post("/karto/auth/registration")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void registrationFailsWhenEmailIsInvalid() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        form = new RegistrationForm(
                form.firstname(),
                form.surname(),
                form.phone(),
                form.password(),
                form.passwordConfirmation(),
                "invalid-email",
                form.birthDate()
        );

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(form))
                .when()
                .post("/karto/auth/registration")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void registrationFailsWhenBirthDateIsInFuture() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        form = new RegistrationForm(
                form.firstname(),
                form.surname(),
                form.phone(),
                form.password(),
                form.passwordConfirmation(),
                form.email(),
                LocalDate.now().plusYears(1)
        );

        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(form))
                .when()
                .post("/karto/auth/registration")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

}
