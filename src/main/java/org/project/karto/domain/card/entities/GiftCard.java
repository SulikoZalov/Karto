package org.project.karto.domain.card.entities;

import org.project.karto.domain.card.enumerations.GiftCardRecipientType;
import org.project.karto.domain.card.enumerations.GiftCardStatus;
import org.project.karto.domain.card.enumerations.Store;
import org.project.karto.domain.card.value_objects.*;
import org.project.karto.domain.common.annotations.Nullable;
import org.project.karto.domain.common.value_objects.KeyAndCounter;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

public class GiftCard {
    private final CardID id;
    private final BuyerID buyerID;
    private final @Nullable OwnerID ownerID;
    private final Store store;
    private GiftCardStatus giftCardStatus;
    private Balance balance;
    private int countOfUses;
    private boolean isVerified;
    private KeyAndCounter keyAndCounter;
    private final LocalDateTime creationDate;
    private final LocalDateTime expirationDate;

    private GiftCard(
            CardID id,
            BuyerID buyerID,
            @Nullable OwnerID ownerID,
            Store store,
            GiftCardStatus giftCardStatus,
            Balance balance,
            int countOfUses,
            boolean isVerified,
            KeyAndCounter keyAndCounter,
            LocalDateTime creationDate,
            LocalDateTime expirationDate) {

        this.id = id;
        this.buyerID = buyerID;
        this.ownerID = ownerID;
        this.store = store;
        this.giftCardStatus = giftCardStatus;
        this.balance = balance;
        this.countOfUses = countOfUses;
        this.isVerified = isVerified;
        this.keyAndCounter = keyAndCounter;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

    public static GiftCard selfBoughtCard(BuyerID buyerID, Balance balance,
                                          Store store, String secretKey) {
        if (buyerID == null) throw new IllegalArgumentException("Buyer id can`t be null");
        if (balance == null) throw new IllegalArgumentException("Balance can`t be null");
        if (store == null) throw new IllegalArgumentException("Store can`t be null");

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(Period.ofMonths(2));
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, new OwnerID(buyerID.value()), store,
                GiftCardStatus.PENDING, balance, 0, false, new KeyAndCounter(secretKey, 0), creationDate, expirationDate);
    }

    public static GiftCard boughtAsAGift(BuyerID buyerID, Balance balance, @Nullable OwnerID ownerID,
                                         Store store, String secretKey) {
        if (buyerID == null) throw new IllegalArgumentException("Buyer id can`t be null");
        if (balance == null) throw new IllegalArgumentException("Balance can`t be null");
        if (store == null) throw new IllegalArgumentException("Store can`t be null");

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(Period.ofMonths(2));
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, ownerID, store,
                GiftCardStatus.PENDING, balance, 0, false,  new KeyAndCounter(secretKey, 0), creationDate, expirationDate);
    }

    public static GiftCard fromRepository(
            CardID id,
            BuyerID buyerID,
            OwnerID ownerID,
            Store store,
            GiftCardStatus giftCardStatus,
            Balance balance,
            int countOfUses,
            boolean isVerified,
            KeyAndCounter keyAndCounter,
            LocalDateTime creationDate,
            LocalDateTime expirationDate) {

        return new GiftCard(id, buyerID, ownerID, store, giftCardStatus,
                balance, countOfUses, isVerified, keyAndCounter, creationDate, expirationDate);
    }

    public CardID id() {
        return id;
    }

    public BuyerID buyerID() {
        return buyerID;
    }

    public OwnerID ownerID() {
        return ownerID;
    }

    public Store store() {
        return store;
    }

    public GiftCardStatus giftCardStatus() {
        return giftCardStatus;
    }

    public KeyAndCounter keyAndCounter() {
        return keyAndCounter;
    }

    public GiftCardRecipientType recipientType() {
        return buyerID.value().equals(ownerID.value()) ?
                GiftCardRecipientType.SELF :
                GiftCardRecipientType.OTHER;
    }

    public Balance balance() {
        return balance;
    }

    public int countOfUses() {
        return countOfUses;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public LocalDateTime creationDate() {
        return creationDate;
    }

    public LocalDateTime expirationDate() {
        return expirationDate;
    }

    public boolean isExpired() {
        if (expirationDate.isBefore(LocalDateTime.now())) {
            giftCardStatus = GiftCardStatus.EXPIRED;
            return true;
        }

        return false;
    }

    public void activate() {
        if (isExpired()) throw new IllegalStateException("You can`t activate expired card");
        if (isVerified) throw new IllegalStateException("You can`t enable already active card");
        if (giftCardStatus != GiftCardStatus.PENDING)
            throw new IllegalStateException("Only cards in PENDING state can be verified.");
        isVerified = true;
        giftCardStatus = GiftCardStatus.ACTIVE;
        keyAndCounter = new KeyAndCounter(keyAndCounter.key(), keyAndCounter.counter() + 1);
    }

    public boolean hasSufficientBalance(Amount amount) {
        if (amount == null) throw new IllegalArgumentException("Amount can`t be null");
        return balance.value().compareTo(amount.value()) >= 0;
    }

    public void spend(Amount amount) {
        if (amount == null) throw new IllegalArgumentException("Amount can`t be null");
        if (isExpired()) throw new IllegalStateException("You can`t activate expired card");
        if (giftCardStatus != GiftCardStatus.ACTIVE) throw new IllegalStateException("Card is not activated");

        if (!hasSufficientBalance(amount))
            throw new IllegalArgumentException("There is not enough money on the balance");

        countOfUses++;
        balance = new Balance(balance.value().subtract(amount.value()));
    }
}
