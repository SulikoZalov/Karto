package org.project.karto.unit.domain

import org.project.karto.domain.card.enumerations.GiftCardStatus
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
        def owner = new OwnerID(UUID.randomUUID());

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
        def ownerID = null

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
}