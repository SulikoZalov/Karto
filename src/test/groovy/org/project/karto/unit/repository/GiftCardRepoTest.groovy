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
        repo.findBy(giftCard.id()).success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "save gift card: bought as gift"() {
        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        repo.findBy(giftCard.id()).success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateBoughtAsGiftCard()})
    }

    void "save gift card: self bought common type"() {
        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        repo.findBy(giftCard.id()).success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBoughtCommonGiftCard()})
    }

    void "save gift card: bought as gift common type"() {
        when:
        def result = repo.save(giftCard)

        then:
        result.success()
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
        updateResult1.success()
        updateResult2.success()

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }

    void "successful find by card ID"() {
        when:
        def result = repo.save(giftCard)

        then:
        result.success()

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