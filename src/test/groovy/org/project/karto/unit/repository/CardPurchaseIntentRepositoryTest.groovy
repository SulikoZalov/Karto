package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.card.enumerations.PurchaseStatus
import org.project.karto.domain.card.value_objects.Fee
import org.project.karto.domain.common.value_objects.Amount
import org.project.karto.infrastructure.repository.JDBCCardPurchaseIntentRepository
import org.project.karto.infrastructure.repository.JDBCOrderIDRepository
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class CardPurchaseIntentRepositoryTest extends Specification {

    @Inject
    JDBCOrderIDRepository orderIDRepository;

    @Inject
    JDBCCardPurchaseIntentRepository repository

    def "should save and retrieve CardPurchaseIntent by ID"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("100.00")))
        repository.save(intent)

        when:
        def result = repository.findBy(intent.id())

        then:
        result.success()
        result.value().id() == intent.id()
        result.value().buyerID() == intent.buyerID()
        result.value().status() == PurchaseStatus.PENDING
    }

    def "should update CardPurchaseIntent status and fee"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("100.00")))
        repository.save(intent)

        when:
        Fee fee = new Fee(new BigDecimal("0.1"))
        intent.markAsSuccess(fee)
        repository.update(intent)
        def updated = repository.findBy(intent.id())

        then:
        updated.success()
        updated.value().status() == PurchaseStatus.SUCCESS
        updated.value().removedFee().get().rate() == fee.rate()
        updated.value().resultDate().isPresent()
    }

    def "should generate next order ID from sequence"() {
        when:
        def next1 = orderIDRepository.next()
        def next2 = orderIDRepository.next()

        then:
        next1.success()
        next2.success()
        next2.value() > next1.value()
    }

    def "should find by buyerID"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("50.00")))
        repository.save(intent)

        when:
        def found = repository.findBy(intent.buyerID())

        then:
        found.success()
        found.value().id().equals(intent.id())
    }

    def "should find by orderID"() {
        given:
        def intent = TestDataGenerator.generateCardPurchaseIntent(new Amount(new BigDecimal("70.00")))
        repository.save(intent)

        when:
        def found = repository.findBy(intent.orderID())

        then:
        found.success()
        found.value().id() == intent.id()
    }

    def "should handle findBy with non-existing ID"() {
        when:
        def result = repository.findBy(UUID.randomUUID())

        then:
        !result.success()
        result.value() == null
    }
}
