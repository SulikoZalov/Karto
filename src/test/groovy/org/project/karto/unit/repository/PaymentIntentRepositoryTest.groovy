package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.card.entities.PaymentIntent
import org.project.karto.domain.card.enumerations.PurchaseStatus
import org.project.karto.domain.card.value_objects.BuyerID
import org.project.karto.domain.card.value_objects.CardID
import org.project.karto.domain.card.value_objects.StoreID
import org.project.karto.domain.common.value_objects.Amount
import org.project.karto.infrastructure.repository.JDBCPaymentIntentRepository
import org.project.karto.util.PostgresTestResource
import spock.lang.Specification

import java.lang.reflect.Method

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(PostgresTestResource.class)
class PaymentIntentRepositoryTest extends Specification {

    @Inject
    JDBCPaymentIntentRepository repo

    def "successfully save and find by id"() {
        given:
        def payment = createPaymentIntent()

        when:
        def saveResult = repo.save(payment)

        then:
        saveResult.success()

        when:
        def findResult = repo.findBy(payment.id())

        then:
        findResult.success()
        with(findResult.value()) {
            id() == payment.id()
            buyerID() == payment.buyerID()
            cardID() == payment.cardID()
            storeID().orElse(null) == payment.storeID().orElse(null)
            orderID() == payment.orderID()
            totalAmount() == payment.totalAmount()
            status() == payment.status()
        }
    }

    def "successfully save and find by orderID"() {
        given:
        def payment = createPaymentIntent()

        when:
        def saveResult = repo.save(payment)

        then:
        saveResult.success()

        when:
        def findResult = repo.findBy(payment.orderID())

        then:
        findResult.success()
        findResult.value().id() == payment.id()
    }

    def "successfully update status and resultDate"() {
        given:
        def payment = createPaymentIntent()
        repo.save(payment)

        when:
        payment.markAsSuccess()
        def updateResult = repo.update(payment)

        then:
        updateResult.success()

        when:
        def findResult = repo.findBy(payment.id())

        then:
        findResult.success()
        findResult.value().status() == PurchaseStatus.SUCCESS
        findResult.value().resultDate() != null
    }

    def "fail find by non-existent id"() {
        when:
        def result = repo.findBy(UUID.randomUUID())

        then:
        !result.success()
    }

    def "fail find by non-existent orderID"() {
        when:
        def result = repo.findBy(-999L)

        then:
        !result.success()
    }

    private static PaymentIntent createPaymentIntent() {
        def buyerID = new BuyerID(UUID.randomUUID())
        def cardID = new CardID(UUID.randomUUID())
        def storeID = new StoreID(UUID.randomUUID())
        def orderID = System.currentTimeMillis()
        def amount = new Amount(1000L)

        Method ofMethod = PaymentIntent.getDeclaredMethod("of", BuyerID, CardID, StoreID, long, Amount)
        ofMethod.setAccessible(true)
        return (PaymentIntent) ofMethod.invoke(null, buyerID, cardID, storeID, orderID, amount)
    }
}