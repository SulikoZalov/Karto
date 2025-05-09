package org.project.karto.domain.card.entities;

import org.junit.jupiter.api.Test;
import org.project.karto.domain.card.enumerations.GiftCardRecipientType;
import org.project.karto.domain.card.enumerations.GiftCardStatus;
import org.project.karto.domain.card.enumerations.GiftCardType;
import org.project.karto.domain.card.value_objects.Amount;
import org.project.karto.domain.card.value_objects.Balance;
import org.project.karto.domain.card.value_objects.BuyerID;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.project.karto.util.TestDataGenerator.*;

class GiftCardTest {

    @Test
    void selfBoughtCard_CommonType_ShouldBeCreatedCorrectly() {
        BuyerID buyerID = generateBuyerID();
        Balance balance = generateBalance();
        String secretKey = generateSecretKey();

        GiftCard card = GiftCard.selfBoughtCard_CommonType(buyerID, balance, secretKey);

        assertNotNull(card.id());
        assertEquals(buyerID, card.buyerID());
        assertEquals(buyerID.value(), card.ownerID().value());
        assertNull(card.storeID());
        assertEquals(GiftCardStatus.PENDING, card.giftCardStatus());
        assertEquals(balance, card.balance());
        assertFalse(card.isVerified());
        assertEquals(GiftCardType.COMMON, card.cardType());
        assertEquals(GiftCardRecipientType.SELF, card.recipientType());
    }

    @Test
    void shouldActivateCardSuccessfully() {
        GiftCard card = GiftCard.selfBoughtCard_CommonType(
                generateBuyerID(),
                generateBalance(),
                generateSecretKey());

        card.activate();

        assertTrue(card.isVerified());
        assertEquals(GiftCardStatus.ACTIVE, card.giftCardStatus());
    }

    @Test
    void shouldSpendMoneyCorrectly() {
        Balance initialBalance = new Balance(BigDecimal.valueOf(500));
        GiftCard card = GiftCard.selfBoughtCard_CommonType(
                generateBuyerID(),
                initialBalance,
                generateSecretKey());

        card.activate();
        Amount amount = new Amount(BigDecimal.valueOf(200));
        card.spend(amount);

        assertEquals(1, card.countOfUses());
        assertEquals(BigDecimal.valueOf(300), card.balance().value());
    }

    @Test
    void shouldThrowIfSpendOnInactiveCard() {
        GiftCard card = GiftCard.selfBoughtCard_CommonType(
                generateBuyerID(),
                generateBalance(),
                generateSecretKey());

        Amount amount = new Amount(BigDecimal.valueOf(100));

        assertThrows(IllegalStateException.class, () -> card.spend(amount));
    }
}