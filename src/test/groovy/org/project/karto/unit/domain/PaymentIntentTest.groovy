package org.project.karto.unit.domain

import org.project.karto.domain.card.entities.PaymentIntent
import org.project.karto.domain.card.enumerations.PurchaseStatus
import org.project.karto.domain.card.value_objects.*
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException
import org.project.karto.domain.common.value_objects.Amount
import spock.lang.Specification

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class PaymentIntentTest extends Specification {

    def "should create new PaymentIntent with status PENDING via of() reflection"() {
        given:
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        CardID cardID = new CardID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        long orderID = 123L
        Amount amount = new Amount(1000L)
        InternalFeeAmount fee = new InternalFeeAmount(10L)

        and: "Prepare reflection for PaymentIntent.of()"
        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount, InternalFeeAmount)
        ofMethod.setAccessible(true)

        when:
        PaymentIntent intent = (PaymentIntent) ofMethod.invoke(null, buyerID, cardID, storeID, orderID, amount, fee)

        then:
        intent.id() != null
        intent.buyerID() == buyerID
        intent.cardID() == cardID
        intent.storeID().get() == storeID
        intent.orderID() == orderID
        intent.totalAmount() == amount
        intent.creationDate() != null
        intent.resultDate().isEmpty()
        intent.status() == PurchaseStatus.PENDING
    }

    def "should fail when creating PaymentIntent with null BuyerID"() {
        given:
        CardID cardID = new CardID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        long orderID = 123L
        Amount amount = new Amount(1000L)
        InternalFeeAmount fee = new InternalFeeAmount(10L)

        and:
        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount, InternalFeeAmount)
        ofMethod.setAccessible(true)

        when:
        ofMethod.invoke(null, null, cardID, storeID, orderID, amount, fee)

        then:
        def ex = thrown(InvocationTargetException)
        ex.cause instanceof IllegalDomainArgumentException
        ex.cause.message == "BuyerID cannot be null"
    }

    def "should fail when creating PaymentIntent with null CardID"() {
        given:
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        long orderID = 123L
        Amount amount = new Amount(1000L)
        InternalFeeAmount fee = new InternalFeeAmount(10L)

        and:
        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount, InternalFeeAmount)
        ofMethod.setAccessible(true)

        when:
        ofMethod.invoke(null, buyerID, null, storeID, orderID, amount, fee)

        then:
        def ex = thrown(InvocationTargetException)
        ex.cause instanceof IllegalDomainArgumentException
        ex.cause.message == "CardID cannot be null"
    }

    def "should fail when creating PaymentIntent with null Amount"() {
        given:
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        CardID cardID = new CardID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        long orderID = 123L
        InternalFeeAmount fee = new InternalFeeAmount(10L)

        and:
        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount, InternalFeeAmount)
        ofMethod.setAccessible(true)

        when:
        ofMethod.invoke(null, buyerID, cardID, storeID, orderID, null, fee)

        then:
        def ex = thrown(InvocationTargetException)
        ex.cause instanceof IllegalDomainArgumentException
        ex.cause.message == "TotalAmount cannot be null"
    }

    def "should fail when creating PaymentIntent with non-positive orderID"() {
        given:
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        CardID cardID = new CardID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        Amount amount = new Amount(1000L)
        InternalFeeAmount fee = new InternalFeeAmount(10L)

        and:
        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount, InternalFeeAmount)
        ofMethod.setAccessible(true)

        when:
        ofMethod.invoke(null, buyerID, cardID, storeID, 0L, amount, fee)

        then:
        def ex = thrown(InvocationTargetException)
        ex.cause instanceof IllegalDomainArgumentException
        ex.cause.message == "OrderID cannot be lower than or equal zero"
    }

    def "should fail when creating PaymentIntent with null external fee amount"() {
        given:
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        CardID cardID = new CardID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        Amount amount = new Amount(1000L)
        InternalFeeAmount fee = null

        and:
        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount, InternalFeeAmount)
        ofMethod.setAccessible(true)

        when:
        ofMethod.invoke(null, buyerID, cardID, storeID, 2L, amount, fee)

        then:
        def ex = thrown(InvocationTargetException)
        ex.cause instanceof IllegalDomainArgumentException
        ex.cause.message == "Fee amount cannot be null"
    }

    def "should successfully change status from PENDING to SUCCESS"() {
        given:
        def payment = createValidPaymentIntent()
        ExternalPayeeDescription description = new ExternalPayeeDescription("some description")

        when:
        payment.markAsSuccess(description)

        then:
        payment.status() == PurchaseStatus.SUCCESS
        payment.resultDate() != null
    }

    def "should successfully change status from PENDING to CANCEL"() {
        given:
        def payment = createValidPaymentIntent()

        when:
        payment.markAsCancel()

        then:
        payment.status() == PurchaseStatus.CANCEL
        payment.resultDate() != null
    }

    def "should successfully change status from PENDING to FAILURE"() {
        given:
        def payment = createValidPaymentIntent()

        when:
        payment.markAsFailure()

        then:
        payment.status() == PurchaseStatus.FAILURE
        payment.resultDate() != null
    }

    def "should throw exception when trying to change status twice"() {
        given:
        def payment = createValidPaymentIntent()
        payment.markAsSuccess(new ExternalPayeeDescription("some description"))

        when:
        payment.markAsCancel()

        then:
        def ex = thrown(IllegalDomainArgumentException)
        ex.message == "You can`t change status twice"
    }

    def "should successfully confirm PaymentIntent after status change via reflection"() {
        given:
        def payment = createValidPaymentIntent()
        payment.markAsSuccess(new ExternalPayeeDescription("some description"))

        and:
        Method confirmMethod = PaymentIntent.getDeclaredMethod("confirm")
        confirmMethod.setAccessible(true)

        when:
        confirmMethod.invoke(payment)

        then:
        payment.isConfirmed()
    }

    def "should throw exception when trying to confirm PaymentIntent in PENDING status via reflection"() {
        given:
        def payment = createValidPaymentIntent()

        and:
        Method confirmMethod = PaymentIntent.getDeclaredMethod("confirm")
        confirmMethod.setAccessible(true)

        when:
        confirmMethod.invoke(payment)

        then:
        def ex = thrown(InvocationTargetException)
        ex.cause instanceof IllegalDomainArgumentException
        ex.cause.message == "You can`t confirm payment intent with PENDING status"
    }

    def "should throw exception when trying to confirm already confirmed PaymentIntent via reflection"() {
        given:
        def payment = createValidPaymentIntent()
        payment.markAsFailure()

        and:
        Method confirmMethod = PaymentIntent.getDeclaredMethod("confirm")
        confirmMethod.setAccessible(true)

        when: "First confirmation"
        confirmMethod.invoke(payment)

        and: "Second confirmation attempt"
        confirmMethod.invoke(payment)

        then:
        def ex = thrown(InvocationTargetException)
        ex.cause instanceof IllegalDomainArgumentException
        ex.cause.message == "PaymentIntent is already confirmed"
    }

    private static PaymentIntent createValidPaymentIntent() {
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        CardID cardID = new CardID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        long orderID = 123L
        Amount amount = new Amount(1000L)
        InternalFeeAmount fee = new InternalFeeAmount(10L)

        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount, InternalFeeAmount)
        ofMethod.setAccessible(true)
        return (PaymentIntent) ofMethod.invoke(null, buyerID, cardID, storeID, orderID, amount, fee)
    }
}
