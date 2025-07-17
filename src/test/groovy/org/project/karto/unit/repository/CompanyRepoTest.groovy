package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException
import org.project.karto.infrastructure.repository.JDBCCompanyRepository
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification
import spock.lang.Unroll

import java.time.temporal.ChronoUnit

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class CompanyRepoTest extends Specification{

    @Inject
    JDBCCompanyRepository repo

    @Unroll("#company.id | #company.companyName.companyName")
    void "successful save"() {
        when:
        def save_result = repo.save(company)

        then:
        save_result.success()
        save_result.value() == 1

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
        firstSaveResult.value() == 1
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
        saveResult.value() == 1

        when: "update company's card limitations"
        company.specifyCardUsageLimitations(card_limits)
        def updateResult = repo.updateCardUsageLimitations(company)

        then: "verify success"
        updateResult.success()
        updateResult.value() == 1

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

    @Unroll("#company.id | old -> #company.cardUsageLimitation().expirationPeriod(), #company.cardUsageLimitation().maxUsageCount() | new -> #card_limits.expirationPeriod(), #card_limits.maxUsageCount()")
    void "fail updating card limits of inactive company"() {
        when: "save company"
        def saveResult = repo.save(company)

        then: "verify success"
        notThrown(Exception)
        saveResult.success()
        saveResult.value() == 1

        when: "update company's card limitations"
        company.specifyCardUsageLimitations(card_limits)

        then: "verify failure"
        thrown(IllegalDomainArgumentException)

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
        saveResult.value() == 1

        when: "update password"
        company.changePassword(password)
        def updateResult = repo.updatePassword(company)

        then: "verify success"
        updateResult.success()
        updateResult.value() == 1

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

    void "update company counter"() {
        given:
        def saveResult = repo.save(company)

        expect:
        saveResult.success()
        saveResult.value() == 1

        when:
        company.incrementCounter()
        def result = repo.updateCounter(company)

        then:
        notThrown(Exception)
        result.success()
        result.value() == 1

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "update company verification"() {
        given:
        def saveResult = repo.save(company)

        expect:
        saveResult.success()
        saveResult.value() == 1

        when:
        company.incrementCounter()
        company.enable()
        def result = repo.updateVerification(company)

        then:
        notThrown(Exception)
        result.success()
        result.value() == 1

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "successful find by ID"() {
        when:
        def result = repo.save(company)

        then:
        notThrown(Exception)
        result.success()
        result.value() == 1

        when:
        def findResult = repo.findBy(company.id())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "fail find by non existent ID"() {
        when:
        def findResult = repo.findBy(company.id())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "successful find by registration number"() {
        when:
        def result = repo.save(company)

        then:
        notThrown(Exception)
        result.success()

        when:
        def findResult = repo.findBy(company.registrationNumber())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "fail find by non existent registration number"() {
        when:
        def findResult = repo.findBy(company.registrationNumber())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "successful find by phone number"() {
        when:
        def result = repo.save(company)

        then:
        notThrown(Exception)
        result.success()

        when:
        def findResult = repo.findBy(company.phone())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "fail find by non existent phone number"() {
        when:
        def findResult = repo.findBy(company.phone())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "successful find by email"() {
        when:
        def result = repo.save(company)

        then:
        notThrown(Exception)
        result.success()

        when:
        def findResult = repo.findBy(company.email())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "fail find by non existent email"() {
        when:
        def findResult = repo.findBy(company.email())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "successful find by company name"() {
        when:
        def result = repo.save(company)

        then:
        notThrown(Exception)
        result.success()

        when:
        def findResult = repo.findBy(company.companyName())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "fail find by non existent company name"() {
        when:
        def findResult = repo.findBy(company.companyName())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "successful is exists by registration number"() {
        when:
        def result = repo.save(company)

        then:
        notThrown(Exception)
        result.success()

        when:
        def isExistResult = repo.isExists(company.registrationNumber())

        then:
        notThrown(Exception)
        isExistResult

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "fail is exists by non existent registration number"() {
        when:
        def isExistResult = repo.isExists(company.registrationNumber())

        then:
        notThrown(Exception)
        !isExistResult

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "successful is exists by phone number"() {
        when:
        def result = repo.save(company)

        then:
        notThrown(Exception)
        result.success()

        when:
        def isExistResult = repo.isExists(company.phone())

        then:
        notThrown(Exception)
        isExistResult

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "fail is exists by non existent phone number"() {
        when:
        def isExistResult = repo.isExists(company.phone())

        then:
        notThrown(Exception)
        !isExistResult

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "successful is exists by email"() {
        when:
        def result = repo.save(company)

        then:
        notThrown(Exception)
        result.success()

        when:
        def isExistResult = repo.isExists(company.email())

        then:
        notThrown(Exception)
        isExistResult

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "fail is exists by non existent email"() {
        when:
        def isExistResult = repo.isExists(company.email())

        then:
        notThrown(Exception)
        !isExistResult

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "successful is exists by company name"() {
        when:
        def result = repo.save(company)

        then:
        notThrown(Exception)
        result.success()

        when:
        def isExistResult = repo.isExists(company.companyName())

        then:
        notThrown(Exception)
        isExistResult

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    void "fail is exists by non existent company name"() {
        when:
        def isExistResult = repo.isExists(company.companyName())

        then:
        notThrown(Exception)
        !isExistResult

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }
}
