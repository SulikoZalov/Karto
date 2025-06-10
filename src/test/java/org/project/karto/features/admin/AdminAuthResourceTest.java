package org.project.karto.features.admin;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;
import org.project.karto.util.PostgresTestResource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class AdminAuthResourceTest {

    @ConfigProperty(name = "admin.verification.key")
    String verificationKey;

    @Test
    void successfulLogin() {
        var response = given()
                .header("X-VERIFICATION-KEY", verificationKey)
                .when()
                .post("karto/admin/auth/login")
                .then()
                .extract()
                .response();

        assertThat(response.statusCode(), equalTo(200));
    }

    @Test
    void failureOnInvalidVerificationKey() {
        var response = given()
                .header("X-VERIFICATION-KEY", "invalid key")
                .when()
                .post("karto/admin/auth/login")
                .then()
                .extract()
                .response();

        assertThat(response.statusCode(), equalTo(403));
    }
}