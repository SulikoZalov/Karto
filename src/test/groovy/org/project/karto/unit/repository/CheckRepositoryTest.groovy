package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.card.entities.CardPurchaseIntent
import org.project.karto.domain.card.enumerations.PaymentType
import org.project.karto.domain.card.value_objects.*
import org.project.karto.domain.common.value_objects.Amount
import org.project.karto.infrastructure.repository.JDBCCheckRepository
import org.project.karto.infrastructure.repository.JDBCGiftCardRepository
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification
import spock.lang.Unroll

import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

import static org.project.karto.util.TestDataGenerator.orderID

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class CheckRepositoryTest extends Specification {

    @Inject
    JDBCCheckRepository repo

    @Inject
    JDBCGiftCardRepository giftCardRepo

    @Inject
    Util util

    void "successful save"() {
        given:
        def userID = util.generateActivateAndSaveUser()
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(userID), null, 1L, new Amount(100))
        def fee = TestDataGenerator.generateFee(BigDecimal.valueOf(0.05))
        def check = intent.markAsSuccess(fee, new Currency("AZN"), PaymentType.FOREIGN_BANK, new PaymentSystem("UP"), new PayeeDescription("desc"), new BankName("BANK"))

        when:
        def result = repo.save(check)

        then:
        notThrown(Exception)
        result.success()
        result.orElseThrow() > 0
    }

    @Unroll
    def "successful save pf payment check [#index]"() {
        given: "Saved gift card"
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
        def saveResult = giftCardRepo.save(giftCard)

        expect: "Initial save should succeed"
        saveResult.success()
        saveResult.value() == 1

        when: "Activating the card"
        giftCard.activate()
        def activationResult = giftCardRepo.update(giftCard)

        then: "Activation update should succeed"
        activationResult.success()
        activationResult.value() == 1

        when: "Performing a transaction"
        def reloadedCard1 = giftCardRepo.findBy(giftCard.id()).value()
        def amount = new Amount(reloadedCard1.balance().value().divide(BigDecimal.valueOf(10), RoundingMode.HALF_UP))
        def paymentIntent = reloadedCard1.initializeTransaction(amount, orderID())
        paymentIntent.markAsSuccess(new PayeeDescription("desc"))
        def giftCardTransactionInitializationUpdateRes = giftCardRepo.update(reloadedCard1)

        then: "Expect successful transaction initialization update"
        giftCardTransactionInitializationUpdateRes.success()
        giftCardTransactionInitializationUpdateRes.value() == 1

        when : "Applying transaction and update"
        def reloadedCard2 = giftCardRepo.findBy(reloadedCard1.id()).value()
        def check = reloadedCard2.applyTransaction(
                paymentIntent,
                new UserActivitySnapshot(
                        reloadedCard2.ownerID().get().value(),
                        new BigDecimal("5000"),
                        60,
                        LocalDateTime.now(),
                        10, false
                ),
                Currency.getInstance("USD"),
                PaymentType.KARTO_PAYMENT,
                new PaymentSystem("UP"),
                new BankName("BANK")
        )
        def transactionResult = giftCardRepo.update(reloadedCard2)

        then: "Transaction update should succeed"
        transactionResult.success()
        transactionResult.value() == 1

        when: "Save check"
        def resultOfCheckSaving = repo.save(check)

        then: "Expect successful check saving"
        resultOfCheckSaving.success()
        resultOfCheckSaving.value() == 1

        where:
        index << (1..10)
    }

    void "fail saving twice"() {
        given:
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(util.generateActivateAndSaveUser()), null, orderID(), new Amount(100))
        def fee = TestDataGenerator.generateFee(BigDecimal.valueOf(0.05))
        def check = intent.markAsSuccess(fee, new Currency("AZN"), PaymentType.FOREIGN_BANK, new PaymentSystem("UP"), new PayeeDescription("desc"), new BankName("BANK"))

        when:
        def result = repo.save(check)
        def fail_result = repo.save(check)

        then:
        notThrown(Exception)
        result.success()
        result.orElseThrow() > 0

        and:
        !fail_result.success()
    }

    void "successful find by check id"() {
        given:
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(util.generateActivateAndSaveUser()), null, orderID(), new Amount(100))
        def fee = TestDataGenerator.generateFee(BigDecimal.valueOf(0.05))
        def check = intent.markAsSuccess(fee, new Currency("AZN"), PaymentType.FOREIGN_BANK, new PaymentSystem("UP"), new PayeeDescription("desc"), new BankName("BANK"))

        when:
        def result = repo.save(check)

        then:
        notThrown(Exception)
        result.success()
        result.orElseThrow() > 0

        when:
        def find_result = repo.findBy(check.id())

        then:
        notThrown(Exception)
        find_result.success()

        def found_check = find_result.orElseThrow()

        found_check.id() == check.id()
        found_check.orderID() == check.orderID()
        found_check.buyerID() == check.buyerID()
        found_check.totalAmount() == check.totalAmount()
        found_check.currency() == check.currency()
        found_check.paymentType() == check.paymentType()
        found_check.internalFee() == check.internalFee()
        found_check.externalFee() == check.externalFee()
        found_check.paymentSystem() == check.paymentSystem()
        found_check.description() == check.description()
        found_check.creationDate().truncatedTo(ChronoUnit.MILLIS) == check.creationDate().truncatedTo(ChronoUnit.MILLIS)

        if (found_check.storeID() != null && check.storeID() != null) {
            found_check.storeID() == check.storeID()
        }

        if (found_check.cardID() != null && check.cardID() != null) {
            found_check.cardID() == check.cardID()
        }

    }

    void "successful find by buyer id"() {
        given:
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(util.generateActivateAndSaveUser()), null, orderID(), new Amount(100))
        def fee = TestDataGenerator.generateFee(BigDecimal.valueOf(0.05))
        def check = intent.markAsSuccess(fee, new Currency("AZN"), PaymentType.FOREIGN_BANK, new PaymentSystem("UP"), new PayeeDescription("desc"), new BankName("BANK"))

        when:
        def result = repo.save(check)

        then:
        notThrown(Exception)
        result.success()
        result.orElseThrow() > 0

        when:
        def find_result = repo.findBy(check.buyerID())

        then:
        notThrown(Exception)
        find_result.success()

        def found_check = find_result.orElseThrow().getFirst()

        found_check.id() == check.id()
        found_check.orderID() == check.orderID()
        found_check.buyerID() == check.buyerID()
        found_check.totalAmount() == check.totalAmount()
        found_check.currency() == check.currency()
        found_check.paymentType() == check.paymentType()
        found_check.internalFee() == check.internalFee()
        found_check.externalFee() == check.externalFee()
        found_check.paymentSystem() == check.paymentSystem()
        found_check.description() == check.description()
        found_check.creationDate().truncatedTo(ChronoUnit.MILLIS) == check.creationDate().truncatedTo(ChronoUnit.MILLIS)

        if (found_check.storeID() != null && check.storeID() != null) {
            found_check.storeID() == check.storeID()
        }

        if (found_check.cardID() != null && check.cardID() != null) {
            found_check.cardID() == check.cardID()
        }

    }

    void "successful find by store id"() {
        given:
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(util.generateActivateAndSaveUser()), new StoreID(util.generateActivateAndSaveCompany()), orderID(), new Amount(100))
        def fee = TestDataGenerator.generateFee(BigDecimal.valueOf(0.05))
        def check = intent.markAsSuccess(fee, new Currency("AZN"), PaymentType.FOREIGN_BANK, new PaymentSystem("UP"), new PayeeDescription("desc"), new BankName("BANK"))

        when:
        def result = repo.save(check)

        then:
        notThrown(Exception)
        result.success()
        result.orElseThrow() > 0

        when:
        def find_result = repo.findBy(check.storeID().orElseThrow())

        then:
        notThrown(Exception)
        find_result.success()

        def found_check = find_result.orElseThrow().getFirst()

        found_check.id() == check.id()
        found_check.orderID() == check.orderID()
        found_check.buyerID() == check.buyerID()
        found_check.totalAmount() == check.totalAmount()
        found_check.currency() == check.currency()
        found_check.paymentType() == check.paymentType()
        found_check.internalFee() == check.internalFee()
        found_check.externalFee() == check.externalFee()
        found_check.paymentSystem() == check.paymentSystem()
        found_check.description() == check.description()
        found_check.creationDate().truncatedTo(ChronoUnit.MILLIS) == check.creationDate().truncatedTo(ChronoUnit.MILLIS)

        if (found_check.storeID() != null && check.storeID() != null) {
            found_check.storeID() == check.storeID()
        }

        if (found_check.cardID() != null && check.cardID() != null) {
            found_check.cardID() == check.cardID()
        }

    }
}
