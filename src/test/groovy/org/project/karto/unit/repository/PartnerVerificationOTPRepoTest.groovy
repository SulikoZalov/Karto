package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.infrastructure.repository.JDBCCompanyRepository
import org.project.karto.infrastructure.repository.JDBCPartnerVerificationOTPRepository
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class PartnerVerificationOTPRepoTest extends Specification {

    @Inject
    JDBCCompanyRepository companyRepository

    @Inject
    JDBCPartnerVerificationOTPRepository otpRepository

    def "successfully save otp"() {
        when:
        def companySaveResult = companyRepository.save(company)
        def otp = TestDataGenerator.generatePartnerVerificationOTP(company)

        then:
        companySaveResult.success()

        when:
        def otpSaveResult = otpRepository.save(otp)

        then:
        notThrown(Throwable)
        otpSaveResult.success()

        when:
        def findBy = otpRepository.findBy(otp)

        then:
        notThrown(Throwable)
        findBy.success()
        def foundedOTP = findBy.value()
        !foundedOTP.isConfirmed()
        foundedOTP.otp() == otp.otp()

        where:
        company << (1..10).collect({ TestDataGenerator.generateCompany()})
    }

    void "fail saving OTP twice"() {
        when:
        def companySaveResult = companyRepository.save(company)
        def otp = TestDataGenerator.generatePartnerVerificationOTP(company)

        then:
        companySaveResult.success()

        when:
        def otpSaveResult1 = otpRepository.save(otp)
        def otpSaveResult2 = otpRepository.save(otp)

        then:
        notThrown(Throwable)
        otpSaveResult1.success()
        !otpSaveResult2.success()

        where:
        company << (1..10).collect({ TestDataGenerator.generateCompany()})
    }

    void "successfully update OTP"() {
        when:
        def companySaveResult = companyRepository.save(company)
        def otp = TestDataGenerator.generatePartnerVerificationOTP(company)

        then:
        companySaveResult.success()

        when:
        def otpSaveResult = otpRepository.save(otp)

        then:
        notThrown(Throwable)
        otpSaveResult.success()

        when:
//        otp.confirm()
        def confirmResult = otpRepository.updateConfirmation(otp)

        then:
        notThrown(Exception)
        confirmResult.success()

        where:
        company << (1..10).collect({ TestDataGenerator.generateCompany()})
    }

    void "successfully remove OTP"() {
        when:
        def companySaveResult = companyRepository.save(company)
        def otp = TestDataGenerator.generatePartnerVerificationOTP(company)

        then:
        companySaveResult.success()

        when:
        def otpSaveResult = otpRepository.save(otp)

        then:
        notThrown(Throwable)
        otpSaveResult.success()

        when:
        def removeResult = otpRepository.remove(otp)

        then:
        notThrown(Exception)
        removeResult.success()

        where:
        company << (1..10).collect({ TestDataGenerator.generateCompany()})
    }

    void "successfully find by OTP"() {
        when:
        def companySaveResult = companyRepository.save(company)
        def otp = TestDataGenerator.generatePartnerVerificationOTP(company)

        then:
        companySaveResult.success()

        when:
        def otpSaveResult = otpRepository.save(otp)

        then:
        notThrown(Throwable)
        otpSaveResult.success()

        when:
        def findResult = otpRepository.findBy(otp)

        then:
        notThrown(Exception)
        findResult.success()

        where:
        company << (1..10).collect({ TestDataGenerator.generateCompany()})
    }

    void "fail find by non existent OTP"() {
        when:
        def companySaveResult = companyRepository.save(company)
        def otp = TestDataGenerator.generatePartnerVerificationOTP(company)

        then:
        companySaveResult.success()

        when:
        def findResult = otpRepository.findBy(otp)

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        company << (1..10).collect({ TestDataGenerator.generateCompany()})
    }

    void "successfully find by OTP string"() {
        when:
        def companySaveResult = companyRepository.save(company)
        def otp = TestDataGenerator.generatePartnerVerificationOTP(company)

        then:
        companySaveResult.success()

        when:
        def otpSaveResult = otpRepository.save(otp)

        then:
        notThrown(Throwable)
        otpSaveResult.success()

        when:
        def findResult = otpRepository.findBy(otp.otp())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        company << (1..10).collect({ TestDataGenerator.generateCompany()})
    }

    void "fail find by non existent OTP string"() {
        when:
        def companySaveResult = companyRepository.save(company)
        def otp = TestDataGenerator.generatePartnerVerificationOTP(company)

        then:
        companySaveResult.success()

        when:
        def findResult = otpRepository.findBy(otp.otp())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        company << (1..10).collect({ TestDataGenerator.generateCompany()})
    }

    void "successfully find by company ID"() {
        when:
        def companySaveResult = companyRepository.save(company)
        def otp = TestDataGenerator.generatePartnerVerificationOTP(company)

        then:
        companySaveResult.success()

        when:
        def otpSaveResult = otpRepository.save(otp)

        then:
        notThrown(Throwable)
        otpSaveResult.success()

        when:
        def findResult = otpRepository.findBy(company.id())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        company << (1..10).collect({ TestDataGenerator.generateCompany()})
    }

    void "fail find by non existent company ID"() {
        given:
        def noNameCompany = TestDataGenerator.generateCompany()
        when:
        def companySaveResult = companyRepository.save(company)
        def otp = TestDataGenerator.generatePartnerVerificationOTP(company)

        then:
        companySaveResult.success()

        when:
        def otpSaveResult = otpRepository.save(otp)

        then:
        notThrown(Throwable)
        otpSaveResult.success()

        when:
        def findResult = otpRepository.findBy(noNameCompany.id())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        company << (1..10).collect({ TestDataGenerator.generateCompany()})
    }
}
