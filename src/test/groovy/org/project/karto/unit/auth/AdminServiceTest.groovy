package org.project.karto.unit.auth

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.junit.QuarkusMock
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import org.eclipse.microprofile.config.ConfigProvider
import org.project.karto.application.dto.auth.CompanyRegistrationForm
import org.project.karto.application.service.AdminService
import org.project.karto.domain.common.value_objects.Email
import org.project.karto.domain.common.value_objects.Phone
import org.project.karto.domain.companies.value_objects.CompanyName
import org.project.karto.domain.companies.value_objects.RegistrationNumber
import org.project.karto.infrastructure.repository.JDBCCompanyRepository
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

@Dependent
@QuarkusSpockTest
class AdminServiceTest extends Specification {

    @Inject
    AdminService service

    void "successful auth"() {
        given:
        String verificationKey = ConfigProvider.getConfig()
                .getConfigValue("admin.verification.key")
                .getValue()

        when:
        def token = service.auth(verificationKey)

        then:
        notThrown(WebApplicationException)
        token != null
        token.token() != null
        !token.token().isBlank()
    }

    void "auth failure"() {
        given:
        String verificationKey = "invalid key"

        when:
        def token = service.auth(verificationKey)

        then:
        WebApplicationException e = thrown(WebApplicationException)
        e.getResponse().getEntity() == "Invalid administrator verification key."
        token == null
    }

    void "successful partner registration"() {
        given:
        CompanyRegistrationForm form = TestDataGenerator.generateCompanyRegistrationForm()

        when:
        service.registerPartner(form)

        then:
        notThrown(WebApplicationException)
    }

    void "fail registration: form is null"() {
        given:
        CompanyRegistrationForm form = null;

        when:
        service.registerPartner(form)

        then:
        WebApplicationException e = thrown(WebApplicationException)
        e.getResponse().getEntity() == "Company registration form must be filled."
    }

    void "fail registration: password invalid"() {
        given:
        CompanyRegistrationForm form = TestDataGenerator.generateCompanyRegistrationForm()
        form = new CompanyRegistrationForm(
                form.registrationCountryCode(),
                form.registrationNumber(),
                form.companyName(),
                form.email(),
                form.phone(),
                "",
                form.cardExpirationDays(),
                form.cardMaxUsageCount()
        )

        when:
        service.registerPartner(form)

        then:
        thrown(WebApplicationException)
    }

    void "fail registration: company name invalid"() {
        given:
        CompanyRegistrationForm form = TestDataGenerator.generateCompanyRegistrationForm()
        form = new CompanyRegistrationForm(
                form.registrationCountryCode(),
                form.registrationNumber(),
                "",
                form.email(),
                form.phone(),
                form.rawPassword(),
                form.cardExpirationDays(),
                form.cardMaxUsageCount()
        )

        when:
        service.registerPartner(form)

        then:
        thrown(WebApplicationException)
    }

    void "fail registration: company name exists"() {
        given:
        JDBCCompanyRepository mockRepo = Mock(JDBCCompanyRepository)
        QuarkusMock.installMockForType(mockRepo, JDBCCompanyRepository.class)

        CompanyRegistrationForm form = TestDataGenerator.generateCompanyRegistrationForm()

        when:
        service.registerPartner(form)

        then:
        1 * mockRepo.isExists(new CompanyName(form.companyName())) >> true
        WebApplicationException e = thrown(WebApplicationException)
        e.getResponse().getEntity() == "Company name already exists."
    }

    void "fail registration: registration number exists"() {
        given:
        JDBCCompanyRepository mockRepo = Mock(JDBCCompanyRepository)
        QuarkusMock.installMockForType(mockRepo, JDBCCompanyRepository.class)

        CompanyRegistrationForm form = TestDataGenerator.generateCompanyRegistrationForm()

        when:
        service.registerPartner(form)

        then:
        1 * mockRepo.isExists(new CompanyName(form.companyName())) >> false
        1 * mockRepo.isExists(new RegistrationNumber(form.registrationCountryCode(), form.registrationNumber())) >> true
        WebApplicationException e = thrown(WebApplicationException)
        e.getResponse().getEntity() == "Registration number already exists."
    }

    void "fail registration: phone number exists"() {
        given:
        JDBCCompanyRepository mockRepo = Mock(JDBCCompanyRepository)
        QuarkusMock.installMockForType(mockRepo, JDBCCompanyRepository.class)

        CompanyRegistrationForm form = TestDataGenerator.generateCompanyRegistrationForm()

        when:
        service.registerPartner(form)

        then:
        1 * mockRepo.isExists(new CompanyName(form.companyName())) >> false
        1 * mockRepo.isExists(new RegistrationNumber(form.registrationCountryCode(), form.registrationNumber())) >> false
        1 * mockRepo.isExists(new Phone(form.phone())) >> true
        WebApplicationException e = thrown(WebApplicationException)
        e.getResponse().getEntity() == "Phone already exists."
    }

    void "fail registration: email exists"() {
        given:
        JDBCCompanyRepository mockRepo = Mock(JDBCCompanyRepository)
        QuarkusMock.installMockForType(mockRepo, JDBCCompanyRepository.class)

        CompanyRegistrationForm form = TestDataGenerator.generateCompanyRegistrationForm()

        when:
        service.registerPartner(form)

        then:
        1 * mockRepo.isExists(new CompanyName(form.companyName())) >> false
        1 * mockRepo.isExists(new RegistrationNumber(form.registrationCountryCode(), form.registrationNumber())) >> false
        1 * mockRepo.isExists(new Phone(form.phone())) >> false
        1 * mockRepo.isExists(new Email(form.email())) >> true
        WebApplicationException e = thrown(WebApplicationException)
        e.getResponse().getEntity() == "Email already exists."
    }
}
