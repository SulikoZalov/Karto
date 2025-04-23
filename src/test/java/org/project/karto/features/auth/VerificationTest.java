package org.project.karto.features.auth;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.project.karto.application.dto.RegistrationForm;
import org.project.karto.domain.user.entities.OTP;
import org.project.karto.infrastructure.security.HOTPGenerator;
import org.project.karto.util.DBManagementUtils;
import org.project.karto.util.TestDataGenerator;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class VerificationTest {

    private final DBManagementUtils dbManagement;

    private final HOTPGenerator hotpGenerator;

    VerificationTest(DBManagementUtils dbManagement) {
        this.dbManagement = dbManagement;
        this.hotpGenerator = new HOTPGenerator();
    }

    @Test
    void validVerification() {
        OTP otp = dbManagement.saveUser(TestDataGenerator.generateRegistrationForm());

        given()
                .queryParam("otp", otp.otp())
                .when()
                .patch("/karto/auth/verification")
                .then()
                .assertThat()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    void verificationWithInvalidOTP() {
        OTP ignore = dbManagement.saveUser(TestDataGenerator.generateRegistrationForm());

        given()
                .queryParam("otp", "invalidotp")
                .when()
                .patch("/karto/auth/verification")
                .then()
                .assertThat()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void verificationWithMismatchOTP() {
        OTP ignore = dbManagement.saveUser(TestDataGenerator.generateRegistrationForm());

        given()
                .queryParam("otp", hotpGenerator.generateHOTP(HOTPGenerator.generateSecretKey(), 0))
                .when()
                .patch("/karto/auth/verification")
                .then()
                .assertThat()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void validVerificationWithResendOTP() {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        OTP ignore = dbManagement.saveUser(form);

        given()
                .queryParam("phoneNumber", form.phone())
                .when()
                .get("/karto/auth/resend-otp")
                .then()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode());

        OTP resendOTP = dbManagement.getUserOTP(form.email());
        given()
                .queryParam("otp", resendOTP.otp())
                .when()
                .patch("/karto/auth/verification")
                .then()
                .assertThat()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    void verificationWhenUseOldOTPAfterResend() {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        OTP old = dbManagement.saveUser(form);

        given()
                .queryParam("phoneNumber", form.phone())
                .when()
                .get("/karto/auth/resend-otp")
                .then()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode());

        given()
                .queryParam("otp", old.otp())
                .when()
                .patch("/karto/auth/verification")
                .then()
                .assertThat()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }
}