package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.card.enumerations.PaymentType
import org.project.karto.domain.card.value_objects.Currency
import org.project.karto.domain.card.value_objects.ExternalPayeeDescription
import org.project.karto.domain.card.value_objects.PaymentSystem
import org.project.karto.domain.card.value_objects.UserActivitySnapshot
import org.project.karto.domain.common.value_objects.Amount
import org.project.karto.infrastructure.repository.JDBCGiftCardRepository
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification
import spock.lang.Unroll

import java.math.RoundingMode
import java.time.LocalDateTime

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class GiftCardRepoTest extends Specification {

    @Inject
    JDBCGiftCardRepository repo

    @Inject
    Util util

    @Unroll
    def "save gift card: self bought [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        index << (1..10)
    }

    @Unroll
    def  "save gift card: bought as gift [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        index << (1..10)
    }

    @Unroll
    def "save gift card: self bought common type [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        index << (1..10)
    }

    @Unroll
    def "save gift card: bought as gift common type [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        index << (1..10)
    }

    @Unroll
    def "fail saving same card twice [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result1 = repo.save(giftCard)
        def result2 = repo.save(giftCard)

        then:
        result1.success()
        result1.value() == 1
        !result2.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "update gift card after transaction [#index]"() {
        given: "A newly created gift card"
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def saveResult = repo.save(giftCard)

        expect: "Gift card is saved"
        saveResult.success()
        saveResult.value() == 1

        when: "Activating the gift card"
        giftCard.activate()
        def activationResult = repo.update(giftCard)

        then: "Activation update should succeed"
        activationResult.success()
        activationResult.value() == 1

        when: "Initialize and complete a transaction"
        def reloadedCard1 = repo.findBy(giftCard.id()).value()
        def amount = new Amount(reloadedCard1.balance().value()
                .divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP))
        def paymentIntent = reloadedCard1.initializeTransaction(amount, TestDataGenerator.orderID())
        paymentIntent.markAsSuccess(new ExternalPayeeDescription("desc"))
        repo.update(reloadedCard1)

        def reloadedCard2 = repo.findBy(reloadedCard1.id()).value()

        reloadedCard2.applyTransaction(
                paymentIntent,
                new UserActivitySnapshot(
                        reloadedCard1.ownerID().get().value(),
                        new BigDecimal("1000"),
                        30,
                        LocalDateTime.now(),
                        5
                ),
                Currency.getInstance("USD"),
                PaymentType.KARTO_PAYMENT,
                new PaymentSystem("UP")
        )
        def updateResult = repo.update(reloadedCard2)

        then: "Transaction update should succeed"
        updateResult.success()
        updateResult.value() == 1

        where:
        index << (1..10)
    }

    @Unroll
    def "update amount of activated gift card through full lifecycle [#index]"() {
        given: "A newly created gift card"
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        def saveResult = repo.save(giftCard)

        expect: "Initial save should succeed"
        saveResult.success()
        saveResult.value() == 1

        when: "Activating the card"
        giftCard.activate()
        def activationResult = repo.update(giftCard)

        then: "Activation update should succeed"
        activationResult.success()
        activationResult.value() == 1

        when: "Performing a transaction"
        def reloadedCard1 = repo.findBy(giftCard.id()).value()
        def amount = new Amount(reloadedCard1.balance().value()
                .divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP))
        def paymentIntent = reloadedCard1.initializeTransaction(amount, TestDataGenerator.orderID())
        paymentIntent.markAsSuccess(new ExternalPayeeDescription("desc"))
        repo.update(reloadedCard1)

        def reloadedCard2 = repo.findBy(reloadedCard1.id()).value()

        reloadedCard2.applyTransaction(
                paymentIntent,
                new UserActivitySnapshot(
                        reloadedCard2.ownerID().get().value(),
                        new BigDecimal("5000"),
                        60,
                        LocalDateTime.now(),
                        10
                ),
                Currency.getInstance("USD"),
                PaymentType.KARTO_PAYMENT,
                new PaymentSystem("UP")
        )
        def transactionResult = repo.update(reloadedCard2)

        then: "Transaction update should succeed"
        transactionResult.success()
        transactionResult.value() == 1

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by card ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1

        when:
        def findResult = repo.findBy(giftCard.id())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "fail find by non existent card ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def findResult = repo.findBy(giftCard.id())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by buyer ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()

        when:
        def findResult = repo.findBy(giftCard.buyerID())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "fail find by non existent buyer ID: empty list [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def findResult = repo.findBy(giftCard.buyerID())

        then:
        notThrown(Exception)
        findResult.success()
        def value = findResult.value()
        value.size() == 0

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by owner ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()

        when:
        def findResult = repo.findBy(giftCard.ownerID().get())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "fail find by non existent owner ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def findResult = repo.findBy(giftCard.ownerID().get())

        then:
        notThrown(Exception)
        findResult.success()
        def value = findResult.value()
        value.size() == 0

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by store ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()

        when:
        def findResult = repo.findBy(giftCard.storeID().get())

        then:
        notThrown(Exception)
        findResult.success()

        where:
        index << (1..10)
    }

    def "fail find by non existent store ID"() {
        when:
        def findResult = repo.findBy(giftCard.storeID().get())

        then:
        notThrown(Exception)
        findResult.success()
        def value = findResult.value()
        value.size() == 0

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }
}