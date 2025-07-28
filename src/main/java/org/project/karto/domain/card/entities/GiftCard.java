package org.project.karto.domain.card.entities;

import org.project.karto.domain.card.enumerations.*;
import org.project.karto.domain.card.events.CashbackEvent;
import org.project.karto.domain.card.exceptions.FailedPaymentIntentException;
import org.project.karto.domain.card.value_objects.Currency;
import org.project.karto.domain.card.value_objects.*;
import org.project.karto.domain.common.annotations.Nullable;
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.common.exceptions.IllegalDomainStateException;
import org.project.karto.domain.common.interfaces.KartoDomainEvent;
import org.project.karto.domain.common.tuples.Pair;
import org.project.karto.domain.common.value_objects.Amount;
import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.KeyAndCounter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.project.karto.domain.common.util.Utils.required;

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
    private long version;
    private final Deque<KartoDomainEvent> events;

    public static final BigDecimal KARTO_COMMON_CARD_FEE_RATE = BigDecimal.valueOf(0.02);
    public static final BigDecimal DEFAULT_CASHBACK = BigDecimal.valueOf(0.01); // 1%
    public static final BigDecimal MAX_CASHBACK_RATE = BigDecimal.valueOf(0.035); // 3.5%
    public static final BigDecimal ACTIVITY_MULTIPLIER = BigDecimal.valueOf(0.001); // 0.01% for every consecutive usage
                                                                                    // day

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
            LocalDateTime lastUsage,
            long version) {

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
        this.version = version;
    }

    public static GiftCard selfBoughtCard(BuyerID buyerID, Balance balance, StoreID storeID,
            String secretKey, CardUsageLimitations cardUsageLimitations) {

        validateInputs(buyerID, balance, secretKey, cardUsageLimitations);
        if (storeID == null)
            throw new IllegalDomainArgumentException("StoreID cannot be null for store-specific gift cards.");

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(cardUsageLimitations.expirationPeriod());

        int maxCountOfUses = cardUsageLimitations.maxUsageCount();
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, new OwnerID(buyerID.value()), storeID,
                maxCountOfUses,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate,
                creationDate, 1);
    }

    public static GiftCard boughtAsAGift(BuyerID buyerID, Balance balance, StoreID storeID,
            String secretKey, CardUsageLimitations cardUsageLimitations) {

        validateInputs(buyerID, balance, secretKey, cardUsageLimitations);
        if (storeID == null)
            throw new IllegalDomainArgumentException("StoreID cannot be null for store-specific gift cards.");

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(cardUsageLimitations.expirationPeriod());

        int maxCountOfUses = cardUsageLimitations.maxUsageCount();
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, null, storeID, maxCountOfUses,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate,
                creationDate, 1);
    }

    public static GiftCard selfBoughtCommonCard(BuyerID buyerID, Balance balance,
            String secretKey, CardUsageLimitations cardUsageLimitations) {

        validateInputs(buyerID, balance, secretKey, cardUsageLimitations);

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(cardUsageLimitations.expirationPeriod());

        int maxCountOfUses = cardUsageLimitations.maxUsageCount();
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, new OwnerID(buyerID.value()), null, maxCountOfUses,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate,
                creationDate, 1);
    }

    public static GiftCard giftedCommonCard(BuyerID buyerID, Balance balance,
            String secretKey, CardUsageLimitations cardUsageLimitations) {

        validateInputs(buyerID, balance, secretKey, cardUsageLimitations);

        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(cardUsageLimitations.expirationPeriod());

        int maxCountOfUses = cardUsageLimitations.maxUsageCount();
        return new GiftCard(new CardID(UUID.randomUUID()), buyerID, null, null, maxCountOfUses,
                GiftCardStatus.PENDING, balance, 0, new KeyAndCounter(secretKey, 0), creationDate, expirationDate,
                creationDate, 1);
    }

    private static void validateInputs(BuyerID buyerID, Balance balance, String secretKey,
            CardUsageLimitations cardUsageLimitations) {

        if (buyerID == null)
            throw new IllegalDomainArgumentException("Buyer id can`t be null");
        if (balance == null)
            throw new IllegalDomainArgumentException("Balance can`t be null");
        if (secretKey == null)
            throw new IllegalDomainArgumentException("Secret key can`t be null");
        if (cardUsageLimitations == null)
            throw new IllegalDomainArgumentException("Card limitations can`t be null");
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
            LocalDateTime lastUsage,
            long version) {

        return new GiftCard(id, buyerID, ownerID, storeID, maxCountOfUses,
                giftCardStatus, balance, countOfUses, keyAndCounter, creationDate, expirationDate, lastUsage, version);
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
        if (ownerID == null)
            return GiftCardRecipientType.OTHER;
        return buyerID.value().equals(ownerID.value()) ? GiftCardRecipientType.SELF : GiftCardRecipientType.OTHER;
    }

    public GiftCardType giftCardType() {
        if (storeID == null)
            return GiftCardType.COMMON;
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

    public long version() {
        return version;
    }

    public long previousVersion() {
        if (version == 1)
            return 1;
        return version - 1;
    }

    public List<KartoDomainEvent> pullEvents() {
        List<KartoDomainEvent> eventList = new ArrayList<>(events);
        events.clear();
        return eventList;
    }

    public boolean markExpiredIfNeeded() {
        if (expirationDate.isBefore(LocalDateTime.now())) {
            giftCardStatus = GiftCardStatus.EXPIRED;
            incrementVersion();
            return true;
        }

        return false;
    }

    public void activate() {
        if (markExpiredIfNeeded())
            throw new IllegalDomainStateException("You can`t activate expired card");
        if (isVerified())
            throw new IllegalDomainStateException("You can`t enable already active card");
        if (ownerID == null)
            throw new IllegalDomainStateException("You can`t activate account without owner id.");
        if (giftCardStatus != GiftCardStatus.PENDING)
            throw new IllegalDomainStateException("Only cards in PENDING state can be verified.");

        this.giftCardStatus = GiftCardStatus.ACTIVE;
        this.keyAndCounter = new KeyAndCounter(keyAndCounter.key(), keyAndCounter.counter() + 1);
        incrementVersion();
    }

    public void activate(OwnerID ownerID) {
        if (markExpiredIfNeeded())
            throw new IllegalDomainStateException("You can`t activate expired card.");
        if (isVerified())
            throw new IllegalDomainStateException("You can`t enable already active card.");
        if (ownerID == null)
            throw new IllegalDomainArgumentException("You can`t activate account without owner id.");
        if (giftCardStatus != GiftCardStatus.PENDING)
            throw new IllegalDomainStateException("Only cards in PENDING state can be verified.");

        if (this.ownerID != null)
            throw new IllegalDomainStateException("You can`t change owner id. It can be specified only once.");
        if (ownerID.value().equals(this.buyerID.value()))
            throw new IllegalDomainStateException(
                    "The card was purchased as a gift, the owner's ID cannot be equal to the buyer's ID");

        this.ownerID = ownerID;
        this.giftCardStatus = GiftCardStatus.ACTIVE;
        this.keyAndCounter = new KeyAndCounter(keyAndCounter.key(), keyAndCounter.counter() + 1);
        incrementVersion();
    }

    public synchronized PaymentIntent initializeTransaction(Amount amount, long orderID, StoreID storeID) {
        if (amount == null)
            throw new IllegalDomainArgumentException("Amount can`t be null");
        if (markExpiredIfNeeded())
            throw new IllegalDomainStateException("You can`t use expired card");

        if (giftCardStatus != GiftCardStatus.ACTIVE)
            throw new IllegalDomainStateException("Card is not activated");
        if (countOfUses >= maxCountOfUses)
            throw new IllegalDomainArgumentException("Card reached max count of uses");
        if (this.storeID != null && this.storeID != storeID)
            throw new IllegalDomainArgumentException("StoreID specified wrong.");

        BigDecimal fee = calculateInternalFee(amount);
        Amount totalAmount = new Amount(amount.value().add(fee));

        if (!hasSufficientBalance(totalAmount))
            throw new IllegalDomainArgumentException("There is not enough money on the balance");

        countOfUses++;
        incrementVersion();
        return PaymentIntent.of(buyerID, id, storeID, orderID, totalAmount, new InternalFeeAmount(fee));
    }

    public synchronized Check applyTransaction(PaymentIntent intent, UserActivitySnapshot activitySnapshot,
            Currency currency,
            PaymentType paymentType, PaymentSystem paymentSystem, BankName bankName) {

        required("paymentIntent", intent);
        required("userActivitySnapshot", activitySnapshot);
        required("currency", currency);
        required("paymentType", paymentType);
        required("paymentSystem", paymentSystem);

        if (!intent.cardID().equals(id))
            throw new IllegalDomainArgumentException("Payment intent do not match the card");
        if (!activitySnapshot.userID().equals(ownerID.value()))
            throw new IllegalDomainArgumentException("UserID do not match");

        if (intent.isConfirmed())
            throw new IllegalDomainStateException("Payment intent is already confirmed");

        intent.confirm();
        if (intent.status() != PurchaseStatus.SUCCESS) {
            countOfUses--;
            incrementVersion();
            throw new FailedPaymentIntentException("Payment intent is not successful");
        }

        balance = calculateBalance(intent.totalAmount());
        lastUsage = LocalDateTime.now();

        Pair<BigDecimal, Boolean> cashback = calculateCashback(intent.totalAmount().value(), activitySnapshot);
        events.addFirst(new CashbackEvent(id, ownerID, cashback.getFirst(), cashback.getSecond()));
        incrementVersion();

        return Check.paymentCheck(intent.orderID(), intent.buyerID(), storeID, id, intent.totalAmount(), currency,
                intent.feeAmount(), paymentSystem, intent.paymentDescription(), bankName);
    }

    private void incrementVersion() {
        version++;
    }

    private boolean hasSufficientBalance(Amount amount) {
        if (amount == null)
            throw new IllegalDomainArgumentException("Amount can`t be null");
        return balance.value().compareTo(amount.value()) >= 0;
    }

    private Balance calculateBalance(Amount totalAmount) {
        return new Balance(balance.value().subtract(totalAmount.value()));
    }

    private BigDecimal calculateInternalFee(Amount amount) {
        if (giftCardType() == GiftCardType.COMMON)
            return BigDecimal.ZERO;
        return amount.value().multiply(KARTO_COMMON_CARD_FEE_RATE);
    }

    private Pair<BigDecimal, Boolean> calculateCashback(BigDecimal spentAmount, UserActivitySnapshot snapshot) {
        if (snapshot.lastUsageReachedMaximumCashbackRate())
            return Pair.of(DEFAULT_CASHBACK, false);

        LoyaltyLevel level = LoyaltyLevel.determineLevel(snapshot);
        BigDecimal loyaltyBonus = level.cashbackBonus();
        BigDecimal activityBonus = BigDecimal.valueOf(snapshot.consecutiveActiveDays()).multiply(ACTIVITY_MULTIPLIER);

        boolean reachedMaxCashbackRate = false;
        BigDecimal totalRate = DEFAULT_CASHBACK.add(loyaltyBonus).add(activityBonus);
        if (totalRate.compareTo(MAX_CASHBACK_RATE) > 0) {
            totalRate = MAX_CASHBACK_RATE;
            reachedMaxCashbackRate = true;
        }

        return Pair.of(spentAmount.multiply(totalRate), reachedMaxCashbackRate);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        GiftCard giftCard = (GiftCard) o;
        return Objects.equals(id, giftCard.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
