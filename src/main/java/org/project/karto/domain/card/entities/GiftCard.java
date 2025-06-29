package org.project.karto.domain.card.entities;

import org.project.karto.domain.card.enumerations.GiftCardRecipientType;
import org.project.karto.domain.card.enumerations.GiftCardStatus;
import org.project.karto.domain.card.enumerations.GiftCardType;
import org.project.karto.domain.card.events.CashbackEvent;
import org.project.karto.domain.card.value_objects.*;
import org.project.karto.domain.common.annotations.Nullable;
import org.project.karto.domain.common.interfaces.KartoDomainEvent;
import org.project.karto.domain.common.value_objects.Amount;
import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.KeyAndCounter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

public class GiftCard {
    private final CardID id;
    private final BuyerID buyerID;
    private @Nullable OwnerID ownerID;
    private final @Nullable StoreID storeID;
    private final int maxCountOfUses;
    private final LocalDateTime creationDate;
    private final LocalDateTime expirationDate;

    private GiftCardStatus giftCardStatus;
    private Balance balance;
    private int countOfUses;
    private KeyAndCounter keyAndCounter;
    private LocalDateTime lastUsage;
    private final Deque<KartoDomainEvent> events;

    public static final BigDecimal DEFAULT_CASHBACK = BigDecimal.valueOf(0.02);        // 2%
    public static final BigDecimal MAX_CASHBACK_RATE = BigDecimal.valueOf(0.12);       // 12%
    public static final BigDecimal SPENT_DIVISOR = BigDecimal.valueOf(50);             // 50 units of spending
    public static final BigDecimal SPENT_MULTIPLIER = BigDecimal.valueOf(0.0025);      // 0.25%
    public static final BigDecimal ACTIVITY_MULTIPLIER = BigDecimal.valueOf(0.0003);   // 0.03% for every consecutive usage day

    private GiftCard(
            CardID id,
            BuyerID buyerID,
            @Nullable OwnerID ownerID,
            StoreID storeID,
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
        this.storeID = storeID;
        this.maxCountOfUses = maxCountOfUses;
        this.giftCardStatus = giftCardStatus;
        this.balance = balance;
        this.countOfUses = countOfUses;
        this.keyAndCounter = keyAndCounter;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.lastUsage = lastUsage;
        this.events = new ArrayDeque<>();
    }

