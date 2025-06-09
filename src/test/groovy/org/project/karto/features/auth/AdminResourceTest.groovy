package org.project.karto.features.auth

import com.aingrace.test.spock.QuarkusSpockTest
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusMock
import io.restassured.http.ContentType
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.application.service.AdminService
import org.project.karto.infrastructure.security.JWTUtility
import org.project.karto.util.TestDataGenerator
import org.project.karto.util.testResources.ApplicationTestResource
import spock.lang.Ignore
import spock.lang.Specification

import static io.restassured.RestAssured.*

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = ApplicationTestResource.class, restrictToAnnotatedClass = true)
class AdminResourceTest extends Specification{

    @Inject
    JWTUtility jwtUtility

    @Inject
    ObjectMapper mapper

    AdminService adminService

    void "successful partner registration"() {
        given:
        def adminToken = jwtUtility.generateAdministratorToken()
        def companyRegistrationForm = TestDataGenerator.generateCompanyRegistrationForm()
        def formJson = mapper.writeValueAsString(companyRegistrationForm)

        when:
        def response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(formJson)
                .when()
                .post("karto/admin/register/partner")
                .then()
                .extract()

        then: "verify that AdminService::registerPartner has been invoked once"
        1 * adminService.registerPartner(_)

        and: "verify response"
        response.statusCode() == 200
    }

    @Ignore("unfinished")
    void "failing partner registration"() {
        given:
        def adminToken = jwtUtility.generateAdministratorToken()
        def companyRegistrationForm = TestDataGenerator.generateCompanyRegistrationForm()
        def formJson = mapper.writeValueAsString(companyRegistrationForm)

        when:
        def response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + adminToken)
                .body(formJson)
                .when()
                .post("karto/admin/register/partner")
                .then()
                .extract()

        then: "verify that AdminService::registerPartner has been invoked once"
        1 * adminService.registerPartner(_)
    }
}
