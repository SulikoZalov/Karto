package org.project.karto.unit.domain


import org.project.karto.domain.card.entities.GiftCard
import org.project.karto.domain.card.enumerations.GiftCardRecipientType
import org.project.karto.domain.card.enumerations.GiftCardStatus
import org.project.karto.domain.card.enumerations.GiftCardType
import org.project.karto.domain.card.enumerations.PaymentType
import org.project.karto.domain.card.events.CashbackEvent
import org.project.karto.domain.card.value_objects.*
import org.project.karto.domain.common.value_objects.Amount
import org.project.karto.domain.common.value_objects.KeyAndCounter
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification
import spock.lang.Unroll

import java.math.RoundingMode
import java.time.LocalDateTime

class GiftCardTest extends Specification {

    def "should create self-bought card with correct initial values"() {
        when:
        def card = TestDataGenerator.generateSelfBougthGiftCard()

        then:
        card.buyerID().value() == card.ownerID().get().value()
        card.giftCardType() == GiftCardType.STORE_SPECIFIC
        card.countOfUses() == 0
        card.storeID().isPresent()
        card.giftCardStatus() == GiftCardStatus.PENDING
        !card.isVerified()
    }

    def "should create bought-as-gift card with correct initial values"() {
        when:
        def card = TestDataGenerator.generateBoughtAsGiftCard()

        then:
        card.ownerID().isEmpty()
        card.giftCardType() == GiftCardType.STORE_SPECIFIC
        card.storeID().isPresent()
        card.countOfUses() == 0
        card.giftCardStatus() == GiftCardStatus.PENDING
        !card.isVerified()
    }

    def "should create self-bought common type gift card with correct initial values"() {
        when:
        def card = TestDataGenerator.generateSelfBoughtCommonGiftCard()

        then:
        card.storeID().isEmpty()
        card.buyerID().value() == card.ownerID().get().value()
        card.giftCardType() == GiftCardType.COMMON
        card.countOfUses() == 0
        card.giftCardStatus() == GiftCardStatus.PENDING
        !card.isVerified()
    }

