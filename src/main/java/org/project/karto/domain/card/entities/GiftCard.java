package org.project.karto.domain.card.entities;

import org.project.karto.domain.card.enumerations.GiftCardRecipientType;
import org.project.karto.domain.card.enumerations.GiftCardStatus;
import org.project.karto.domain.card.value_objects.*;
import org.project.karto.domain.common.annotations.Nullable;
import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.KeyAndCounter;

import java.time.LocalDateTime;
import java.util.UUID;

public class GiftCard {
    private final CardID id;
    private final PAN pan;
    private final BuyerID buyerID;
    private final @Nullable OwnerID ownerID;
    private final StoreID storeID;
    private final int maxCountOfUses;
    private final LocalDateTime creationDate;
    private final LocalDateTime expirationDate;

    private GiftCardStatus giftCardStatus;
    private Balance balance;
    private int countOfUses;
    private KeyAndCounter keyAndCounter;
    private LocalDateTime lastUsage;

    private GiftCard(
            CardID id,
            BuyerID buyerID,
            @Nullable OwnerID ownerID,
            StoreID storeID,
            int maxCountOfUses,
            PAN pan,
            GiftCardStatus giftCardStatus,
            Balance balance,
            int countOfUses,
            KeyAndCounter keyAndCounter,
            LocalDateTime creationDate,
            LocalDateTime expirationDate,
            LocalDateTime lastUsage) {

        this.id = id;
        this.buyerID = buyerID;
        this.ownerID = ownerID;
        this.storeID = storeID;
        this.maxCountOfUses = maxCountOfUses;
        this.pan = pan;
        this.giftCardStatus = giftCardStatus;
        this.balance = balance;
        this.countOfUses = countOfUses;
        this.keyAndCounter = keyAndCounter;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.lastUsage = lastUsage;
    }

    public static GiftCard selfBoughtCard(PAN pan, BuyerID buyerID, Balance balance, StoreID storeID,
                                          String secretKey, CardUsageLimitations cardUsageLimitations) {

        validateInputs(pan, buyerID, balance, storeID, secretKey, cardUsageLimitations);

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(cardUsageLimitations.expirationPeriod());

        int maxCountOfUses = cardUsageLimitations.maxUsageCount();
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, new OwnerID(buyerID.value()), storeID, maxCountOfUses, pan,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate, creationDate);
    }

    public static GiftCard boughtAsAGift(PAN pan, BuyerID buyerID, Balance balance, @Nullable OwnerID ownerID,
                                         StoreID storeID, String secretKey, CardUsageLimitations cardUsageLimitations) {

        validateInputs(pan, buyerID, balance, storeID, secretKey, cardUsageLimitations);

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(cardUsageLimitations.expirationPeriod());

        int maxCountOfUses = cardUsageLimitations.maxUsageCount();
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, ownerID, storeID, maxCountOfUses, pan,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate, creationDate);
    }

    private static void validateInputs(PAN pan, BuyerID buyerID, Balance balance, StoreID storeID,
                                       String secretKey, CardUsageLimitations cardUsageLimitations) {

        if (pan == null) throw new IllegalArgumentException("PAN cannot be null");
        if (buyerID == null) throw new IllegalArgumentException("Buyer id can`t be null");
        if (balance == null) throw new IllegalArgumentException("Balance can`t be null");
        if (storeID == null) throw new IllegalArgumentException("Store can`t be null");
        if (secretKey == null) throw new IllegalArgumentException("Secret key can`t be null");
        if (cardUsageLimitations == null) throw new IllegalArgumentException("Card limitations can`t be null");
    }

    public static GiftCard fromRepository(
            CardID id,
            PAN pan,
            BuyerID buyerID,
            OwnerID ownerID,
            StoreID storeID,
            GiftCardStatus giftCardStatus,
            Balance balance,
            int countOfUses,
            int maxCountOfUses,
            KeyAndCounter keyAndCounter,
            LocalDateTime creationDate,
            LocalDateTime expirationDate,
            LocalDateTime lastUsage) {

        return new GiftCard(id, buyerID, ownerID, storeID, maxCountOfUses, pan,
                giftCardStatus, balance, countOfUses, keyAndCounter, creationDate, expirationDate, lastUsage);
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

    public StoreID storeID() {
        return storeID;
    }

    public PAN pan() {
        return pan;
    }

    public GiftCardStatus giftCardStatus() {
        return giftCardStatus;
    }

    public KeyAndCounter keyAndCounter() {
        return keyAndCounter;
    }

    public GiftCardRecipientType recipientType() {
        if (ownerID == null) return GiftCardRecipientType.OTHER;
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

    public int maxCountOfUses() {
        return maxCountOfUses;
    }

    public boolean isVerified() {
        return giftCardStatus == GiftCardStatus.ACTIVE;
    }

    public LocalDateTime creationDate() {
        return creationDate;
    }

    public LocalDateTime expirationDate() {
        return expirationDate;
    }

    public LocalDateTime lastUsage() {
        return lastUsage;
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
        if (isVerified()) throw new IllegalStateException("You can`t enable already active card");
        if (giftCardStatus != GiftCardStatus.PENDING)
            throw new IllegalStateException("Only cards in PENDING state can be verified.");

        giftCardStatus = GiftCardStatus.ACTIVE;
        keyAndCounter = new KeyAndCounter(keyAndCounter.key(), keyAndCounter.counter() + 1);
    }

    public boolean hasSufficientBalance(Amount amount) {
        if (amount == null) throw new IllegalArgumentException("Amount can`t be null");
        return balance.value().compareTo(amount.value()) >= 0;
    }

    public void spend(Amount amount) {
        if (amount == null) throw new IllegalArgumentException("Amount can`t be null");
        if (isExpired()) throw new IllegalStateException("You can`t use expired card");
        if (giftCardStatus != GiftCardStatus.ACTIVE) throw new IllegalStateException("Card is not activated");
        if (countOfUses >= maxCountOfUses) throw new IllegalArgumentException("Card reached max count of uses");
        if (!hasSufficientBalance(amount))
            throw new IllegalArgumentException("There is not enough money on the balance");

        countOfUses++;
        balance = new Balance(balance.value().subtract(amount.value()));
        lastUsage = LocalDateTime.now();
    }
}
