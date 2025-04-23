package org.project.karto.features.auth.registration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.project.karto.application.dto.RegistrationForm;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.user.values_objects.PersonalData;
import org.project.karto.infrastructure.security.HOTPGenerator;
import org.project.karto.util.TestDataGenerator;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class RegistrationTest {

    private final UserRepository userRepository;

    static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    public RegistrationTest(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        userRepository.save(User.of(
                new PersonalData(form.firstname(), form.surname(), form.phone(), "encoded", form.email(), form.birthDate()),
                HOTPGenerator.generateSecretKey()
        ));

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
        userRepository.save(User.of(
                new PersonalData(form.firstname(), form.surname(), form.phone(), "encoded", "other@email.com", form.birthDate()),
                HOTPGenerator.generateSecretKey()
        ));

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
