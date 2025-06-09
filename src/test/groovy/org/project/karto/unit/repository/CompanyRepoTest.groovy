package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.infrastructure.repository.JDBCCompanyRepository
import org.project.karto.util.ApplicationTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification
import spock.lang.Unroll

import java.time.temporal.ChronoUnit

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = ApplicationTestResource.class, restrictToAnnotatedClass = true)
class CompanyRepoTest extends Specification{

    @Inject
    JDBCCompanyRepository repo

    @Unroll("#company.id | #company.companyName.companyName")
    void "successful save"() {
        when:
        def save_result = repo.save(company)

        then:
        save_result.success()

        and:
        def result = repo.findBy(company.id())
        result.success()

        and:
        def company_from_repo = result.orElseThrow()
        verifyAll {
            company_from_repo.id() == company.id()
            company_from_repo.registrationNumber() == company.registrationNumber()
            company_from_repo.companyName() == company.companyName()
            company_from_repo.email() == company.email()
            company_from_repo.phone() == company.phone()
            company_from_repo.password() == company.password()
            company_from_repo.creationDate().truncatedTo(ChronoUnit.SECONDS) == company.creationDate().truncatedTo(ChronoUnit.SECONDS)
            company_from_repo.cardUsageLimitation() == company.cardUsageLimitation()
            company_from_repo.companyStatus() == company.companyStatus()
        }

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "fail saving same company 2 times"() {
        when:
        def firstSaveResult = repo.save(company)
        def secondSaveResult = repo.save(company)

        then:
        firstSaveResult.success()
        !secondSaveResult.success()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }


    @Unroll("#company.id | old -> #company.cardUsageLimitation().expirationPeriod(), #company.cardUsageLimitation().maxUsageCount() | new -> #card_limits.expirationPeriod(), #card_limits.maxUsageCount()")
    void "successfully update card limits"() {
        when: "save company"
        company.incrementCounter()
        company.enable()
        def saveResult = repo.save(company)

        then: "verify success"
        notThrown(Exception)
        saveResult.success()

        when: "update company's card limitations"
        company.specifyCardUsageLimitations(card_limits)
        def updateResult = repo.updateCardUsageLimitations(company)

        then: "verify success"
        updateResult.success()

        and: "retrieve updated company"
        def result = repo.findBy(company.id())
        result.success()

        and: "verify changes"
        def limit = result.orElseThrow().cardUsageLimitation()
        limit.expirationDays() == card_limits.expirationDays()
        limit.expirationPeriod() == card_limits.expirationPeriod()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
        card_limits << (1..10).collect({TestDataGenerator.generateCardLimits()})
    }

    @Unroll("#company.id | old -> #company.password().password() | new -> #password.password()")
    void "successfully update company password"() {
        given:
        def oldPassword = company.password().password()

        when: "save company"
        company.incrementCounter()
        company.enable()
        def saveResult = repo.save(company)

        then: "verify success"
        saveResult.success()

        when: "update password"
        company.changePassword(password)
        def updateResult = repo.updatePassword(company)

        then: "verify success"
        updateResult.success()

        and: "retrieve updated company"
        def findResult = repo.findBy(company.id())
        findResult.success()

        and: "verify changes"
        def new_password = findResult.orElseThrow().password()
        new_password.password() != oldPassword

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
        password << (1..10).collect({TestDataGenerator.generatePassword()})
    }
}
