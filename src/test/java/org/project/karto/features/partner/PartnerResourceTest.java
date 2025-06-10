package org.project.karto.features.partner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.project.karto.application.dto.auth.CompanyRegistrationForm;
import org.project.karto.application.dto.auth.LoginForm;
import org.project.karto.application.dto.auth.Token;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.entities.Company;
import org.project.karto.domain.companies.entities.PartnerVerificationOTP;
import org.project.karto.domain.companies.repository.CompanyRepository;
import org.project.karto.infrastructure.security.JWTUtility;
import org.project.karto.util.DBManagementUtils;
import org.project.karto.util.PostgresTestResource;
import org.project.karto.util.TestDataGenerator;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class PartnerResourceTest {

    @Inject
    JWTUtility jwtUtility;

    @Inject
    DBManagementUtils dbManagementUtils;

    @Inject
    CompanyRepository companyRepository;

    ObjectMapper mapper = new ObjectMapper();

    static final String OTP_RESEND = "/karto/partner/otp/resend";

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

    @Test
    void verification() throws JsonProcessingException {
        CompanyRegistrationForm form = saveCompany();
        PartnerVerificationOTP otp = dbManagementUtils.getCompanyOTP(form);

        given()
                .queryParam("otp", otp.otp())
                .when()
                .patch("/karto/partner/verification")
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());

        Company company = companyRepository.findBy(new Phone(form.phone())).orElseThrow();
        Assertions.assertTrue(company.isActive());
    }

    @Test
    void changeCardLimitations() throws JsonProcessingException {
        String token = login();

        given()
                .queryParam("expiration", 91)
                .queryParam("maxUsageCount", 10)
                .header("Authorization", "Bearer " + token)
                .when()
                .patch("/karto/partner/card/limitations")
                .then()
                .log().all()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
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

    private void verifyCompany(PartnerVerificationOTP otp) {
        given()
                .queryParam("otp", otp.otp())
                .when()
                .patch("/karto/partner/verification")
                .then()
                .statusCode(Response.Status.ACCEPTED.getStatusCode());
    }

    @Test
    void loginCompany() throws JsonProcessingException {
        login();
    }

    private String login() throws JsonProcessingException {
        CompanyRegistrationForm form = saveCompany();
        PartnerVerificationOTP otp = dbManagementUtils.getCompanyOTP(form);
        verifyCompany(otp);

        LoginForm loginForm = new LoginForm(form.phone(), form.rawPassword());
        return given()
                .contentType(ContentType.JSON)
                .body(mapper.writeValueAsString(loginForm))
                .when()
                .post("/karto/partner/login")
                .then()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body()
                .as(Token.class).token();
    }
}
