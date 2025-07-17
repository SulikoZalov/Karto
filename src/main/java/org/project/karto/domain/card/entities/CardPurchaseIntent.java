package org.project.karto.domain.card.entities;

import org.project.karto.domain.card.enumerations.GiftCardType;
import org.project.karto.domain.card.enumerations.PaymentType;
import org.project.karto.domain.card.enumerations.PurchaseStatus;
import org.project.karto.domain.card.value_objects.*;
import org.project.karto.domain.common.annotations.Nullable;
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.common.exceptions.IllegalDomainStateException;
import org.project.karto.domain.common.value_objects.Amount;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.project.karto.domain.common.util.Utils.required;

public class CardPurchaseIntent {
    private final UUID id;
    private final BuyerID buyerID;
    private final @Nullable StoreID storeID;
    private final long orderID;
    private final Amount totalPayedAmount;
    private final LocalDateTime creationDate;

    private @Nullable LocalDateTime resultDate;
    private PurchaseStatus status;
    private @Nullable Fee removedFee;

    private CardPurchaseIntent(
            UUID id,
            BuyerID buyerID,
            StoreID storeID,
            long orderID,
            Amount totalPayedAmount,
            LocalDateTime creationDate,
            LocalDateTime resultDate,
            PurchaseStatus status,
            Fee removedFee) {

        this.id = id;
        this.buyerID = buyerID;
        this.storeID = storeID;
        this.orderID = orderID;
        this.totalPayedAmount = totalPayedAmount;
        this.creationDate = creationDate;
        this.resultDate = resultDate;
        this.status = status;
        this.removedFee = removedFee;
    }

    public static CardPurchaseIntent of(
            UUID id,
            BuyerID buyerID,
            @Nullable StoreID storeID,
            long orderID,
            Amount totalPayedAmount) {

        required("id", id);
        required("buyerID", buyerID);
        required("totalPayedAmount", totalPayedAmount);
        if (orderID <= 0) throw new IllegalDomainArgumentException("OrderID cannot be negative or zero");

        return new CardPurchaseIntent(id, buyerID, storeID, orderID, totalPayedAmount,
                LocalDateTime.now(), null, PurchaseStatus.PENDING, null);
    }

    public static CardPurchaseIntent fromRepository(
            UUID id,
            BuyerID buyerID,
            StoreID storeID,
            long orderID,
            Amount totalPayedAmount,
            LocalDateTime creationDate,
            LocalDateTime resultDate,
            PurchaseStatus status,
            Fee removedFee) {

        return new CardPurchaseIntent(id, buyerID, storeID, orderID, totalPayedAmount, creationDate, resultDate, status, removedFee);
    }

    public UUID id() {
        return id;
    }

    public BuyerID buyerID() {
        return buyerID;
    }

    public Optional<StoreID> storeID() {
        return Optional.ofNullable(storeID);
    }

    public GiftCardType giftCardType() {
        return storeID == null ? GiftCardType.COMMON : GiftCardType.STORE_SPECIFIC;
    }

    public long orderID() {
        return orderID;
    }

    public Amount totalPayedAmount() {
        return totalPayedAmount;
    }

    public LocalDateTime creationDate() {
        return creationDate;
    }

    public Optional<LocalDateTime> resultDate() {
        return Optional.ofNullable(resultDate);
    }

    public PurchaseStatus status() {
        return status;
    }

    public Optional<Fee> removedFee() {
        return Optional.ofNullable(removedFee);
    }

    public Check markAsSuccess(Fee removedFee, Currency currency, PaymentType paymentType,
                               PaymentSystem paymentSystem, ExternalPayeeDescription description) {

        required("removedFee", removedFee);
        required("currency", currency);
        required("paymentType", paymentType);
        required("paymentSystem", paymentSystem);
        required("description", description);

        isPendingCard();

        Amount feeAmount = removedFee.calculateFee(totalPayedAmount);
        if (feeAmount.value().compareTo(totalPayedAmount.value()) > 0)
            throw new IllegalDomainArgumentException("The commission cannot be greater than the total amount paid.");

        this.resultDate = LocalDateTime.now();
        this.status = PurchaseStatus.SUCCESS;
        this.removedFee = removedFee;
        return Check.cardPurchaseCheck(orderID, buyerID, storeID, totalPayedAmount, currency, paymentType,
                new InternalFeeAmount(BigDecimal.ZERO), new ExternalFeeAmount(feeAmount.value()), paymentSystem, description);
    }

    public Amount calculateNetAmount() {
        if (status != PurchaseStatus.SUCCESS)
            throw new IllegalDomainStateException("Cannot calculate net amount: status is not SUCCESS");

        Amount feeAmount = removedFee.calculateFee(totalPayedAmount);
        return new Amount(totalPayedAmount.value().subtract(feeAmount.value()));
    }

    public void markAsCancel() {
        isPendingCard();

        this.resultDate = LocalDateTime.now();
        this.status = PurchaseStatus.CANCEL;
    }

    public void markAsFailure() {
        isPendingCard();

        this.resultDate = LocalDateTime.now();
        this.status = PurchaseStatus.FAILURE;
    }

    private void isPendingCard() {
        if (status != PurchaseStatus.PENDING) throw new IllegalDomainStateException("Transaction cannot change it`s status twice.");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CardPurchaseIntent that = (CardPurchaseIntent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
