package org.project.karto.features.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.project.karto.application.dto.auth.RegistrationForm;
import org.project.karto.domain.companies.entities.Company;
import org.project.karto.domain.companies.repository.CompanyRepository;
import org.project.karto.infrastructure.security.JWTUtility;
import org.project.karto.util.DBManagementUtils;
import org.project.karto.util.PostgresTestResource;
import org.project.karto.util.TestDataGenerator;

import java.io.IOException;
import java.io.InputStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class AdminResourceTest {

    @Inject
    JWTUtility jwtUtility;

    @Inject
    CompanyRepository repo;

    @Inject
    ObjectMapper mapper;

    @Inject
    DBManagementUtils dbManagementUtils;

    static final String ADD_IMAGE = "karto/admin/patner/cards/picture/put";

    static final String GET_IMAGE = "karto/admin/partner/cards/picture";

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

    @Test
    void addPicture_shouldReturnAcceptedWhenInputValid() throws IOException {
        Company company = generateEnableAndSaveCompany();
        String adminToken = jwtUtility.generateAdministratorToken();
        byte[] imageData = getImageBytes();

        given()
                .contentType(ContentType.BINARY)
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("companyName", company.companyName().companyName())
                .body(imageData)
                .when()
                .patch(ADD_IMAGE)
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    void addPicture_shouldReturnBadRequestWhenInputStreamNull() {
        Company company = generateEnableAndSaveCompany();
        String adminToken = jwtUtility.generateAdministratorToken();

        given()
                .contentType(ContentType.BINARY)
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("companyName", company.companyName().companyName())
                .when()
                .patch(ADD_IMAGE)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void loadPicture_shouldReturnOkWithPictureData() throws IOException {
        Company company = generateEnableAndSaveCompany();
        String adminToken = jwtUtility.generateAdministratorToken();
        String companyName = company.companyName().companyName();

        byte[] imageData = getImageBytes();
        given()
                .contentType(ContentType.BINARY)
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("companyName", companyName)
                .body(imageData)
                .patch(ADD_IMAGE)
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("companyName", companyName)
                .when()
                .get(GET_IMAGE)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    @Test
    void loadPicture_shouldReturnNotFoundWhenNoPictureFound() {
        String adminToken = jwtUtility.generateAdministratorToken();
        String companyName = "Non-existent Company";

        given()
                .header("Authorization", "Bearer " + adminToken)
                .queryParam("companyName", companyName)
                .when()
                .get(GET_IMAGE)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    private static byte @NotNull [] getImageBytes() throws IOException {
        try (InputStream is = AdminResourceTest.class
                .getClassLoader()
                .getResourceAsStream("democracy.png")) {

            if (is == null) throw new IllegalStateException("Resource not found");
            return is.readAllBytes();
        }
    }

    private Company generateEnableAndSaveCompany() {
        Company company = TestDataGenerator.generateCompany();
        company.incrementCounter();
        company.enable();
        repo.save(company);
        return company;
    }
}
