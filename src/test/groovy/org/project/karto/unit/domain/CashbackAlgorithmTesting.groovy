package org.project.karto.unit.domain

import org.project.karto.domain.card.entities.GiftCard
import org.project.karto.domain.card.enumerations.PaymentType
import org.project.karto.domain.card.events.CashbackEvent
import org.project.karto.domain.card.value_objects.*
import org.project.karto.domain.common.value_objects.Amount
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

import java.math.RoundingMode
import java.time.LocalDateTime
import java.util.concurrent.ThreadLocalRandom

import static org.project.karto.util.TestDataGenerator.generateSelfBougthGiftCard

class CashbackAlgorithmTesting extends Specification {

    static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current()

    def "Cashback algorithm under realistic usage"() {
        given:
        giftCard.activate()
        def amount = generateRealisticAmount(giftCard)
        def snapshot = generateRealisticActivity(giftCard)

        when:
        def event = transaction(giftCard, amount, snapshot)

        then:
        event != null
        println "Transaction amount: $amount, activity: $snapshot, event: $event"

        where:
        giftCard << (1..25).collect({generateSelfBougthGiftCard(new Balance(BigDecimal.valueOf(2200L)))})
    }

    static CashbackEvent transaction(GiftCard giftCard, Amount amount, UserActivitySnapshot activitySnapshot) {
        def paymentIntent = giftCard.initializeTransaction(amount, TestDataGenerator.orderID())
        paymentIntent.markAsSuccess(new ExternalPayeeDescription("desc"))

        giftCard.applyTransaction(
                paymentIntent,
                activitySnapshot,
                Currency.getInstance("USD"),
                PaymentType.KARTO_PAYMENT,
                new PaymentSystem("UP")
        )

        giftCard.pullEvents().getFirst()
    }

    static Amount generateRealisticAmount(GiftCard card) {
        def balance = card.balance().value()
        def percent = BigDecimal.valueOf(Math.random() * 0.3 + 0.1) // 10%–40%
        def amount = (balance * percent).setScale(2, RoundingMode.HALF_UP)
        new Amount(amount)
    }

    static UserActivitySnapshot generateRealisticActivity(GiftCard card) {
        def totalSpent = BigDecimal.valueOf(50 + Math.random() * 2001).setScale(2, RoundingMode.HALF_UP)
        def giftCardsBought = RANDOM.nextInt(1, 50)
        def activeDays = RANDOM.nextInt(0, 15)

        new UserActivitySnapshot(
                card.ownerID().get().value(),
                totalSpent,
                giftCardsBought,
                LocalDateTime.now(),
                activeDays, false
        )
    }
}
