package org.project.karto.features.partner;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.project.karto.application.dto.auth.CompanyRegistrationForm;
import org.project.karto.infrastructure.security.JWTUtility;
import org.project.karto.util.ApplicationTestResource;
import org.project.karto.util.TestDataGenerator;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(value = ApplicationTestResource.class, restrictToAnnotatedClass = true)
class PartnerResourceTest {

    @Inject
    JWTUtility jwtUtility;

    ObjectMapper mapper = new ObjectMapper();

    static final String OTP_RESEND = "/karto/partner/otp/resend";

    static final String VERIFICATION = "/karto/partner/verification";

    @Test
    void resendOTP() throws JsonProcessingException {
        CompanyRegistrationForm form = saveCompany();

        given()
                .param("phoneNumber", form.phone())
                .when()
                .get(OTP_RESEND)
                .then()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode());
    }

    private CompanyRegistrationForm saveCompany() throws JsonProcessingException {
        String adminToken = jwtUtility.generateAdministratorToken();
        CompanyRegistrationForm companyRegistrationForm = TestDataGenerator.generateCompanyRegistrationForm();
        String formJson = mapper.writeValueAsString(companyRegistrationForm);

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(formJson)
                .when()
                .post("karto/admin/register/partner")
                .then()
                .extract();

        return companyRegistrationForm;
    }
}
