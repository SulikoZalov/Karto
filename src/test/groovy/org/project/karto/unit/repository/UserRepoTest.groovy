package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.common.containers.Result
import org.project.karto.domain.common.value_objects.Amount
import org.project.karto.application.pagination.PageRequest
import org.project.karto.domain.card.entities.GiftCard
import org.project.karto.domain.common.value_objects.Email
import org.project.karto.domain.common.value_objects.Phone
import org.project.karto.domain.user.entities.User
import org.project.karto.domain.user.values_objects.RefreshToken
import org.project.karto.infrastructure.repository.JDBCCompanyRepository
import org.project.karto.infrastructure.repository.JDBCGiftCardRepository
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
    JDBCUserRepository userRepo

    @Inject
    JDBCGiftCardRepository giftCardRepo

    @Inject
    JDBCCompanyRepository companyRepo

    @Inject
    JWTUtility jwtUtility

    void "successfully save user"() {
        when:
        def result = userRepo.save(user)

        then:
        result.success()
        result.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully save refresh token"() {
        given:
        def token = new RefreshToken(user.id(), jwtUtility.generateRefreshToken(user))

        when:
        def userSaveResult = userRepo.save(user)

        then:
        userSaveResult.success()
        userSaveResult.value() == 1

        when:
        def refreshTokenSaveResult = userRepo.saveRefreshToken(token)

        then:
        refreshTokenSaveResult.success()
        refreshTokenSaveResult.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully update phone number"() {
        when:
        def result = userRepo.save(user)

        then:
        result.success()
        result.value() == 1

        when:
        user.registerPhoneForVerification(phone)
        def updatePhoneResult = userRepo.updatePhone(user)

        then:
        notThrown(Exception)
        updatePhoneResult.success()
        updatePhoneResult.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.generateUserWithoutPhoneAndPassword()})
        phone << (1..10).collect({TestDataGenerator.generatePhone()})
    }

    void "successfully update counter"() {
        when:
        def result = userRepo.save(user)

        then:
        result.success()
        result.value() == 1

        when:
        user.incrementCounter()
        def updateCounterResult = userRepo.updateCounter(user)

        then:
        notThrown(Exception)
        updateCounterResult.success()
        updateCounterResult.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully update 2fa"() {
        when:
        def result = userRepo.save(user)

        then:
        result.success()
        result.value() == 1

        when:
        user.incrementCounter()
        user.enable()
        user.incrementCounter()
        user.enable2FA()
        def _2faResult = userRepo.update2FA(user)

        then:
        notThrown(Exception)
        _2faResult.success()
        _2faResult.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully update verification"() {
        when:
        def result = userRepo.save(user)

        then:
        result.success()
        result.value() == 1

        when:
        user.incrementCounter()
        user.enable()
        def verificationResult = userRepo.updateVerification(user)

        then:
        notThrown(Exception)
        verificationResult.success()
        verificationResult.value() == 1

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully update cashback storage"() {
        when:
        def result = userRepo.save(user)
        BigDecimal oldCash = user.cashbackStorage().amount();

        then:
        result.success()
        result.value() == 1

        when:
        user.incrementCounter()
        user.enable()
        user.addCashback(amount, false)
        def storageUpdateResult = userRepo.updateCashbackStorage(user)

        then:
        notThrown(Exception)
        storageUpdateResult.success()
        storageUpdateResult.value() == 1
        user.cashbackStorage().amount() == oldCash.add(amount.value())

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
        amount << (1..10).collect({TestDataGenerator.generateAmount(BigDecimal.valueOf(50))})
    }

    void "successfully update ban"() {
        given:
        User user = TestDataGenerator.generateUser()

        when:
        user.ban()

        then:
        userRepo.updateBan(user)
    }

    void "successful is email exists"() {
        when:
        def result = userRepo.save(user)

        then:
        result.success()
        result.value() == 1

        when:
        def isExistsResult = userRepo.isEmailExists(new Email(user.personalData().email()))

        then:
        isExistsResult

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "fail is exists by non existent email"() {
        when:
        def isExistsResult = userRepo.isEmailExists(new Email(user.personalData().email()))

        then:
        !isExistsResult

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successful is phone number exists"() {
        when:
        def result = userRepo.save(user)

        then:
        result.success()

        when:
        def isExistsResult = userRepo.isPhoneExists(new Phone(user.personalData().phone().orElseThrow()))

        then:
        isExistsResult

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "fail is exists by non existent phone number"() {
        when:
        def isExistsResult = userRepo.isPhoneExists(new Phone(user.personalData().phone().orElseThrow()))

        then:
        !isExistsResult

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successful find by ID"() {
        when:
        def result = userRepo.save(user)

        then:
        result.success()

        when:
        def findResult = userRepo.findBy(user.id())

        then:
        findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "fail find by non existent ID"() {
        when:
        def findResult = userRepo.findBy(user.id())

        then:
        !findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successful find by email"() {
        when:
        def result = userRepo.save(user)

        then:
        result.success()

        when:
        def findResult = userRepo.findBy(new Email(user.personalData().email()))

        then:
        findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "fail find by non existent email"() {
        when:
        def findResult = userRepo.findBy(new Email(user.personalData().email()))

        then:
        !findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successful find by phone number"() {
        when:
        def result = userRepo.save(user)

        then:
        result.success()

        when:
        def findResult = userRepo.findBy(new Phone(user.personalData().phone().orElseThrow()))

        then:
        findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "fail find by non existent phone number"() {
        when:
        def findResult = userRepo.findBy(new Phone(user.personalData().phone().orElseThrow()))

        then:
        !findResult.success()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
    }

    void "successfully retrieve user's cards"() {
        given:
        List<GiftCard> cards = (1..5).collect({TestDataGenerator.generateSelfBougthGiftCard(user.id(), company.id())})

        when:
        def userSaveResult = userRepo.save(user)
        def companySaveResult = companyRepo.save(company)
        def cardSaveResults = cards.collect({giftCardRepo.save(it)})

        then:
        notThrown(Exception)
        userSaveResult.success()
        companySaveResult.success()
        cardSaveResults.forEach { it.orElseThrow() >= 0}

        when:
        def findResult = userRepo.userCards(new PageRequest(3, 0), new Email(user.personalData().email()))

        then:
        notThrown(Exception)
        findResult.success()

        println findResult.orElseThrow()

        where:
        user << (1..10).collect({ TestDataGenerator.generateUser()})
        company << (1..10).collect({TestDataGenerator.generateCompany()})
    }
}
