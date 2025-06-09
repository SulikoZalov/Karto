package org.project.karto.features.auth

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
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

    @ConfigProperty(name = "admin.verification.key")
    String verificationKey;

    void "successful login"() {
        when:
        def response = given().header("X-VERIFICATION-KEY", verificationKey)
                .when()
                .post("karto/admin/auth/login")
                .then()
                .extract()

        then: "verify response"
        response.statusCode() == 200
    }

    void "failure on invalid verification key"() {
        when:
        def response = given().header("X-VERIFICATION-KEY", "invalid key")
                .when()
                .post("karto/admin/auth/login")
                .then()
                .extract()

        then: "verify response"
        response.statusCode() == 403
    }
}
