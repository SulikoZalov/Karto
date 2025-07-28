package org.project.karto.domain.card.entities;

import org.project.karto.domain.card.enumerations.PurchaseStatus;
import org.project.karto.domain.card.value_objects.*;
import org.project.karto.domain.common.annotations.Nullable;
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.common.value_objects.Amount;

import static org.project.karto.domain.common.util.Utils.required;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PaymentIntent {
    private final UUID id;
    private final BuyerID buyerID;
    private final CardID cardID;
    private final StoreID storeID;
    private final long orderID;
    private final Amount totalAmount;
    private final InternalFeeAmount feeAmount;
    private final LocalDateTime creationDate;

    private @Nullable LocalDateTime resultDate;
    private PurchaseStatus status;
    private boolean isConfirmed;
    private @Nullable PayeeDescription description;

    private PaymentIntent(
            UUID id,
            BuyerID buyerID,
            CardID cardID,
            StoreID storeID,
            long orderID,
            Amount totalAmount,
            LocalDateTime creationDate,
            LocalDateTime resultDate,
            PurchaseStatus status,
            boolean isConfirmed,
            PayeeDescription description,
            InternalFeeAmount feeAmount) {

        this.id = id;
        this.buyerID = buyerID;
        this.cardID = cardID;
        this.storeID = storeID;
        this.orderID = orderID;
        this.totalAmount = totalAmount;
        this.creationDate = creationDate;
        this.resultDate = resultDate;
        this.status = status;
        this.isConfirmed = isConfirmed;
        this.description = description;
        this.feeAmount = feeAmount;
    }

    static PaymentIntent of(
            BuyerID buyerID,
            CardID cardID,
            StoreID storeID,
            long orderID,
            Amount totalAmount,
            InternalFeeAmount feeAmount) {

        required("buyerID", buyerID);
        required("cardID", cardID);
        required("storeID", storeID);
        required("totalAmount", totalAmount);
        required("feeAmount", feeAmount);

        if (orderID <= 0)
            throw new IllegalDomainArgumentException("OrderID cannot be lower than or equal zero");
        if (feeAmount.value().compareTo(totalAmount.value()) > 0)
            throw new IllegalDomainArgumentException("Fee amount cannot be more than total pay");

        return new PaymentIntent(UUID.randomUUID(), buyerID, cardID, storeID, orderID, totalAmount,
                LocalDateTime.now(), null, PurchaseStatus.PENDING, false, null, feeAmount);
    }

    public static PaymentIntent fromRepository(
            UUID id,
            BuyerID buyerID,
            CardID cardID,
            StoreID storeID,
            long orderID,
            Amount totalAmount,
            LocalDateTime creationDate,
            @Nullable LocalDateTime resultDate,
            PurchaseStatus status,
            boolean isConfirmed,
            @Nullable PayeeDescription payeeDescription,
            InternalFeeAmount feeAmount) {

        return new PaymentIntent(id, buyerID, cardID, storeID, orderID, totalAmount,
                creationDate, resultDate, status, isConfirmed, payeeDescription, feeAmount);
    }

    public UUID id() {
        return id;
    }

    public BuyerID buyerID() {
        return buyerID;
    }

    public CardID cardID() {
        return cardID;
    }

    public StoreID storeID() {
        return storeID;
    }

    public long orderID() {
        return orderID;
    }

    public Amount totalAmount() {
        return totalAmount;
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

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public PayeeDescription paymentDescription() {
        return description;
    }

    public InternalFeeAmount feeAmount() {
        return feeAmount;
    }

    public void markAsSuccess(PayeeDescription payeeDescription) {
        if (payeeDescription == null)
            throw new IllegalDomainArgumentException("Description cannot be null");
        isStatusPending();

        this.status = PurchaseStatus.SUCCESS;
        this.description = payeeDescription;
        touch();
    }

    public void markAsCancel() {
        isStatusPending();

        this.status = PurchaseStatus.CANCEL;
        touch();
    }

    public void markAsFailure() {
        isStatusPending();

        this.status = PurchaseStatus.FAILURE;
        touch();
    }

    void confirm() {
        if (isConfirmed)
            throw new IllegalDomainArgumentException("PaymentIntent is already confirmed");
        if (status == PurchaseStatus.PENDING)
            throw new IllegalDomainArgumentException("You can`t confirm payment intent with PENDING status");

        this.isConfirmed = true;
    }

    private void touch() {
        this.resultDate = LocalDateTime.now();
    }

    private void isStatusPending() {
        if (status != PurchaseStatus.PENDING)
            throw new IllegalDomainArgumentException("You can`t change status twice");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        PaymentIntent that = (PaymentIntent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
