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

import java.math.RoundingMode
import java.time.LocalDateTime

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class GiftCardRepoTest extends Specification {

    @Inject
    JDBCGiftCardRepository repo

    void "save gift card: self bought"() {
        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "save gift card: bought as gift"() {
        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateBoughtAsGiftCard()})
    }

    void "save gift card: self bought common type"() {
        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBoughtCommonGiftCard()})
    }

    void "save gift card: bought as gift common type"() {
        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateBoughtAsGiftCommonCard()})
    }

    void "fail saving same card twice"() {
        when:
        def result1 = repo.save(giftCard)
        def result2 = repo.save(giftCard)

        then:
        result1.success()
        result1.value() == 1
        !result2.success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "update gift card after transaction"() {
        given: "A newly created gift card"
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
        giftCard << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard() }
    }

    void "update amount of activated gift card through full lifecycle"() {
        given: "A newly created gift card"
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
        giftCard << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard() }
    }

    void "successful find by card ID"() {
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
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "fail find by non existent card ID"() {
        when:
        def findResult = repo.findBy(giftCard.id())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "successful find by buyer ID"() {
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
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "fail find by non existent buyer ID: empty list"() {
        when:
        def findResult = repo.findBy(giftCard.buyerID())

        then:
        notThrown(Exception)
        findResult.success()
        def value = findResult.value()
        value.size() == 0

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "successful find by owner ID"() {
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
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "fail find by non existent owner ID"() {
        when:
        def findResult = repo.findBy(giftCard.ownerID().get())

        then:
        notThrown(Exception)
        findResult.success()
        def value = findResult.value()
        value.size() == 0

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "successful find by store ID"() {
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
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "fail find by non existent store ID"() {
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