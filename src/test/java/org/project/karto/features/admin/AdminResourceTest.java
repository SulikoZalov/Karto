package org.project.karto.features.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.project.karto.application.dto.auth.RegistrationForm;
import org.project.karto.infrastructure.security.JWTUtility;
import org.project.karto.util.DBManagementUtils;
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

    @Inject
    DBManagementUtils dbManagementUtils;

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

    @Test
    void successfulUserBan() throws JsonProcessingException {
        RegistrationForm form = TestDataGenerator.generateRegistrationForm();
        dbManagementUtils.saveUser(form);

        String adminToken = jwtUtility.generateAdministratorToken();

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .param("phone", form.phone())
                .when()
                .patch("karto/admin/ban/user")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }
}