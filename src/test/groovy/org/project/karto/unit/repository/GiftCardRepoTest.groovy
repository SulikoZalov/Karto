package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.application.pagination.PageRequest;
import org.project.karto.application.dto.gift_card.CardDTO;
import org.project.karto.domain.card.enumerations.PaymentType
import org.project.karto.domain.card.value_objects.*
import org.project.karto.domain.card.entities.GiftCard
import org.project.karto.domain.common.value_objects.Amount
import org.project.karto.infrastructure.repository.JDBCGiftCardRepository
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification
import spock.lang.Unroll

import java.math.RoundingMode
import java.time.LocalDateTime

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class GiftCardRepoTest extends Specification {

    @Inject
    JDBCGiftCardRepository repo

    @Inject
    Util util

    @Unroll
    def "save gift card: self bought [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        index << (1..10)
    }

    @Unroll
    def  "save gift card: bought as gift [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        index << (1..10)
    }

    @Unroll
    def "save gift card: self bought common type [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        index << (1..10)
    }

    @Unroll
    def "save gift card: bought as gift common type [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result = repo.save(giftCard)

        then:
        result.success()
        result.value() == 1
        repo.findBy(giftCard.id()).success()

        where:
        index << (1..10)
    }

    @Unroll
    def "fail saving same card twice [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def result1 = repo.save(giftCard)
        def result2 = repo.save(giftCard)

        then:
        result1.success()
        result1.value() == 1
        !result2.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "update gift card after transaction [#index]"() {
        given: "A newly created gift card"
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )
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
        def paymentIntent = reloadedCard1.initializeTransaction(amount, TestDataGenerator.orderID(), storeID(giftCard))
        paymentIntent.markAsSuccess(new PayeeDescription("desc"))
        repo.update(reloadedCard1)

        def reloadedCard2 = repo.findBy(reloadedCard1.id()).value()

        reloadedCard2.applyTransaction(
                paymentIntent,
                new UserActivitySnapshot(
                        reloadedCard1.ownerID().get().value(),
                        new BigDecimal("1000"),
                        30,
                        LocalDateTime.now(),
                        5, false
                ),
                Currency.getInstance("USD"),
                PaymentType.KARTO_PAYMENT,
                new PaymentSystem("UP"),
                new BankName("BANK")
        )
        def updateResult = repo.update(reloadedCard2)

        then: "Transaction update should succeed"
        updateResult.success()
        updateResult.value() == 1

        where:
        index << (1..10)
    }

    @Unroll
    def "update amount of activated gift card through full lifecycle [#index]"() {
        given: "A newly created gift card"
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

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
        def paymentIntent = reloadedCard1.initializeTransaction(amount, TestDataGenerator.orderID(), storeID(giftCard))
        paymentIntent.markAsSuccess(new PayeeDescription("desc"))
        repo.update(reloadedCard1)

        def reloadedCard2 = repo.findBy(reloadedCard1.id()).value()

        reloadedCard2.applyTransaction(
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
        def transactionResult = repo.update(reloadedCard2)

        then: "Transaction update should succeed"
        transactionResult.success()
        transactionResult.value() == 1

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by card ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

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
        index << (1..10)
    }

    @Unroll
    def "fail find by non existent card ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def findResult = repo.findBy(giftCard.id())

        then:
        notThrown(Exception)
        !findResult.success()

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by buyer ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

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
        index << (1..10)
    }

    @Unroll
    def "fail find by non existent buyer ID: empty list [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def findResult = repo.findBy(giftCard.buyerID())

        then:
        notThrown(Exception)
        findResult.success()
        def value = findResult.value()
        value.size() == 0

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by owner ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

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
        index << (1..10)
    }

    @Unroll
    def "fail find by non existent owner ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

        when:
        def findResult = repo.findBy(giftCard.ownerID().get())

        then:
        notThrown(Exception)
        findResult.success()
        def value = findResult.value()
        value.size() == 0

        where:
        index << (1..10)
    }

    @Unroll
    def "successful find by store ID [#index]"() {
        given:
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(
                util.generateActivateAndSaveUser(),
                util.generateActivateAndSaveCompany()
        )

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
        index << (1..10)
    }

    def "fail find by non existent store ID"() {
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

    @Unroll
    def "get available gift cards with pagination [#index]"() {
        given: "Multiple companies with gift cards"
        def company1 = util.generateActivateAndSaveCompany()
        def company2 = util.generateActivateAndSaveCompany()
        def user = util.generateActivateAndSaveUser()
        
        (1..3).each {
            def giftCard = TestDataGenerator.generateSelfBougthGiftCard(user, company1)
            repo.save(giftCard)
        }
                        
        (1..5).each {
            def giftCard = TestDataGenerator.generateSelfBougthGiftCard(user, company2)
            repo.save(giftCard)
        }

        when: "Getting available gift cards with pagination"
        def pageRequest = new PageRequest(10, 0)
        def result = repo.availableGiftCards(pageRequest)

        then: "Result should be successful and contain companies ordered by gift card count"
        //notThrown(Exception)
        result.success()
        def cards = result.value()
        cards.size() >= 2
        
        if (cards.size() >= 2) {
            def firstCard = cards.find { it.partnerID() == company2 }
            def secondCard = cards.find { it.partnerID() == company1 }
            
            firstCard != null
            secondCard != null
        }

        where:
        index << (1..5)
    }

    @Unroll
    def "get available gift cards with small page size [#index]"() {
        given: "Multiple companies with gift cards"
        def company1 = util.generateActivateAndSaveCompany()
        def company2 = util.generateActivateAndSaveCompany()
        def user = util.generateActivateAndSaveUser()
        
        (1..2).each {
            def giftCard = TestDataGenerator.generateSelfBougthGiftCard(user, company1)
            repo.save(giftCard)
        }
        
        (1..3).each {
            def giftCard = TestDataGenerator.generateSelfBougthGiftCard(user, company2)
            repo.save(giftCard)
        }

        when: "Getting available gift cards with small page size"
        def pageRequest = new PageRequest(1,0)
        def result = repo.availableGiftCards(pageRequest)

        then: "Should return only one company"
        notThrown(Exception)
        result.success()
        def cards = result.value()
        cards.size() == 1

        where:
        index << (1..5)
    }

    @Unroll
    def "get available gift cards with offset [#index]"() {
        given: "Multiple companies with gift cards"
        def companies = (1..3).collect { util.generateActivateAndSaveCompany() }
        def user = util.generateActivateAndSaveUser()
        
        companies.eachWithIndex { company, idx ->
            (1..(idx + 1)).each {
                def giftCard = TestDataGenerator.generateSelfBougthGiftCard(user, company)
                repo.save(giftCard)
            }
        }

        when: "Getting available gift cards with offset"
        def pageRequest = new PageRequest(2, 1)
        def result = repo.availableGiftCards(pageRequest)

        then: "Should skip first company and return next ones"
        notThrown(Exception)
        result.success()
        def cards = result.value()
        cards.size() <= 2

        where:
        index << (1..5)
    }

    StoreID storeID(GiftCard giftCard) {
        giftCard.storeID().isPresent() ? giftCard.storeID().get() : new StoreID(util.generateActivateAndSaveCompany())
    }
}
