package org.project.karto.features.auth

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusMock
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import org.project.karto.application.dto.auth.Token
import org.project.karto.application.service.AdminService
import org.project.karto.infrastructure.security.JWTUtility
import org.project.karto.util.testResources.ApplicationTestResource
import spock.lang.Specification

import static io.restassured.RestAssured.*

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = ApplicationTestResource.class, restrictToAnnotatedClass = true)
class AdminAuthResourceTest extends Specification {

    @Inject
    JWTUtility jwtUtility

    AdminService mockService

    def setup() {
        mockService = Mock(AdminService)
        QuarkusMock.installMockForType(mockService, AdminService.class)
    }

    void "successful login"() {
        given:
        def adminToken = jwtUtility.generateAdministratorToken()

        when:
        def response = given().header("X-VERIFICATION-KEY", "valid key")
                .when()
                .post("karto/admin/auth/login")
                .then()
                .extract()

        then: "verify that AdminService::auth has been invoked once"
        1 * mockService.auth("valid key") >> new Token(adminToken)

        and: "verify response"
        response.statusCode() == 200
        adminToken == response.body().jsonPath().get("token").toString()
    }

    void "failure on invalid verification key"() {
        when:
        def response = given().header("X-VERIFICATION-KEY", "invalid key")
                .when()
                .post("karto/admin/auth/login")
                .then()
                .extract()

        then: "verify that AdminService::auth has been invoked once"
        1 * mockService.auth("invalid key") >> { throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).build()) }

        and: "verify response"
        response.statusCode() == 403
    }
}
