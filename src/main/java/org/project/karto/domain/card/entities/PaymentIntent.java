package org.project.karto.domain.card.entities;

import org.project.karto.domain.card.enumerations.PurchaseStatus;
import org.project.karto.domain.card.value_objects.BuyerID;
import org.project.karto.domain.card.value_objects.CardID;
import org.project.karto.domain.card.value_objects.ExternalPayeeDescription;
import org.project.karto.domain.card.value_objects.StoreID;
import org.project.karto.domain.common.annotations.Nullable;
import org.project.karto.domain.common.value_objects.Amount;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class PaymentIntent {
    private final UUID id;
    private final BuyerID buyerID;
    private final CardID cardID;
    private final @Nullable StoreID storeID;
    private final long orderID;
    private final Amount totalAmount;
    private final LocalDateTime creationDate;

    private @Nullable LocalDateTime resultDate;
    private PurchaseStatus status;
    private boolean isConfirmed;
    private @Nullable ExternalPayeeDescription description;

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
            ExternalPayeeDescription description) {

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
    }

    static PaymentIntent of(
            BuyerID buyerID,
            CardID cardID,
            StoreID storeID,
            long orderID,
            Amount totalAmount) {

        if (buyerID == null) throw new IllegalArgumentException("BuyerID cannot be null");
        if (cardID == null) throw new IllegalArgumentException("CardID cannot be null");
        if (totalAmount == null) throw new IllegalArgumentException("TotalAmount cannot be null");
        if (orderID <= 0) throw new IllegalArgumentException("OrderID cannot be lower than or equal zero");

        return new PaymentIntent(UUID.randomUUID(), buyerID, cardID, storeID, orderID, totalAmount,
                LocalDateTime.now(), null, PurchaseStatus.PENDING, false, null);
    }

    public static PaymentIntent fromRepository(
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
            ExternalPayeeDescription payeeDescription) {

        return new PaymentIntent(id, buyerID, cardID, storeID, orderID, totalAmount,
                creationDate, resultDate, status, isConfirmed, payeeDescription);
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

    public Optional<StoreID> storeID() {
        return Optional.ofNullable(storeID);
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

    public ExternalPayeeDescription paymentDescription() {
        return description;
    }

    public void markAsSuccess(ExternalPayeeDescription payeeDescription) {
        if (description == null) throw new IllegalArgumentException("Description cannot be null");
        isStatusPending();

        this.status = PurchaseStatus.SUCCESS;
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
        if (isConfirmed) throw new IllegalArgumentException("PaymentIntent is already confirmed");
        if (status == PurchaseStatus.PENDING)
            throw new IllegalArgumentException("You can`t confirm payment intent with PENDING status");

        this.isConfirmed = true;
    }

    private void touch() {
        this.resultDate = LocalDateTime.now();
    }

    private void isStatusPending() {
        if (status != PurchaseStatus.PENDING)
            throw new IllegalArgumentException("You can`t change status twice");
    }
}