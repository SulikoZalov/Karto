package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.user.values_objects.RefreshToken
import org.project.karto.infrastructure.repository.JDBCUserRepository
import org.project.karto.infrastructure.security.JWTUtility
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class UserRepoTest extends Specification{

    @Inject
    JDBCUserRepository repo

    @Inject
    JWTUtility jwtUtility

    void "successfully save user"() {
        when:
        def result = repo.save(user)

        then:
        result.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully save refresh token"() {
        given:
        def token = new RefreshToken(user.id(), jwtUtility.generateRefreshToken(user))

        when:
        def userSaveResult = repo.save(user)

        then:
        userSaveResult.success()

        when:
        def refreshTokenSaveResult = repo.saveRefreshToken(token)

        then:
        refreshTokenSaveResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully update phone number"() {
        when:
        def result = repo.save(user)

        then:
        result.success()

        when:
        user.registerPhoneForVerification(phone)
        def updatePhoneResult = repo.updatePhone(user)

        then:
        notThrown(Exception)
        updatePhoneResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUserWithoutPhoneAndPassword()})
        phone << (1..10).collect({TestDataGenerator.generatePhone()})
    }

    void "successfully update counter"() {
        when:
        def result = repo.save(user)

        then:
        result.success()

        when:
        user.incrementCounter()
        def updateCounterResult = repo.updateCounter(user)

        then:
        notThrown(Exception)
        updateCounterResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully update 2fa"() {
        when:
        def result = repo.save(user)

        then:
        result.success()

        when:
        user.incrementCounter()
        user.enable()
        user.incrementCounter()
        user.enable2FA()
        def _2faResult = repo.update2FA(user)

        then:
        notThrown(Exception)
        _2faResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully update verification"() {
        when:
        def result = repo.save(user)

        then:
        result.success()

        when:
        user.incrementCounter()
        user.enable()
        def verificationResult = repo.updateVerification(user)

        then:
        notThrown(Exception)
        verificationResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }
}
