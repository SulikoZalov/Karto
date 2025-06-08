package org.project.karto.features.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.infrastructure.repository.JDBCCompanyRepository
import org.project.karto.util.TestDataGenerator
import org.project.karto.util.testResources.ApplicationTestResource
import spock.lang.Ignore
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
        repo.save(company)

        then:
        def result = repo.findBy(company.id())

        and:
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
            company_from_repo.cardUsageLimitation() == company_from_repo.cardUsageLimitation()
        }

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }

    @Ignore("can't easily verify failure")
    void "fail saving same company 2 times"() {
        when:
        repo.save(company)
        repo.save(company)

        then:
        def result = repo.findBy(company.id())

        and:
        !result.success()

        and:
        print result.throwable()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }


    @Unroll("#company.id | old -> #company.cardUsagesLimitation().expirationPeriod(), #company.cardUsagesLimitation().maxUsageCount() | new -> #card_limits.expirationPeriod(), #card_limits.maxUsageCount()")
    void "successfully update card limits"() {
        when: "save company"
        repo.save(company)

        then: "successfully retrieve it"
        repo.findBy(company.id()).success()

        when: "update company's card limitations"
        company.specifyCardUsageLimitations(card_limits)
        company.cardUsageLimitation()
        repo.update(company)

        then: "retrieve updated company"
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
    @Ignore("SQL typo | can't verify failures")
    void "successfully update company password"() {
        when: "save company"
        println "saving company with password " << company.password().password()
        repo.save(company)

        then: "successfully retrieve it"
        repo.findBy(company.id()).success()

        when: "update password"
        company.changePassword(password)
        repo.updatePassword(company)

        then: "retrieve updated company"
        def result = repo.findBy(company.id())
        result.success()

        and: "verify changes"
        def new_password = result.orElseThrow().password()
        println "new passwd is " << new_password
        new_password.password() != company.password().password()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
        password << (1..10).collect({TestDataGenerator.generatePassword()})
    }


    @Ignore("SQL typo | can't verify failures")
    // TODO UNFINISHED
    void "fail updating same password twice"() {
        when: "save company"
        repo.save(company)

        then: "successfully retrieve it"
        repo.findBy(company.id()).success()

        when: "update password"
        company.changePassword(password)
        repo.updatePassword(company)

        then: "retrieve updated company"
        def result = repo.findBy(company.id())
        result.success()

        and: "verify changes"
        def new_password = result.orElseThrow().password()
        new_password.password() != company.password().password()

        where:
        company << (1..10).collect({TestDataGenerator.generateCompany()})
        password << (1..10).collect({TestDataGenerator.generatePassword()})
    }
}
