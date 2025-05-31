package org.project.karto.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hadzhy.jetquerious.jdbc.JetQuerious;
import io.restassured.http.ContentType;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.Response;
import org.project.karto.application.dto.auth.RegistrationForm;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.user.entities.OTP;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.OTPRepository;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.user.values_objects.Email;
import org.project.karto.domain.user.values_objects.Phone;

import java.util.Objects;

import static com.hadzhy.jetquerious.sql.QueryForge.*;
import static io.restassured.RestAssured.given;

@Singleton
public class DBManagementUtils {

    private final OTPRepository otpRepository;

    private final UserRepository userRepository;

    private final JetQuerious jetQuerious = JetQuerious.instance();

    private static final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .build();

    public static final String DELETE_USER = batchOf(
            delete()
                    .from("otp")
                    .where("user_id = (SELECT id FROM user_account WHERE email = ?)")
                    .build()
                    .toSQlQuery(),
            delete()
                    .from("refresh_token")
                    .where("user_id = (SELECT id FROM user_account WHERE email = ?)")
                    .build()
                    .toSQlQuery(),
            delete()
                    .from("user_account")
                    .where("email = ?")
                    .build()
                    .toSQlQuery());

    DBManagementUtils(
            UserRepository userRepository,
            OTPRepository otpRepository) {

        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
    }

    public OTP saveUser(RegistrationForm form) throws JsonProcessingException {
        given()
                .contentType(ContentType.JSON)
                .body(objectMapper.writeValueAsString(form))
                .when()
                .post("/karto/auth/registration")
                .then()
                .assertThat()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());

        Result<User, Throwable> user = userRepository.findBy(new Phone(form.phone()));
        return otpRepository.findBy(Objects.requireNonNull(user.orElseThrow()).id()).orElseThrow();
    }

    public OTP getUserOTP(String email) {
        User user = Objects.requireNonNull(userRepository.findBy(new Email(email)).orElseThrow());
        return otpRepository.findBy(user.id()).orElseThrow();
    }

    public void saveAndVerifyUser(RegistrationForm form) throws JsonProcessingException {
        OTP otp = saveUser(form);

        given()
                .queryParam("otp", otp.otp())
                .when()
                .patch("/karto/auth/verification")
                .then()
                .assertThat()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    public void removeUser(String email) {
        jetQuerious.write(DELETE_USER, email, email, email);
    }
}
