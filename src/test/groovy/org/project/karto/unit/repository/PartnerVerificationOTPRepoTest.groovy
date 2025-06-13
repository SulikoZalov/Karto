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
}
