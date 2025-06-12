package org.project.karto.unit.domain

import org.project.karto.domain.card.enumerations.GiftCardStatus
import org.project.karto.domain.card.value_objects.Amount
import org.project.karto.domain.card.value_objects.Balance
import org.project.karto.domain.card.value_objects.OwnerID
import org.project.karto.util.TestDataGenerator
import spock.lang.*

class GiftCardTest extends Specification {

    def "should create self-bought card with correct initial values"() {
        when:
        def card = TestDataGenerator.generateSelfBougthGiftCard()

        then:
        card.buyerID().value() == card.ownerID().value()
        card.countOfUses() == 0
        card.giftCardStatus() == GiftCardStatus.PENDING
        !card.isVerified()
    }

    def "should create bought-as-gift card with correct initial values"() {
        when:
        def card = TestDataGenerator.generateBoughtAsGiftCard()

        then:
        card.ownerID() == null
        card.countOfUses() == 0
        card.giftCardStatus() == GiftCardStatus.PENDING
        !card.isVerified()
    }

    def "should activate pending self bought card"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard()

        when:
        card.activate()

        then:
        card.giftCardStatus() == GiftCardStatus.ACTIVE
        card.isVerified()
        card.keyAndCounter().counter() == 1
    }

    def "should activate pending bought as a gift card"() {
        given:
        def card = TestDataGenerator.generateBoughtAsGiftCard()
        def ownerID = new OwnerID(UUID.randomUUID())

        when:
        card.activate(ownerID)

        then:
        card.giftCardStatus() == GiftCardStatus.ACTIVE
        card.isVerified()
        card.keyAndCounter().counter() == 1
    }

    def "should thrown an exception when invalid activation of self bought card: should provide owner id"() {
        given:
        def card = TestDataGenerator.generateBoughtAsGiftCard()

        when:
        card.activate()

        then:
        def e = thrown(IllegalStateException)
        e.getMessage() == "You can`t activate account without owner id."
    }

    def "should thrown an exception: invalid owner id"() {
        given:
        def card = TestDataGenerator.generateBoughtAsGiftCard()

        when:
        card.activate(null)

        then:
        def e = thrown(IllegalArgumentException)
        e.getLocalizedMessage() == "You can`t activate account without owner id."
    }

    def "should thrown an exception: you cant enable already active card"() {
        given:
        def card = TestDataGenerator.generateBoughtAsGiftCard()
        def ownerID = new OwnerID(UUID.randomUUID())

        when:
        card.activate(ownerID)
        card.activate(ownerID)

        then:
        def e = thrown(IllegalStateException)
        e.getMessage() == "You can`t enable already active card."
    }

    def "should thrown an exception: you cant change owner id"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard()
        def newOwnerID = new OwnerID(UUID.randomUUID())

        when:
        card.activate()
        card.activate(newOwnerID)

        then:
        thrown(IllegalStateException)
    }

    def "should thrown an exception: buyer can`t be an owner when card bought as a gift"() {
        given:
        def card = TestDataGenerator.generateBoughtAsGiftCard()
        def ownerID = new OwnerID(card.buyerID().value())

        when:
        card.activate(ownerID)

        then:
        def e = thrown(IllegalStateException)
        e.getMessage() == "The card was purchased as a gift, the owner's ID cannot be equal to the buyer's ID"
    }

    def "should successfully spend money from self bought card"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(new Balance(BigDecimal.valueOf(100L)))
        card.activate()
        def amount = new Amount(BigDecimal.valueOf(50L))

        when:
        card.spend(amount)

        then:
        card.balance().value() == BigDecimal.valueOf(50L)
        card.countOfUses() == 1
    }

    def "should successfully spend money from bought as gift card"() {
        given:
        def card = TestDataGenerator.generateBoughtAsGiftCard(new Balance(BigDecimal.valueOf(100L)))
        card.activate(new OwnerID(UUID.randomUUID()))
        def amount = new Amount(BigDecimal.valueOf(50L))

        when:
        card.spend(amount)

        then:
        card.balance().value() == BigDecimal.valueOf(50L)
        card.countOfUses() == 1
    }

    def "should thrown an exception: can`t spend from unverified card"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(new Balance(BigDecimal.valueOf(100L)))
        def amount = TestDataGenerator.generateAmount(BigDecimal.valueOf(100L))

        when:
        card.spend(amount)

        then:
        def e = thrown(IllegalStateException)
        e.getMessage() == "Card is not activated"
    }

    def "should thrown an exception: has no sufficient balance"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(new Balance(BigDecimal.valueOf(100L)))
        card.activate()
        def amount = new Amount(BigDecimal.valueOf(120L))

        when:
        card.spend(amount)

        then:
        def e = thrown(IllegalArgumentException)
        e.getMessage() == "There is not enough money on the balance"
    }

    def "should throw if card reached max count of uses"() {
        given:
        def card = TestDataGenerator.generateSelfBougthGiftCard(new Balance(BigDecimal.valueOf(100L)), 3)
        card.activate()
        Amount amount = new Amount(BigDecimal.valueOf(1L))

        when:
        card.spend(amount)
        card.spend(amount)
        card.spend(amount)
        card.spend(amount)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Card reached max count of uses"
    }
}