    public static GiftCard selfBoughtCard(BuyerID buyerID, Balance balance, StoreID storeID,
                                          String secretKey, CardUsageLimitations cardUsageLimitations) {

        validateInputs(buyerID, balance, secretKey, cardUsageLimitations);
        if (storeID == null) throw new IllegalArgumentException("StoreID cannot be null for store-specific gift cards.");

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(cardUsageLimitations.expirationPeriod());

        int maxCountOfUses = cardUsageLimitations.maxUsageCount();
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, new OwnerID(buyerID.value()), storeID, maxCountOfUses,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate, creationDate);
    }

    public static GiftCard boughtAsAGift(BuyerID buyerID, Balance balance, StoreID storeID,
                                         String secretKey, CardUsageLimitations cardUsageLimitations) {

        validateInputs(buyerID, balance, secretKey, cardUsageLimitations);
        if (storeID == null) throw new IllegalArgumentException("StoreID cannot be null for store-specific gift cards.");

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(cardUsageLimitations.expirationPeriod());

        int maxCountOfUses = cardUsageLimitations.maxUsageCount();
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, null, storeID, maxCountOfUses,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate, creationDate);
    }

    public static GiftCard selfBoughtCommonCard(BuyerID buyerID, Balance balance,
                                                String secretKey, CardUsageLimitations cardUsageLimitations) {

        validateInputs(buyerID, balance, secretKey, cardUsageLimitations);

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(cardUsageLimitations.expirationPeriod());

        int maxCountOfUses = cardUsageLimitations.maxUsageCount();
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, new OwnerID(buyerID.value()), null, maxCountOfUses,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate, creationDate);
    }

    public static GiftCard giftedCommonCard(BuyerID buyerID, Balance balance,
                                            String secretKey, CardUsageLimitations cardUsageLimitations) {

        validateInputs(buyerID, balance, secretKey, cardUsageLimitations);

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(cardUsageLimitations.expirationPeriod());

        int maxCountOfUses = cardUsageLimitations.maxUsageCount();
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, null, null, maxCountOfUses,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate, creationDate);
    }

    private static void validateInputs(BuyerID buyerID, Balance balance, String secretKey,
                                       CardUsageLimitations cardUsageLimitations) {

        if (buyerID == null) throw new IllegalArgumentException("Buyer id can`t be null");
        if (balance == null) throw new IllegalArgumentException("Balance can`t be null");
        if (secretKey == null) throw new IllegalArgumentException("Secret key can`t be null");
        if (cardUsageLimitations == null) throw new IllegalArgumentException("Card limitations can`t be null");
    }

    public static GiftCard fromRepository(
            CardID id,
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

        return new GiftCard(id, buyerID, ownerID, storeID, maxCountOfUses,
                giftCardStatus, balance, countOfUses, keyAndCounter, creationDate, expirationDate, lastUsage);
    }

    public CardID id() {
        return id;
    }

    public BuyerID buyerID() {
        return buyerID;
    }

    public Optional<OwnerID> ownerID() {
        return Optional.ofNullable(ownerID);
    }

    public Optional<StoreID> storeID() {
        return Optional.ofNullable(storeID);
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

    public GiftCardType giftCardType() {
        if (storeID == null) return GiftCardType.COMMON;
        return GiftCardType.STORE_SPECIFIC;
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

    public List<KartoDomainEvent> pullEvents() {
        List<KartoDomainEvent> eventList = new ArrayList<>(events);
        events.clear();
        return eventList;
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
        if (ownerID == null) throw new IllegalStateException("You can`t activate account without owner id.");
        if (giftCardStatus != GiftCardStatus.PENDING)
            throw new IllegalStateException("Only cards in PENDING state can be verified.");

        this.giftCardStatus = GiftCardStatus.ACTIVE;
        this.keyAndCounter = new KeyAndCounter(keyAndCounter.key(), keyAndCounter.counter() + 1);
    }

    public void activate(OwnerID ownerID) {
        if (isExpired()) throw new IllegalStateException("You can`t activate expired card.");
        if (isVerified()) throw new IllegalStateException("You can`t enable already active card.");
        if (ownerID == null) throw new IllegalArgumentException("You can`t activate account without owner id.");
        if (giftCardStatus != GiftCardStatus.PENDING)
            throw new IllegalStateException("Only cards in PENDING state can be verified.");

        if (this.ownerID != null)
            throw new IllegalStateException("You can`t change owner id. It can be specified only once.");
        if (ownerID.value().equals(this.buyerID.value()))
            throw new IllegalStateException("The card was purchased as a gift, the owner's ID cannot be equal to the buyer's ID");

        this.ownerID = ownerID;
        this.giftCardStatus = GiftCardStatus.ACTIVE;
        this.keyAndCounter = new KeyAndCounter(keyAndCounter.key(), keyAndCounter.counter() + 1);
    }

    public boolean hasSufficientBalance(Amount amount) {
        if (amount == null) throw new IllegalArgumentException("Amount can`t be null");
        return balance.value().compareTo(amount.value()) >= 0;
    }

    public void spend(Amount amount, UserActivitySnapshot activitySnapshot, Fee externalFee) {
        validateSpendAbility(amount, activitySnapshot, externalFee);

        Amount totalAmount = calculateTotalAmount(amount, externalFee);
        if (!hasSufficientBalance(totalAmount))
            throw new IllegalArgumentException("There is not enough money on the balance");

        countOfUses++;
        balance = calculateBalance(totalAmount);
        lastUsage = LocalDateTime.now();

        BigDecimal cashback = calculateCashback(totalAmount.value(), activitySnapshot);
        events.addFirst(new CashbackEvent(id, ownerID, cashback));
    }

    private void validateSpendAbility(Amount amount, UserActivitySnapshot activitySnapshot, Fee externalFee) {
        if (amount == null) throw new IllegalArgumentException("Amount can`t be null");
        if (activitySnapshot == null) throw new IllegalArgumentException("User activity snapshot cannot be null");
        if (externalFee == null) throw new IllegalArgumentException("External fee can`t be null");
        if (!activitySnapshot.userID().equals(ownerID.value()))
            throw new IllegalArgumentException("UserID do not match");
        if (isExpired()) throw new IllegalStateException("You can`t use expired card");
        if (giftCardStatus != GiftCardStatus.ACTIVE) throw new IllegalStateException("Card is not activated");
        if (countOfUses >= maxCountOfUses) throw new IllegalArgumentException("Card reached max count of uses");
    }

    private Balance calculateBalance(Amount totalAmount) {
        return new Balance(balance.value().subtract(totalAmount.value()));
    }

    private Amount calculateTotalAmount(Amount amount, Fee externalFee) {
        amount = new Amount(amount.value().add(externalFee.rate()));
        if (giftCardType() != GiftCardType.COMMON) return amount;

        Amount fee = calculateFee(amount);
        return new Amount(amount.value().add(fee.value()));
    }

    private Amount calculateFee(Amount amount) {
        return new Amount(amount.value().multiply(BigDecimal.valueOf(0.02)));
    }

    private BigDecimal calculateCashback(BigDecimal spentAmount, UserActivitySnapshot snapshot) {
        BigDecimal totalSpentFactor = snapshot.totalAmountSpent()
                .divide(SPENT_DIVISOR, 4, RoundingMode.HALF_UP)
                .multiply(SPENT_MULTIPLIER);

        BigDecimal activityFactor = BigDecimal.valueOf(snapshot.consecutiveActiveDays())
                .multiply(ACTIVITY_MULTIPLIER);

        BigDecimal totalRate = DEFAULT_CASHBACK.add(totalSpentFactor).add(activityFactor);

        if (totalRate.compareTo(MAX_CASHBACK_RATE) > 0) totalRate = MAX_CASHBACK_RATE;
        return spentAmount.multiply(totalRate).setScale(2, RoundingMode.HALF_UP);
    }
}
