package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.user.entities.OTP
import org.project.karto.infrastructure.repository.JDBCOTPRepository
import org.project.karto.infrastructure.repository.JDBCUserRepository
import org.project.karto.infrastructure.security.HOTPGenerator
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class OTPRepoTest extends Specification{

    @Inject
    JDBCOTPRepository otpRepo

    @Inject
    JDBCUserRepository userRepo

    HOTPGenerator hotpGenerator

    def setup() {
        hotpGenerator = new HOTPGenerator()
    }

    void "successfully save OTP"() {
        given:
        def otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()))

        when:
        def userSaveResult = userRepo.save(user)

        then:
        userSaveResult.success()
        userSaveResult.value() == 1

        when:
        def result = otpRepo.save(otp)

        then:
        notThrown(Exception)
        result.success()
        result.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "fail saving OTP of non existent user"() {
        given:
        def otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()))

        when:
        def result = otpRepo.save(otp)

        then:
        notThrown(Exception)
        !result.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully update OTP confirmation"() {
        given:
        def otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()))

        when:
        def userSaveResult = userRepo.save(user)

        then:
        userSaveResult.success()
        userSaveResult.value() == 1

        when:
        def result = otpRepo.save(otp)

        then:
        notThrown(Exception)
        result.success()
        result.value() == 1

        when:
        otp.confirm()
        def updateResult = otpRepo.updateConfirmation(otp)

        then:
        notThrown(Exception)
        updateResult.success()
        updateResult.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully remove OTP"() {
        given:
        def otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()))

        when:
        def userSaveResult = userRepo.save(user)

        then:
        userSaveResult.success()
        userSaveResult.value() == 1

        when:
        def result = otpRepo.save(otp)

        then:
        notThrown(Exception)
        result.success()
        result.value() == 1

        when:
        def removeResult = otpRepo.remove(otp)

        then:
        notThrown(Exception)
        removeResult.success()
        removeResult.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "OTP contains user id success"() {
        given:
        def otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()))

        when:
        def userSaveResult = userRepo.save(user)

        then:
        userSaveResult.success()
        userSaveResult.value() == 1

        when:
        def result = otpRepo.save(otp)

        then:
        notThrown(Exception)
        result.success()
        result.value() == 1

        when:
        def containsResult = otpRepo.contains(user.id())

        then:
        notThrown(Exception)
        containsResult

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successful find by OTP"() {
        given:
        def otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()))

        when:
        def userSaveResult = userRepo.save(user)

        then:
        userSaveResult.success()
        userSaveResult.value() == 1

        when:
        def result = otpRepo.save(otp)

        then:
        notThrown(Exception)
        result.success()
        result.value() == 1

        when:
        def findResult = otpRepo.findBy(otp)

        then:
        notThrown(Exception)
        findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "fail find by non existent OTP"() {
        given:
        def otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()))

        when:
        def userSaveResult = userRepo.save(user)

        then:
        userSaveResult.success()

        when:
        def findResult = otpRepo.findBy(otp)

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successful find by OTP string"() {
        given:
        def otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()))

        when:
        def userSaveResult = userRepo.save(user)

        then:
        userSaveResult.success()

        when:
        def result = otpRepo.save(otp)

        then:
        notThrown(Exception)
        result.success()

        when:
        def findResult = otpRepo.findBy(otp.otp())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "fail find by non existent OTP string"() {
        given:
        def otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()))

        when:
        def userSaveResult = userRepo.save(user)

        then:
        userSaveResult.success()

        when:
        def findResult = otpRepo.findBy(otp.otp())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successful find by user ID"() {
        given:
        def otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()))

        when:
        def userSaveResult = userRepo.save(user)

        then:
        userSaveResult.success()

        when:
        def result = otpRepo.save(otp)

        then:
        notThrown(Exception)
        result.success()

        when:
        def findResult = otpRepo.findBy(user.id())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "fail find by non existent user ID"() {
        given:
        def noNameUser = TestDataGenerator.generateUser()
        def otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()))

        when:
        def userSaveResult = userRepo.save(user)

        then:
        userSaveResult.success()

        when:
        def result = otpRepo.save(otp)

        then:
        notThrown(Exception)
        result.success()

        when:
        def findResult = otpRepo.findBy(noNameUser.id())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }
}
