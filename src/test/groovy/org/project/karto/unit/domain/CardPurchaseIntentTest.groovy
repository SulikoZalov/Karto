package org.project.karto.unit.domain

import org.project.karto.domain.card.entities.CardPurchaseIntent
import org.project.karto.domain.card.enumerations.PurchaseStatus
import org.project.karto.domain.card.value_objects.BuyerID
import org.project.karto.domain.card.value_objects.Fee
import org.project.karto.domain.common.value_objects.Amount
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

class CardPurchaseIntentTest extends Specification {

    def "should create CardPurchaseIntent with valid parameters"() {
        given:
        UUID id = UUID.randomUUID()
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        long orderId = 123
        Amount amount = new Amount(new BigDecimal("100.00"))

        when:
        def intent = CardPurchaseIntent.of(id, buyerID, null, orderId, amount)

        then:
        intent.id() == id
        intent.buyerID() == buyerID
        intent.orderID() == orderId
        intent.totalPayedAmount() == amount
        intent.status() == PurchaseStatus.PENDING
        intent.resultDate().isEmpty()
    }

    @Unroll
    def "should throw exception for invalid parameters during creation: id=#id, buyerID=#buyerID, orderID=#orderId, amount=#amount"() {
        when:
        CardPurchaseIntent.of(id, buyerID, null, orderId, amount)

        then:
        thrown(IllegalArgumentException)

        where:
        id          | buyerID                 | orderId | amount
        null        | new BuyerID(UUID.randomUUID()) | 1      | new Amount(BigDecimal.ONE)
        UUID.randomUUID() | null              | 1      | new Amount(BigDecimal.ONE)
        UUID.randomUUID() | new BuyerID(UUID.randomUUID()) | -1 | new Amount(BigDecimal.ONE)
        UUID.randomUUID() | new BuyerID(UUID.randomUUID()) | 1  | null
        UUID.fromString("00000000-0000-0000-0000-000000000001") | new BuyerID(UUID.fromString("00000000-0000-0000-0000-000000000001")) | 1 | new Amount(BigDecimal.ONE)
    }

    def "should mark intent as success with valid fee"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("100.00")))
        Fee fee = new Fee(new BigDecimal("0.1"))

        when:
        intent.markAsSuccess(fee)

        then:
        intent.status() == PurchaseStatus.SUCCESS
        intent.resultDate().isPresent()
        intent.removedFee().get() == fee
    }

    def "should throw if trying to mark as success twice"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("100.00")))
        Fee fee = new Fee(new BigDecimal("0.05"))
        intent.markAsSuccess(fee)

        when:
        intent.markAsSuccess(fee)

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "Transaction cannot change it`s status twice."
    }

    def "should throw if fee is null during success"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("100.00")))

        when:
        intent.markAsSuccess(null)

        then:
        IllegalArgumentException e = thrown()
        e.message == "Removed fee cannot be null"
    }

    def "should throw if fee amount is greater than total payed amount"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("10.00")))
        Fee highFee = new Fee(new BigDecimal("2.0")) // 200%

        when:
        intent.markAsSuccess(highFee)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "The commission cannot be greater than the total amount paid."
    }

    def "should calculate net amount correctly after success"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("100.00")))
        Fee fee = new Fee(new BigDecimal("0.1"))
        intent.markAsSuccess(fee)

        when:
        def netAmount = intent.calculateNetAmount()

        then:
        netAmount.value() == new BigDecimal("90.00")
    }

    def "should throw when calculating net amount before success"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("100.00")))

        when:
        intent.calculateNetAmount()

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "Cannot calculate net amount: status is not SUCCESS"
    }

    def "should mark as cancel from pending"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("100.00")))

        when:
        intent.markAsCancel()

        then:
        intent.status() == PurchaseStatus.CANCEL
        intent.resultDate().isPresent()
    }

    def "should mark as failure from pending"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("100.00")))

        when:
        intent.markAsFailure()

        then:
        intent.status() == PurchaseStatus.FAILURE
        intent.resultDate().isPresent()
    }

    @Unroll
    def "should throw when trying to mark status twice (initial: #initialStatus, action: #action)"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("100.00")))
        Fee fee = new Fee(new BigDecimal("0.1"))

        switch (initialStatus) {
            case PurchaseStatus.SUCCESS -> intent.markAsSuccess(fee)
            case PurchaseStatus.CANCEL -> intent.markAsCancel()
            case PurchaseStatus.FAILURE -> intent.markAsFailure()
        }

        when:
        action.call(intent)

        then:
        def ex = thrown(IllegalStateException)
        ex.message == "Transaction cannot change it`s status twice."

        where:
        initialStatus         | action
        PurchaseStatus.SUCCESS | { it -> it.markAsCancel() }
        PurchaseStatus.SUCCESS | { it -> it.markAsFailure() }
        PurchaseStatus.SUCCESS | { it -> it.markAsSuccess(new Fee(BigDecimal.valueOf(0.1))) }
        PurchaseStatus.CANCEL  | { it -> it.markAsSuccess(new Fee(BigDecimal.valueOf(0.1))) }
        PurchaseStatus.CANCEL  | { it -> it.markAsFailure() }
        PurchaseStatus.FAILURE | { it -> it.markAsCancel() }
        PurchaseStatus.FAILURE | { it -> it.markAsSuccess(new Fee(BigDecimal.valueOf(0.1))) }
    }

    def "should create from repository with all fields preserved"() {
        given:
        UUID id = UUID.randomUUID()
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        long orderId = 123
        Amount amount = new Amount(new BigDecimal("100.00"))
        LocalDateTime creationDate = LocalDateTime.now().minusDays(1)
        LocalDateTime resultDate = LocalDateTime.now()
        Fee fee = new Fee(BigDecimal.valueOf(0.05))
        PurchaseStatus status = PurchaseStatus.SUCCESS

        when:
        def intent = CardPurchaseIntent.fromRepository(id, buyerID, null, orderId, amount, creationDate, resultDate, status, fee)

        then:
        intent.id() == id
        intent.buyerID() == buyerID
        intent.orderID() == orderId
        intent.totalPayedAmount() == amount
        intent.creationDate() == creationDate
        intent.resultDate().get() == resultDate
        intent.status() == status
        intent.removedFee().get() == fee
    }
}