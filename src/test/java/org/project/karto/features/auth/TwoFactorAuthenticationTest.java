package org.project.karto.features.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.project.karto.application.dto.auth.LoginForm;
import org.project.karto.application.dto.auth.RegistrationForm;
import org.project.karto.domain.user.entities.OTP;
import org.project.karto.util.DBManagementUtils;
import org.project.karto.util.TestDataGenerator;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class TwoFactorAuthenticationTest {

    static final String ENABLE_2FA_URL = "/karto/auth/2FA/enable";

    static final String VERIFY_2FA = "/karto/auth/2FA/verify";

    @Inject
    DBManagementUtils dbManagementUtils;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void valid2FAEnable() throws JsonProcessingException {
        start2FA();
    }

    @Test
    void valid2FAVerification() throws JsonProcessingException {
        RegistrationForm form = start2FA();
        OTP otp = dbManagementUtils.getUserOTP(form.email());

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .param("otp", otp.otp())
                .when()
                .patch(VERIFY_2FA)
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    private RegistrationForm start2FA() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagementUtils.saveVerifiedUser(form);

        LoginForm loginForm = new LoginForm(form.phone(), form.password());
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(loginForm))
                .when()
                .post(ENABLE_2FA_URL)
                .then()
                .log().all()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());

        return form;
    }
}
