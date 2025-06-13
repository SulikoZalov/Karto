package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.card.value_objects.Amount
import org.project.karto.infrastructure.repository.JDBCGiftCardRepository
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

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

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "fail saving same card twice"() {
        when:
        def result1 = repo.save(giftCard)
        def result2 = repo.save(giftCard)

        then:
        result1.success()
        !result2.success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }


    void "update gift card"() {
        when:
        giftCard.activate()
        def result = repo.save(giftCard)

        then:
        result.success()

        when:
        giftCard.spend(new Amount(BigDecimal.valueOf(giftCard.balance().value() / 10)))
        def updateResult = repo.update(giftCard)

        then:
        notThrown(Exception)
        updateResult.success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "update amount of activated gift card"() {
        when:
        def result = repo.save(giftCard)

        then:
        result.success()

        when:
        giftCard.activate()
        def activationResult = repo.update(giftCard)

        then:
        activationResult.success()

        when:
        giftCard.spend(new Amount(BigDecimal.valueOf(giftCard.balance().value() / 10)))
        def updateResult = repo.update(giftCard)

        then:
        updateResult.success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "update gift card twice"() {
        when:
        giftCard.activate()
        def result = repo.save(giftCard)

        then:
        result.success()

        when:
        giftCard.spend(new Amount(BigDecimal.valueOf(giftCard.balance().value() / 10)))
        def updateResult1 = repo.update(giftCard)
        def updateResult2 = repo.update(giftCard)

        then:
        notThrown(Exception)

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }
}