    def "should create bought-as-gift common type gift card with correct initial values"() {
        when:
        def card = TestDataGenerator.generateBoughtAsGiftCommonCard()

        then:
        card.storeID().isEmpty()
        card.ownerID().isEmpty()
        card.giftCardType() == GiftCardType.COMMON
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

    def "should create self-bought store-specific gift card with correct initial state"() {
        given: "Test data"
        def buyerID = TestDataGenerator.generateBuyerID()
        def balance = TestDataGenerator.generateBalance()
        def storeID = new StoreID(UUID.randomUUID())
        def secretKey = TestDataGenerator.generateSecretKey()
        def cardLimits = TestDataGenerator.generateCardLimits()

        when: "Creating a self-bought store-specific gift card"
        def giftCard = GiftCard.selfBoughtCard(buyerID, balance, storeID, secretKey, cardLimits)

        then: "The card should have correct initial state"
        giftCard.id() != null
        giftCard.buyerID() == buyerID
        giftCard.ownerID().isPresent()
        giftCard.ownerID().get().value() == buyerID.value()
        giftCard.storeID().isPresent()
        giftCard.storeID().get() == storeID
        giftCard.maxCountOfUses() == cardLimits.maxUsageCount()
        giftCard.giftCardStatus() == GiftCardStatus.PENDING
        giftCard.balance() == balance
        giftCard.countOfUses() == 0
        giftCard.keyAndCounter().key() == secretKey
        giftCard.keyAndCounter().counter() == 0
        giftCard.creationDate().isBefore(LocalDateTime.now().plusSeconds(1))
        giftCard.lastUsage() == giftCard.creationDate()
        giftCard.version() == 1
        giftCard.oldVersion() == 1
        giftCard.recipientType() == GiftCardRecipientType.SELF
        giftCard.giftCardType() == GiftCardType.STORE_SPECIFIC
    }

    def "should create gift card for someone else with correct initial state"() {
        given: "Test data"
        def buyerID = TestDataGenerator.generateBuyerID()
        def balance = TestDataGenerator.generateBalance()
        def storeID = new StoreID(UUID.randomUUID())
        def secretKey = TestDataGenerator.generateSecretKey()
        def cardLimits = TestDataGenerator.generateCardLimits()

        when: "Creating a gift card for someone else"
        def giftCard = GiftCard.boughtAsAGift(buyerID, balance, storeID, secretKey, cardLimits)

        then: "The card should have correct initial state"
        giftCard.id() != null
        giftCard.buyerID() == buyerID
        !giftCard.ownerID().isPresent()
        giftCard.storeID().isPresent()
        giftCard.storeID().get() == storeID
        giftCard.maxCountOfUses() == cardLimits.maxUsageCount()
        giftCard.giftCardStatus() == GiftCardStatus.PENDING
        giftCard.balance() == balance
        giftCard.countOfUses() == 0
        giftCard.keyAndCounter().key() == secretKey
        giftCard.keyAndCounter().counter() == 0
        giftCard.creationDate().isBefore(LocalDateTime.now().plusSeconds(1))
        giftCard.lastUsage() == giftCard.creationDate()
        giftCard.version() == 1
        giftCard.oldVersion() == 1
        giftCard.recipientType() == GiftCardRecipientType.OTHER
        giftCard.giftCardType() == GiftCardType.STORE_SPECIFIC
    }

    def "should create self-bought common gift card with correct initial state"() {
        given: "Test data"
        def buyerID = TestDataGenerator.generateBuyerID()
        def balance = TestDataGenerator.generateBalance()
        def secretKey = TestDataGenerator.generateSecretKey()
        def cardLimits = TestDataGenerator.generateCardLimits()

        when: "Creating a self-bought common gift card"
        def giftCard = GiftCard.selfBoughtCommonCard(buyerID, balance, secretKey, cardLimits)

        then: "The card should have correct initial state"
        giftCard.id() != null
        giftCard.buyerID() == buyerID
        giftCard.ownerID().isPresent()
        giftCard.ownerID().get().value() == buyerID.value()
        !giftCard.storeID().isPresent()
        giftCard.maxCountOfUses() == cardLimits.maxUsageCount()
        giftCard.giftCardStatus() == GiftCardStatus.PENDING
        giftCard.balance() == balance
        giftCard.countOfUses() == 0
        giftCard.keyAndCounter().key() == secretKey
        giftCard.keyAndCounter().counter() == 0
        giftCard.creationDate().isBefore(LocalDateTime.now().plusSeconds(1))
        giftCard.version() == 1
        giftCard.oldVersion() == 1
        giftCard.recipientType() == GiftCardRecipientType.SELF
        giftCard.giftCardType() == GiftCardType.COMMON
    }

    def "should create common gift card for someone else with correct initial state"() {
        given: "Test data"
        def buyerID = TestDataGenerator.generateBuyerID()
        def balance = TestDataGenerator.generateBalance()
        def secretKey = TestDataGenerator.generateSecretKey()
        def cardLimits = TestDataGenerator.generateCardLimits()

        when: "Creating a common gift card for someone else"
        def giftCard = GiftCard.giftedCommonCard(buyerID, balance, secretKey, cardLimits)

        then: "The card should have correct initial state"
        giftCard.id() != null
        giftCard.buyerID() == buyerID
        !giftCard.ownerID().isPresent()
        !giftCard.storeID().isPresent()
        giftCard.maxCountOfUses() == cardLimits.maxUsageCount()
        giftCard.giftCardStatus() == GiftCardStatus.PENDING
        giftCard.balance() == balance
        giftCard.countOfUses() == 0
        giftCard.keyAndCounter().key() == secretKey
        giftCard.keyAndCounter().counter() == 0
        giftCard.creationDate().isBefore(LocalDateTime.now().plusSeconds(1))
        giftCard.version() == 1
        giftCard.oldVersion() == 1
        giftCard.recipientType() == GiftCardRecipientType.OTHER
        giftCard.giftCardType() == GiftCardType.COMMON
    }

    @Unroll
    def "should throw IllegalArgumentException when creating gift card with invalid parameters: #scenario"() {
        when: "Creating a gift card with invalid parameters"
        creationMethod.call()

        then: "An IllegalArgumentException should be thrown"
        thrown(IllegalArgumentException)

        where:
        scenario                              | creationMethod
        "null buyerID"                        | { -> GiftCard.selfBoughtCard(null, TestDataGenerator.generateBalance(), new StoreID(UUID.randomUUID()), TestDataGenerator.generateSecretKey(), TestDataGenerator.generateCardLimits()) }
        "null balance"                        | { -> GiftCard.selfBoughtCard(TestDataGenerator.generateBuyerID(), null, new StoreID(UUID.randomUUID()), TestDataGenerator.generateSecretKey(), TestDataGenerator.generateCardLimits()) }
        "null secretKey"                      | { -> GiftCard.selfBoughtCard(TestDataGenerator.generateBuyerID(), TestDataGenerator.generateBalance(), new StoreID(UUID.randomUUID()), null, TestDataGenerator.generateCardLimits()) }
        "null cardLimits"                     | { -> GiftCard.selfBoughtCard(TestDataGenerator.generateBuyerID(), TestDataGenerator.generateBalance(), new StoreID(UUID.randomUUID()), TestDataGenerator.generateSecretKey(), null) }
        "null storeID for store-specific"     | { -> GiftCard.selfBoughtCard(TestDataGenerator.generateBuyerID(), TestDataGenerator.generateBalance(), null, TestDataGenerator.generateSecretKey(), TestDataGenerator.generateCardLimits()) }
    }

    def "should activate self-bought gift card successfully"() {
        given: "A self-bought gift card"
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard()

        when: "Activating the card"
        giftCard.activate()

        then: "The card should be activated"
        giftCard.giftCardStatus() == GiftCardStatus.ACTIVE
        giftCard.keyAndCounter().counter() == 1
        giftCard.version() == 2
        giftCard.isVerified()
    }

    def "should activate gifted card with ownerID successfully"() {
        given: "A gifted card and an ownerID"
        def giftCard = TestDataGenerator.generateBoughtAsGiftCard()
        def ownerID = TestDataGenerator.generateOwnerID()

        when: "Activating the card with ownerID"
        giftCard.activate(ownerID)

        then: "The card should be activated with the new owner"
        giftCard.giftCardStatus() == GiftCardStatus.ACTIVE
        giftCard.ownerID().isPresent()
        giftCard.ownerID().get() == ownerID
        giftCard.keyAndCounter().counter() == 1
        giftCard.version() == 2
        giftCard.isVerified()
    }

    def "should initialize transaction successfully when conditions are met"() {
        given: "An active gift card with sufficient balance"
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(new Balance(new BigDecimal("1000")))
        giftCard.activate()
        def amount = new Amount(new BigDecimal("500"))
        def orderID = 12345L

        when: "Initializing a transaction"
        def paymentIntent = giftCard.initializeTransaction(amount, orderID)

        then: "A payment intent should be created"
        paymentIntent != null
        paymentIntent.cardID() == giftCard.id()
        paymentIntent.buyerID() == giftCard.buyerID()
        paymentIntent.storeID().get() == giftCard.storeID().get()
        paymentIntent.orderID() == orderID
        paymentIntent.totalAmount().value() == amount.value().add(amount.value() * GiftCard.KARTO_COMMON_CARD_FEE_RATE)
        paymentIntent.feeAmount().value() == amount.value() * GiftCard.KARTO_COMMON_CARD_FEE_RATE
        !paymentIntent.isConfirmed()
        giftCard.countOfUses() == 1
        giftCard.version() == 3
    }

    @Unroll
    def "should throw exception when initializing transaction with invalid conditions: #scenario"() {
        given: "A gift card in specific state"
        def giftCard = initialCard.call()

        when: "Initializing a transaction"
        giftCard.initializeTransaction(amount, 12345L)

        then: "An exception should be thrown"
        thrown(expectedException)

        where:
        scenario                              | initialCard                                                                 | amount                    | expectedException
        "null amount"                         | {
            def c = TestDataGenerator.generateSelfBougthGiftCard()
            c.activate()
            return c
        }                                                                 | null                      | IllegalArgumentException
        "not activated card"                  | {
            TestDataGenerator.generateSelfBougthGiftCard()
        }                                                                 | new Amount(BigDecimal.TEN) | IllegalStateException
        "max uses reached"                    | {
            def c = TestDataGenerator.generateSelfBougthGiftCard(new Balance(new BigDecimal("1000")), 1)
            c.activate()
            c.initializeTransaction(new Amount(new BigDecimal("500")), 1)
            return c
        }                                                                 | new Amount(new BigDecimal("500")) | IllegalArgumentException
        "insufficient balance"                | {
            def c = TestDataGenerator.generateSelfBougthGiftCard(new Balance(new BigDecimal("100")))
            c.activate()
            return c
        }                                                                 | new Amount(new BigDecimal("200")) | IllegalArgumentException
    }

    def "should apply transaction successfully and calculate cashback"() {
        given: "An active gift card with payment intent"
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(new Balance(new BigDecimal("1000")))
        giftCard.activate()
        def amount = new Amount(new BigDecimal("500"))
        def paymentIntent = giftCard.initializeTransaction(amount, 12345L)
        paymentIntent.markAsSuccess(new ExternalPayeeDescription("desc"))

        and: "User activity snapshot"
        def userID = giftCard.ownerID().get().value()
        def activitySnapshot = new UserActivitySnapshot(
                userID,
                new BigDecimal("5000"),
                1000,
                LocalDateTime.now(),
                10
        )

        when: "Applying the transaction"
        def check = giftCard.applyTransaction(
                paymentIntent,
                activitySnapshot,
                Currency.getInstance("USD"),
                PaymentType.KARTO_PAYMENT,
                new PaymentSystem("UP")
        )

        then: "The transaction should be applied successfully"
        check != null
        check.orderID() == 12345L
        check.buyerID() == giftCard.buyerID()
        check.storeID().get() == giftCard.storeID().get()
        check.cardID().get() == giftCard.id()
        check.totalAmount() == paymentIntent.totalAmount()
        check.currency() == new Currency("USD")
        check.paymentType() == PaymentType.KARTO_PAYMENT
        check.internalFee() == paymentIntent.feeAmount()
        check.paymentSystem() == new PaymentSystem("UP")
        check.description() == new ExternalPayeeDescription("desc")

        and: "The gift card balance should be updated"
        giftCard.balance().value() == new BigDecimal("1000").subtract(paymentIntent.totalAmount().value())

        and: "The last usage date should be updated"
        giftCard.lastUsage().isAfter(giftCard.creationDate())

        and: "A cashback event should be generated"
        def events = giftCard.pullEvents()
        events.size() == 1
        events[0] instanceof CashbackEvent
        (events[0] as CashbackEvent).cardID() == giftCard.id()
        (events[0] as CashbackEvent).ownerID() == giftCard.ownerID().get()

        and: "Version should be incremented"
        giftCard.version() == 4
    }

    def "should throw exception when applying transaction with invalid conditions"() {
        given: "An active gift card"
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(new Balance(new BigDecimal("1000")))
        giftCard.activate()

        and: "A payment intent"
        def paymentIntent = giftCard.initializeTransaction(new Amount(new BigDecimal("500")), 12345L)

        and: "User activity snapshot"
        def userID = giftCard.ownerID().get().value()
        def activitySnapshot = new UserActivitySnapshot(
                userID,
                new BigDecimal("5000"),
                1000,
                LocalDateTime.now(),
                10
        )

        when: "Trying to apply transaction with invalid parameters"
        giftCard.applyTransaction(
                invalidIntent.call(giftCard, paymentIntent),
                activitySnapshot,
                Currency.getInstance("USD"),
                PaymentType.KARTO_PAYMENT,
                new PaymentSystem("UP")
        )

        then: "An exception should be thrown"
        thrown(expectedException)

        where:
        scenario                              | invalidIntent                                                                 | expectedException
        "null payment intent"                | { g, p -> null }                                                             | IllegalArgumentException
        "intent for different card"          | { g, p ->
            def otherCard = TestDataGenerator.generateSelfBougthGiftCard()
            otherCard.activate()
            otherCard.initializeTransaction(new Amount(new BigDecimal("100")), 54321)
        }                                                                   | IllegalArgumentException
        "userID mismatch"                    | { g, p -> p }                                                                 | IllegalArgumentException
    }

    def "should calculate cashback correctly with maximum cap"() {
        given: "An active gift card with payment intent"
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(new Balance(new BigDecimal("10000")))
        giftCard.activate()
        def amount = new Amount(new BigDecimal("1000"))
        def paymentIntent = giftCard.initializeTransaction(amount, 12345L)
        paymentIntent.markAsSuccess(new ExternalPayeeDescription("desc"))

        and: "User activity snapshot with very high values to trigger max cashback"
        def userID = giftCard.ownerID().get().value()
        def activitySnapshot = new UserActivitySnapshot(
                userID,
                new BigDecimal("5000"),
                1000,
                LocalDateTime.now(),
                10
        )

        when: "Applying the transaction"
        giftCard.applyTransaction(
                paymentIntent,
                activitySnapshot,
                Currency.getInstance("USD"),
                PaymentType.KARTO_PAYMENT,
                new PaymentSystem("UP")
        )

        and: "Getting cashback event"
        def events = giftCard.pullEvents()
        def cashback = (events[0] as CashbackEvent).amount()

        then: "The cashback should be capped at MAX_CASHBACK_RATE"
        cashback == (paymentIntent.totalAmount().value() * GiftCard.MAX_CASHBACK_RATE).setScale(2, RoundingMode.HALF_UP)
    }

    def "should not charge fee for common gift cards"() {
        given: "An active common gift card"
        def giftCard = TestDataGenerator.generateSelfBoughtCommonGiftCard(new Balance(new BigDecimal("1000")))
        giftCard.activate()
        def amount = new Amount(new BigDecimal("500"))
        def orderID = 12345L

        when: "Initializing a transaction"
        def paymentIntent = giftCard.initializeTransaction(amount, orderID)

        then: "No fee should be charged"
        paymentIntent.feeAmount().value() == BigDecimal.ZERO
        paymentIntent.totalAmount() == amount
    }

    def "should charge fee for store-specific gift cards"() {
        given: "An active store-specific gift card"
        def giftCard = TestDataGenerator.generateSelfBougthGiftCard(new Balance(new BigDecimal("1000")))
        giftCard.activate()
        def amount = new Amount(new BigDecimal("500"))
        def orderID = 12345L

        when: "Initializing a transaction"
        def paymentIntent = giftCard.initializeTransaction(amount, orderID)

        then: "Fee should be charged"
        paymentIntent.feeAmount().value() == amount.value() * GiftCard.KARTO_COMMON_CARD_FEE_RATE
        paymentIntent.totalAmount().value() == amount.value().add(amount.value() * GiftCard.KARTO_COMMON_CARD_FEE_RATE)
    }

    def "should restore gift card from repository with correct state"() {
        given: "Gift card parameters"
        def cardID = new CardID(UUID.randomUUID())
        def buyerID = TestDataGenerator.generateBuyerID()
        def ownerID = TestDataGenerator.generateOwnerID()
        def storeID = new StoreID(UUID.randomUUID())
        def status = GiftCardStatus.ACTIVE
        def balance = TestDataGenerator.generateBalance()
        def countOfUses = 3
        def maxCountOfUses = 10
        def keyAndCounter = new KeyAndCounter(TestDataGenerator.generateSecretKey(), 2)
        def creationDate = LocalDateTime.now().minusDays(5)
        def expirationDate = creationDate.plusDays(30)
        def lastUsage = LocalDateTime.now().minusDays(1)
        def version = 5L

        when: "Restoring gift card from repository"
        def giftCard = GiftCard.fromRepository(
                cardID, buyerID, ownerID, storeID, status, balance,
                countOfUses, maxCountOfUses, keyAndCounter,
                creationDate, expirationDate, lastUsage, version
        )

        then: "The card should have correct restored state"
        giftCard.id() == cardID
        giftCard.buyerID() == buyerID
        giftCard.ownerID().get() == ownerID
        giftCard.storeID().get() == storeID
        giftCard.giftCardStatus() == status
        giftCard.balance() == balance
        giftCard.countOfUses() == countOfUses
        giftCard.maxCountOfUses() == maxCountOfUses
        giftCard.keyAndCounter() == keyAndCounter
        giftCard.creationDate() == creationDate
        giftCard.expirationDate() == expirationDate
        giftCard.lastUsage() == lastUsage
        giftCard.version() == version
        giftCard.oldVersion() == version
    }
}