package org.project.karto.features.user;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.RepeatedTest;
import org.project.karto.application.dto.common.ErrorMessage;
import org.project.karto.application.dto.user.UserDTO;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.infrastructure.security.JWTUtility;
import org.project.karto.util.PostgresTestResource;
import org.project.karto.util.TestDataGenerator;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.K;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(PostgresTestResource.class)
public class UserProfileTest {

  private static final String KARTO_USER_PROFILE = "/karto/user/profile";

  @Inject
  UserRepository repo;

  @Inject
  JWTUtility jwtUtility;

  @RepeatedTest(10)
  void successfullFoundUserProfile() {
    User userEnt = TestDataGenerator.generateUser();
    userEnt.incrementCounter();
    userEnt.enable();
    repo.save(userEnt);

    String token = jwtUtility.generateToken(userEnt);

    UserDTO user = given()
        .header("Authorization", "Bearer " + token)
        .when()
        .get(KARTO_USER_PROFILE)
        .then()
        .statusCode(Response.Status.OK.getStatusCode())
        .extract()
        .as(UserDTO.class);

    assertAll("UserDTO fields validation",
        () -> assertNotNull(user, "UserDTO object should not be null"),
        () -> assertNotNull(user.firstname(), "Firstname should not be null"),
        () -> assertFalse(user.firstname().isBlank(), "Firstname should not be blank"),
        () -> assertTrue(user.firstname().length() <= 50, "Firstname should be <= 50 chars"),
        () -> assertNotNull(user.surname(), "Surname should not be null"),
        () -> assertFalse(user.surname().isBlank(), "Surname should not be blank"),
        () -> assertTrue(user.surname().length() <= 50, "Surname should be <= 50 chars"),
        () -> assertNotNull(user.email(), "Email should not be null"),
        () -> assertFalse(user.email().isBlank(), "Email should not be blank"),
        () -> assertTrue(user.email().contains("@"), "Email should contain @"),
        () -> assertTrue(user.email().length() <= 100, "Email should be <= 100 chars"),
        () -> assertNotNull(user.phone(), "Phone should not be null"),
        () -> assertFalse(user.phone().isBlank(), "Phone should not be blank"),
        () -> assertTrue(user.phone().length() <= 20, "Phone should be <= 20 chars"),
        () -> assertNotNull(user.birthDate(), "BirthDate should not be null"),
        () -> assertTrue(user.birthDate().isBefore(LocalDate.now()), "BirthDate should be in past"),
        () -> assertTrue(user.birthDate().isAfter(LocalDate.of(1900, 1, 1)), "BirthDate should be realistic"),
        () -> assertNotNull(user.isVerified(), "isVerified should not be null"),
        () -> assertNotNull(user.is2FAEnabled(), "is2FAEnabled should not be null"),
        () -> assertNotNull(user.storedCashback(), "StoredCashback should not be null"),
        () -> assertTrue(user.storedCashback().compareTo(BigDecimal.ZERO) >= 0, "Cashback should not be negative"),
        () -> assertTrue(user.storedCashback().scale() <= 2, "Cashback should have max 2 decimal places"));
  }

  @RepeatedTest(10)
  void userProfileNotFound() {
    String token = jwtUtility.generateToken(TestDataGenerator.generateUser());

    given()
        .header("Authorization", "Bearer " + token)
        .get(KARTO_USER_PROFILE)
        .then()
        .statusCode(Response.Status.NOT_FOUND.getStatusCode());
  }

  @RepeatedTest(10)
  void bannedUser() {
    User userEnt = TestDataGenerator.generateUser();
    userEnt.ban();
    repo.save(userEnt);

    String token = jwtUtility.generateToken(userEnt);

    ErrorMessage errorMessage = given()
        .header("Authorization", "Bearer " + token)
        .get(KARTO_USER_PROFILE)
        .then()
        .statusCode(Response.Status.FORBIDDEN.getStatusCode())
        .extract()
        .as(ErrorMessage.class);

    assertEquals(errorMessage.errorMessage(),
        "Access denied: this user account has been banned due to a violation of platform rules. Contact support for further assistance.");
  }
}
