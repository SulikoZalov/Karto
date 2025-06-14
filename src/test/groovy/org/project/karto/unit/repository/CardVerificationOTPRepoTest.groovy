package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.card.entities.CardVerificationOTP
import org.project.karto.infrastructure.repository.JDBCCardVerificationOTPRepository
import org.project.karto.infrastructure.repository.JDBCGiftCardRepository
import org.project.karto.infrastructure.security.HOTPGenerator
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class CardVerificationOTPRepoTest extends Specification {

    @Inject
    JDBCCardVerificationOTPRepository verificationOTPRepository

    @Inject
    JDBCGiftCardRepository cardRepository

    HOTPGenerator hotpGenerator

    def setup() {
        hotpGenerator = new HOTPGenerator()
    }

    void "successfully save card otp"() {
        given:
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }

    void "fail saving card otp twice"() {
        given:
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult1 = verificationOTPRepository.save(otp)
        def saveResult2 = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult1.success()
        !saveResult2.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }

    void "successful card OTP update"() {
        given:
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        when:
        otp.confirm()
        def confirmResult = verificationOTPRepository.update(otp)

        then:
        notThrown(Exception)
        confirmResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }

    void "successful remove OTP"() {
        given:
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        when:
        def removeResult = verificationOTPRepository.remove(otp)

        then:
        notThrown(Exception)
        removeResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }

    void "successful find by card OTP"() {
        given:
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        when:
        def findResult = verificationOTPRepository.findBy(otp)

        then:
        notThrown(Exception)
        findResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }

    void "fail find by non existent card OTP"() {
        given:
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def findResult = verificationOTPRepository.findBy(otp)

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }

    void "successful find by card OTP string"() {
        given:
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        when:
        def findResult = verificationOTPRepository.findBy(otp.otp())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }

    void "fail find by non existent card OTP string"() {
        given:
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def findResult = verificationOTPRepository.findBy(otp.otp())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }

    void "successful find by owner ID"() {
        given:
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        when:
        def findResult = verificationOTPRepository.findBy(card.ownerID())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }

    void "fail find by non existent owner ID"() {
        given:
        def noNameCard = TestDataGenerator.generateSelfBougthGiftCard()
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        when:
        def findResult = verificationOTPRepository.findBy(noNameCard.ownerID())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }

    void "successful find by card ID"() {
        given:
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        when:
        def findResult = verificationOTPRepository.findBy(card.id())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }

    void "fail find by non existent card ID"() {
        given:
        def noNameCard = TestDataGenerator.generateSelfBougthGiftCard()
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        when:
        def findResult = verificationOTPRepository.findBy(noNameCard.id())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }
}
