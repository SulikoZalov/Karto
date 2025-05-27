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
    private final int maxCountOfUses;
    private final LocalDateTime creationDate;
    private final LocalDateTime expirationDate;

    private GiftCardStatus giftCardStatus;
    private Balance balance;
    private int countOfUses;
    private KeyAndCounter keyAndCounter;
    private LocalDateTime lastUsage;

    public static final Period MIN_VALIDITY_TIME = Period.ofWeeks(1);
    public static final Period MAX_VALIDITY_TIME = Period.ofMonths(13);
    private static final int MIN_COUNT_OF_USES = 3;
    private static final int MAX_COUNT_OF_USES = 1240;

    private GiftCard(
            CardID id,
            BuyerID buyerID,
            @Nullable OwnerID ownerID,
            Store store,
            int maxCountOfUses,
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
        this.store = store;
        this.maxCountOfUses = maxCountOfUses;
        this.giftCardStatus = giftCardStatus;
        this.balance = balance;
        this.countOfUses = countOfUses;
        this.keyAndCounter = keyAndCounter;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.lastUsage = lastUsage;
    }

    public static GiftCard selfBoughtCard(BuyerID buyerID, Balance balance, Store store,
                                          String secretKey, int maxCountOfUses, Period validityPeriod) {

        validateInputs(buyerID, balance, store, secretKey, maxCountOfUses, validityPeriod);

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(validityPeriod);

        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, new OwnerID(buyerID.value()), store, maxCountOfUses,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate, creationDate);
    }

    public static GiftCard boughtAsAGift(BuyerID buyerID, Balance balance, @Nullable OwnerID ownerID,
                                         Store store, String secretKey, int maxCountOfUses, Period validityPeriod) {

        validateInputs(buyerID, balance, store, secretKey, maxCountOfUses, validityPeriod);

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(validityPeriod);

        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, ownerID, store, maxCountOfUses,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate, creationDate);
    }

    private static void validateInputs(BuyerID buyerID, Balance balance, Store store,
                                       String secretKey, int maxCountOfUses, Period validityPeriod) {

        if (buyerID == null) throw new IllegalArgumentException("Buyer id can`t be null");
        if (balance == null) throw new IllegalArgumentException("Balance can`t be null");
        if (store == null) throw new IllegalArgumentException("Store can`t be null");
        if (secretKey == null) throw new IllegalArgumentException("Secret key can`t be null");
        if (validityPeriod == null) throw new IllegalArgumentException("Validity period can`t be null");
        if (!isWithinValidRange(validityPeriod))
            throw new IllegalArgumentException("Invalid validity period range");
        if (!isWithinValidRange(maxCountOfUses))
            throw new IllegalArgumentException("Invalid count of uses range");
    }

    private static boolean isWithinValidRange(Period period) {
        if (period.isNegative() || period.isZero()) return false;

        long days = period.toTotalMonths() * 30 + period.getDays();
        long minDays = MIN_VALIDITY_TIME.toTotalMonths() * 30 + MIN_VALIDITY_TIME.getDays();
        long maxDays = MAX_VALIDITY_TIME.toTotalMonths() * 30 + MAX_VALIDITY_TIME.getDays();
        return days >= minDays && days <= maxDays;
    }

    private static boolean isWithinValidRange(int maxCountOfUses) {
        if (maxCountOfUses < MIN_COUNT_OF_USES) return false;
        return maxCountOfUses <= MAX_COUNT_OF_USES;
    }

    public static GiftCard fromRepository(
            CardID id,
            BuyerID buyerID,
            OwnerID ownerID,
            Store store,
            GiftCardStatus giftCardStatus,
            Balance balance,
            int countOfUses,
            int maxCountOfUses,
            KeyAndCounter keyAndCounter,
            LocalDateTime creationDate,
            LocalDateTime expirationDate,
            LocalDateTime lastUsage) {

        return new GiftCard(id, buyerID, ownerID, store, maxCountOfUses,
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
