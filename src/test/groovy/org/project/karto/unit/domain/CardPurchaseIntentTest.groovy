package org.project.karto.unit.domain

import org.project.karto.domain.card.entities.CardPurchaseIntent
import org.project.karto.domain.card.enumerations.PaymentType
import org.project.karto.domain.card.enumerations.PurchaseStatus
import org.project.karto.domain.card.value_objects.*
import org.project.karto.domain.common.value_objects.Amount
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

class CardPurchaseIntentTest extends Specification {

    def "should create CardPurchaseIntent in PENDING status with optional storeID"() {
        given:
        def id = UUID.randomUUID()
        def buyerID = new BuyerID(UUID.randomUUID())
        def storeID = new StoreID(UUID.randomUUID())
        def orderID = 100L
        def totalAmount = new Amount(100)

        when:
        def intent = CardPurchaseIntent.of(id, buyerID, storeID, orderID, totalAmount)

        then:
        intent.id() == id
        intent.buyerID() == buyerID
        intent.storeID().get() == storeID
        intent.orderID() == orderID
        intent.totalPayedAmount() == totalAmount
        intent.status() == PurchaseStatus.PENDING
        intent.resultDate().isEmpty()
    }

    def "should throw exception when creating with invalid input"() {
        when:
        CardPurchaseIntent.of(null, new BuyerID(UUID.randomUUID()), null, 1L, new Amount(10))

        then:
        thrown(IllegalArgumentException)

        when:
        CardPurchaseIntent.of(UUID.randomUUID(), null, null, 1L, new Amount(10))

        then:
        thrown(IllegalArgumentException)

        when:
        CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(UUID.randomUUID()), null, -1L, new Amount(10))

        then:
        thrown(IllegalArgumentException)

        when:
        CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(UUID.randomUUID()), null, 1L, null)

        then:
        thrown(IllegalArgumentException)
    }

    def "should mark intent as SUCCESS and set removed fee"() {
        given:
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(UUID.randomUUID()), null, 1L, new Amount(100))
        def fee = TestDataGenerator.generateFee(BigDecimal.valueOf(0.05))

        when:
        intent.markAsSuccess(fee, Currency.getInstance("AZN"),
                PaymentType.FOREIGN_BANK, new PaymentSystem("UP"), new ExternalPayeeDescription("desc"))

        then:
        intent.status() == PurchaseStatus.SUCCESS
        intent.removedFee().get() == fee
        intent.resultDate().isPresent()
    }

    def "should not allow success if fee greater than total amount"() {
        given:
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(UUID.randomUUID()), null, 1L, new Amount(50))
        def fee = TestDataGenerator.generateFee(BigDecimal.valueOf(100))

        when:
        intent.markAsSuccess(fee, Currency.getInstance("AZN"),
                PaymentType.FOREIGN_BANK, new PaymentSystem("UP"), new ExternalPayeeDescription("desc"))

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message.contains("commission cannot be greater")
    }

    def "should calculate net amount correctly after success"() {
        given:
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(UUID.randomUUID()), null, 1L, new Amount(100))
        def fee = new Fee(BigDecimal.valueOf(0.05))

        intent.markAsSuccess(fee, Currency.getInstance("AZN"),
                PaymentType.FOREIGN_BANK, new PaymentSystem("UP"), new ExternalPayeeDescription("desc"))

        when:
        def netAmount = intent.calculateNetAmount()

        then:
        netAmount.value() == 95.00
    }

    def "should throw when calculating net amount if not SUCCESS"() {
        given:
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(UUID.randomUUID()), null, 1L, new Amount(100))

        when:
        intent.calculateNetAmount()

        then:
        thrown(IllegalStateException)
    }

    def "should allow marking as CANCEL or FAILURE only when pending"() {
        given:
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(UUID.randomUUID()), null, 1L, new Amount(100))

        when:
        intent.markAsCancel()

        then:
        intent.status() == PurchaseStatus.CANCEL
        intent.resultDate().isPresent()

        when:
        def anotherIntent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(UUID.randomUUID()), null, 2L, new Amount(100))
        anotherIntent.markAsFailure()

        then:
        anotherIntent.status() == PurchaseStatus.FAILURE
        anotherIntent.resultDate().isPresent()
    }

    def "should not allow double status change"() {
        given:
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(UUID.randomUUID()), null, 1L, new Amount(100))
        intent.markAsCancel()
        def fee = TestDataGenerator.generateFee(BigDecimal.valueOf(100))

        when:
        intent.markAsFailure()

        then:
        thrown(IllegalStateException)

        when:
        intent.markAsSuccess(fee, Currency.getInstance("AZN"),
                PaymentType.FOREIGN_BANK, new PaymentSystem("UP"), new ExternalPayeeDescription("desc"))

        then:
        thrown(IllegalStateException)
    }

    def "should fail when marking success with null fee"() {
        given:
        def intent = CardPurchaseIntent.of(UUID.randomUUID(), new BuyerID(UUID.randomUUID()), null, 1L, new Amount(100))

        when:
        intent.markAsSuccess(null, null, null, null, null)

        then:
        thrown(IllegalArgumentException)
    }
}
