package org.project.karto.unit.domain

import org.project.karto.domain.card.entities.PaymentIntent
import org.project.karto.domain.card.enumerations.PurchaseStatus
import org.project.karto.domain.card.value_objects.BuyerID
import org.project.karto.domain.card.value_objects.CardID
import org.project.karto.domain.card.value_objects.StoreID
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

        and: "Prepare reflection for PaymentIntent.of()"
        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount)
        ofMethod.setAccessible(true)

        when:
        PaymentIntent intent = (PaymentIntent) ofMethod.invoke(null, buyerID, cardID, storeID, orderID, amount)

        then:
        intent.id() != null
        intent.buyerID() == buyerID
        intent.cardID() == cardID
        intent.storeID().get() == storeID
        intent.orderID() == orderID
        intent.totalAmount() == amount
        intent.creationDate() != null
        intent.resultDate() == null
        intent.status() == PurchaseStatus.PENDING
    }

    def "should fail when creating PaymentIntent with null BuyerID"() {
        given:
        CardID cardID = new CardID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        long orderID = 123L
        Amount amount = new Amount(1000L)

        and:
        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount)
        ofMethod.setAccessible(true)

        when:
        ofMethod.invoke(null, null, cardID, storeID, orderID, amount)

        then:
        def ex = thrown(InvocationTargetException)
        ex.cause instanceof IllegalArgumentException
        ex.cause.message == "BuyerID cannot be null"
    }

    def "should fail when creating PaymentIntent with null CardID"() {
        given:
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        long orderID = 123L
        Amount amount = new Amount(1000L)

        and:
        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount)
        ofMethod.setAccessible(true)

        when:
        ofMethod.invoke(null, buyerID, null, storeID, orderID, amount)

        then:
        def ex = thrown(InvocationTargetException)
        ex.cause instanceof IllegalArgumentException
        ex.cause.message == "CardID cannot be null"
    }

    def "should fail when creating PaymentIntent with null Amount"() {
        given:
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        CardID cardID = new CardID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        long orderID = 123L

        and:
        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount)
        ofMethod.setAccessible(true)

        when:
        ofMethod.invoke(null, buyerID, cardID, storeID, orderID, null)

        then:
        def ex = thrown(InvocationTargetException)
        ex.cause instanceof IllegalArgumentException
        ex.cause.message == "TotalAmount cannot be null"
    }

    def "should fail when creating PaymentIntent with non-positive orderID"() {
        given:
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        CardID cardID = new CardID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        Amount amount = new Amount(1000L)

        and:
        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount)
        ofMethod.setAccessible(true)

        when:
        ofMethod.invoke(null, buyerID, cardID, storeID, 0L, amount)

        then:
        def ex = thrown(InvocationTargetException)
        ex.cause instanceof IllegalArgumentException
        ex.cause.message == "OrderID cannot be lower than or equal zero"
    }

    def "should successfully change status from PENDING to SUCCESS"() {
        given:
        def payment = createValidPaymentIntent()

        when:
        payment.markAsSuccess()

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
        payment.markAsSuccess()

        when:
        payment.markAsCancel()

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "You can`t change status twice"
    }

    private static PaymentIntent createValidPaymentIntent() {
        BuyerID buyerID = new BuyerID(UUID.randomUUID())
        CardID cardID = new CardID(UUID.randomUUID())
        StoreID storeID = new StoreID(UUID.randomUUID())
        long orderID = 123L
        Amount amount = new Amount(1000L)

        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount)
        ofMethod.setAccessible(true)
        return (PaymentIntent) ofMethod.invoke(null, buyerID, cardID, storeID, orderID, amount)
    }
}
