package org.project.karto.features.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.project.karto.infrastructure.security.JWTUtility;
import org.project.karto.util.PostgresTestResource;
import org.project.karto.util.TestDataGenerator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class AdminResourceTest {

    @Inject
    JWTUtility jwtUtility;

    @Inject
    ObjectMapper mapper;

    @Test
    void successfulPartnerRegistration() throws Exception {
        String adminToken = jwtUtility.generateAdministratorToken();
        var companyRegistrationForm = TestDataGenerator.generateCompanyRegistrationForm();
        String formJson = mapper.writeValueAsString(companyRegistrationForm);

        var response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(formJson)
                .when()
                .post("karto/admin/register/partner")
                .then()
                .extract()
                .response();

        assertThat(response.statusCode(), equalTo(200));
    }
}