package org.project.karto.domain.card.entities;

import org.project.karto.domain.card.enumerations.CheckType;
import org.project.karto.domain.card.enumerations.PaymentType;
import org.project.karto.domain.card.value_objects.*;
import org.project.karto.domain.common.annotations.Nullable;
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.common.value_objects.Amount;

import static org.project.karto.domain.common.util.Utils.required;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Check {
    private final UUID id;
    private final long orderID;
    private final BuyerID buyerID;
    private final @Nullable StoreID storeID;
    private final @Nullable CardID cardID;
    private final Amount totalAmount;
    private final Currency currency;
    private final PaymentType paymentType;
    private final InternalFeeAmount internalFee;
    private final ExternalFeeAmount externalFee;
    private final PaymentSystem paymentSystem;
    private final PayeeDescription description;
    private final BankName bankName;
    private final LocalDateTime creationDate;
    private final CheckType checkType;

    private Check(
            UUID id,
            long orderID,
            BuyerID buyerID,
            @Nullable StoreID storeID,
            @Nullable CardID cardID,
            Amount totalAmount,
            Currency currency,
            PaymentType paymentType,
            InternalFeeAmount internalFee,
            ExternalFeeAmount externalFee,
            PaymentSystem paymentSystem,
            PayeeDescription description,
            BankName bankName,
            LocalDateTime creationDate,
            CheckType checkType) {

        this.id = id;
        this.orderID = orderID;
        this.buyerID = buyerID;
        this.storeID = storeID;
        this.cardID = cardID;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.paymentType = paymentType;
        this.internalFee = internalFee;
        this.externalFee = externalFee;
        this.paymentSystem = paymentSystem;
        this.description = description;
        this.bankName = bankName;
        this.creationDate = creationDate;
        this.checkType = checkType;
    }

    static Check cardPurchaseCheck(
            long orderID,
            BuyerID buyerID,
            @Nullable StoreID storeID,
            Amount spentAmount,
            Currency currency,
            PaymentType paymentType,
            InternalFeeAmount internalFee,
            ExternalFeeAmount externalFee,
            PaymentSystem paymentSystem,
            PayeeDescription description,
            BankName bankName) {

        validateInputs(orderID, buyerID, spentAmount, currency, paymentType,
                internalFee, externalFee, paymentSystem, description);

        return new Check(UUID.randomUUID(), orderID, buyerID, storeID, null, spentAmount,
                currency, paymentType, internalFee, externalFee, paymentSystem, description, bankName,
                LocalDateTime.now(), CheckType.CARD_PURCHASE);
    }

    static Check paymentCheck(
            long orderID,
            BuyerID buyerID,
            StoreID storeID,
            CardID cardID,
            Amount spentAmount,
            Currency currency,
            InternalFeeAmount internalFee,
            PaymentSystem paymentSystem,
            PayeeDescription description,
            BankName bankName) {

        ExternalFeeAmount zeroedFee = new ExternalFeeAmount(BigDecimal.ZERO);

        required("storeID", storeID);
        required("cardID", cardID);
        validateInputs(orderID, buyerID, spentAmount, currency, PaymentType.KARTO_PAYMENT,
                internalFee, zeroedFee, paymentSystem, description);

        return new Check(UUID.randomUUID(), orderID, buyerID, storeID, cardID, spentAmount, currency,
                PaymentType.KARTO_PAYMENT, internalFee, zeroedFee,
                paymentSystem, description, bankName, LocalDateTime.now(), CheckType.PAYMENT);
    }

    public static Check fromRepository(
            UUID checkID,
            long orderID,
            BuyerID buyerID,
            @Nullable StoreID storeID,
            @Nullable CardID cardID,
            Amount spentAmount,
            Currency currency,
            PaymentType paymentType,
            InternalFeeAmount internalFee,
            ExternalFeeAmount externalFee,
            PaymentSystem paymentSystem,
            PayeeDescription description,
            BankName bankName,
            LocalDateTime creationDate,
            CheckType checkType) {

        return new Check(checkID, orderID, buyerID, storeID, cardID, spentAmount, currency, paymentType, internalFee,
                externalFee, paymentSystem, description, bankName, creationDate, checkType);
    }

    private static void validateInputs(
            long orderID,
            BuyerID buyerID,
            Amount spentAmount,
            Currency currency,
            PaymentType paymentType,
            InternalFeeAmount internalFee,
            ExternalFeeAmount externalFee,
            PaymentSystem paymentSystem,
            PayeeDescription description) {

        if (orderID <= 0)
            throw new IllegalDomainArgumentException("orderID must be positive");
        required("buyerID", buyerID);
        required("spentAmount", spentAmount);
        required("currency", currency);
        required("paymentType", paymentType);
        required("internalFee", internalFee);
        required("externalFee", externalFee);
        required("paymentSystem", paymentSystem);
        required("description", description);
    }

    public UUID id() {
        return id;
    }

    public long orderID() {
        return orderID;
    }

    public BuyerID buyerID() {
        return buyerID;
    }

    public Optional<CardID> cardID() {
        return Optional.ofNullable(cardID);
    }

    public Optional<StoreID> storeID() {
        return Optional.ofNullable(storeID);
    }

    public Amount totalAmount() {
        return totalAmount;
    }

    public Currency currency() {
        return currency;
    }

    public PaymentType paymentType() {
        return paymentType;
    }

    public InternalFeeAmount internalFee() {
        return internalFee;
    }

    public ExternalFeeAmount externalFee() {
        return externalFee;
    }

    public PaymentSystem paymentSystem() {
        return paymentSystem;
    }

    public BankName bankName() {
        return bankName;
    }

    public LocalDateTime creationDate() {
        return creationDate;
    }

    public PayeeDescription description() {
        return description;
    }

    public CheckType checkType() {
        return checkType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Check other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
