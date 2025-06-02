package org.project.karto.features.repository

import com.aingrace.test.spock.QuarkusSpockTest
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.common.value_objects.CardUsageLimitations
import org.project.karto.domain.companies.entities.Company
import org.project.karto.domain.companies.value_objects.CompanyName
import org.project.karto.domain.companies.value_objects.RegistrationNumber
import org.project.karto.domain.user.values_objects.Password
import org.project.karto.infrastructure.repository.JDBCCompanyRepository
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

import java.time.Period

@Dependent
@QuarkusSpockTest
class CompanyRepoTest extends Specification{

    @Inject
    JDBCCompanyRepository repo

    def "save company and retrieve"() {
        given:
        def id = UUID.randomUUID()
        def reg_num = new RegistrationNumber("US", "AAAAA-1111")
        def name = new CompanyName("Axiom")
        def email = TestDataGenerator.generateEmail()
        def phone = TestDataGenerator.generatePhone()
        def password = new Password("12345678abcxyz")
        def card_limits = new CardUsageLimitations(Period.ofDays(44), 9)
        Company company = Company.of(id, reg_num, name, email, phone, password, card_limits)

        when:
        repo.save(company)

        then:
        def comp = repo.findBy(id)
        print (comp.orElseThrow())
    }

    def "update company card limits and verify"() {

    }
}
