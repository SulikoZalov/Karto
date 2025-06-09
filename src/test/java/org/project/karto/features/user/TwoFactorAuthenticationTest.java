package org.project.karto.features.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.assertj.core.description.TextDescription;
import org.junit.jupiter.api.Test;
import org.project.karto.application.dto.auth.LoginForm;
import org.project.karto.application.dto.auth.RegistrationForm;
import org.project.karto.domain.user.entities.OTP;
import org.project.karto.util.ApplicationTestResource;
import org.project.karto.util.DBManagementUtils;
import org.project.karto.util.TestDataGenerator;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(value = ApplicationTestResource.class, restrictToAnnotatedClass = true)
class TwoFactorAuthenticationTest {

    static final String ENABLE_2FA_URL = "/karto/auth/2FA/enable";

    static final String VERIFY_2FA = "/karto/auth/2FA/verify";

    static final String RESEND_OTP = "/karto/auth/resend-otp";

    @Inject
    DBManagementUtils dbManagementUtils;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void valid2FAEnable() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagementUtils.saveAndVerifyUser(form);

        LoginForm loginForm = new LoginForm(form.phone(), form.password());
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginForm))
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    void valid2FAVerification() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagementUtils.saveAndVerifyUser(form);

        LoginForm loginForm = new LoginForm(form.phone(), form.password());
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginForm))
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());

        OTP otp = dbManagementUtils.getUserOTP(form.email());

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .param("otp", otp.otp())
                .when()
                .patch(VERIFY_2FA)
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    void _2FA_unverified_account() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagementUtils.saveUser(form);
        LoginForm loginForm = new LoginForm(form.phone(), form.password());

        var response = given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginForm))
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .extract();

        Assertions.assertThat(response.statusCode())
                .describedAs(new TextDescription("Body: %s".formatted(response.body().asPrettyString())))
                .isEqualTo(Response.Status.FORBIDDEN.getStatusCode());
    }

    @Test
    void _2FA_enable_no_login() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body("")
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void _2FA_enable_no_user() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        LoginForm loginForm = new LoginForm(form.phone(), form.password());

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginForm))
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void _2FA_enable_invalid_phone() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagementUtils.saveAndVerifyUser(form);

        LoginForm loginForm = new LoginForm(form.phone() + "()_+", form.password());
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginForm))
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void _2FA_enable_non_existing_phone() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagementUtils.saveAndVerifyUser(form);

        String unregisteredPhone = TestDataGenerator.generateRegistrationForm().phone();
        LoginForm loginForm = new LoginForm(unregisteredPhone, form.password());
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginForm))
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void _2FA_enable_invalid_password() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagementUtils.saveAndVerifyUser(form);

        LoginForm loginForm = new LoginForm(form.phone(), "1");
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginForm))
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void _2FA_enable_duplicate_request() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagementUtils.saveAndVerifyUser(form);

        LoginForm loginForm = new LoginForm(form.phone(), form.password());
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginForm))
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginForm))
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void _2FA_valid_OTP_resend() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagementUtils.saveAndVerifyUser(form);

        LoginForm loginForm = new LoginForm(form.phone(), form.password());
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginForm))
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());

        given()
                .param("phoneNumber", form.phone())
                .get(RESEND_OTP)
                .then()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode());

        OTP otp = dbManagementUtils.getUserOTP(form.email());

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .param("otp", otp.otp())
                .when()
                .patch(VERIFY_2FA)
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }
}
