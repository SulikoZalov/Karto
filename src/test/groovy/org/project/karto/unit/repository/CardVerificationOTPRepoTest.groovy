package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.card.entities.CardVerificationOTP
import org.project.karto.domain.card.entities.GiftCard
import org.project.karto.infrastructure.repository.JDBCCardVerificationOTPRepository
import org.project.karto.infrastructure.repository.JDBCGiftCardRepository
import org.project.karto.infrastructure.security.HOTPGenerator
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification
import spock.lang.Unroll

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class CardVerificationOTPRepoTest extends Specification {

    @Inject
    JDBCCardVerificationOTPRepository verificationOTPRepository

    @Inject
    JDBCGiftCardRepository cardRepository

    @Inject
    Util util

    HOTPGenerator hotpGenerator

    def setup() {
        hotpGenerator = new HOTPGenerator()
    }

    @Unroll
    def "successfully save card otp [#index]"() {
        given:
        GiftCard card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        def writeResult = cardRepository.save(card)

        expect:
        writeResult.success()
        writeResult.value() == 1

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()
        saveResult.value() == 1

        where:
        index << (1..10)
    }

    @Unroll
    def "fail saving card otp twice [#index]"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        def writeResult = cardRepository.save(card)

        expect:
        writeResult.success()
        writeResult.value() == 1

        when:
        def saveResult1 = verificationOTPRepository.save(otp)
        def saveResult2 = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult1.success()
        saveResult1.value() == 1
        !saveResult2.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "successful card OTP update [#index]"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        def writeResult = cardRepository.save(card)

        expect:
        writeResult.success()
        writeResult.value() == 1

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()
        saveResult.value() == 1

        when:
        otp.confirm()
        def confirmResult = verificationOTPRepository.update(otp)

        then:
        notThrown(Exception)
        confirmResult.success()
        confirmResult.value() == 1

        where:
        index << (1..10)
    }

    @Unroll
    def "successful remove OTP [#index]"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        def writeResult = cardRepository.save(card)

        expect:
        writeResult.success()
        writeResult.value() == 1

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()
        saveResult.value() == 1

        when:
        def removeResult = verificationOTPRepository.remove(otp)

        then:
        notThrown(Exception)
        removeResult.success()
        removeResult.value() == 1

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by card OTP [#index]"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        def writeResult = cardRepository.save(card)

        expect:
        writeResult.success()
        writeResult.value() == 1

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()
        saveResult.value() == 1

        when:
        def findResult = verificationOTPRepository.findBy(otp)

        then:
        notThrown(Exception)
        findResult.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "fail find by non existent card OTP [#index]"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def findResult = verificationOTPRepository.findBy(otp)

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by card OTP string [#index]"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
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
        index << (1..10)
    }

    @Unroll
    def "fail find by non existent card OTP string [#index]"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def findResult = verificationOTPRepository.findBy(otp.otp())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by owner ID [#index]"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        when:
        def findResult = verificationOTPRepository.findBy(card.ownerID().get())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "fail find by non existent owner ID [#index]"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def noNameCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        when:
        def findResult = verificationOTPRepository.findBy(noNameCard.ownerID().get())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by card ID [#index]"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
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
        index << (1..10)
    }

    @Unroll
    def "fail find by non existent card ID [#index]"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def noNameCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
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
        index << (1..10)
    }

}