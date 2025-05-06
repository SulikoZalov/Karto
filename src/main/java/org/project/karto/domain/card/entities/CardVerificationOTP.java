package org.project.karto.domain.card.entities;

import org.project.karto.domain.card.value_objects.CardID;

import java.time.Duration;
import java.time.LocalDateTime;

public class CardVerificationOTP {
    private final String otp;
    private final CardID cardID;
    private boolean isConfirmed;
    private final LocalDateTime creationDate;
    private final LocalDateTime expirationDate;

    public static final int EXPIRATION_TIME = 24; // hours

    private CardVerificationOTP(
            String otp,
            CardID cardID,
            boolean isConfirmed,
            LocalDateTime creationDate,
            LocalDateTime expirationDate) {
        this.otp = otp;
        this.cardID = cardID;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

    public static CardVerificationOTP of(CardID cardID, String otp) {
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(Duration.ofMinutes(EXPIRATION_TIME));
        return new CardVerificationOTP(otp, cardID, false, creationDate, expirationDate);
    }

    public static CardVerificationOTP fromRepository(
            String otp,
            CardID cardID,
            boolean isConfirmed,
            LocalDateTime creationDate,
            LocalDateTime expirationDate) {

        return new CardVerificationOTP(otp, cardID, isConfirmed, creationDate, expirationDate);
    }

    public String otp() {
        return otp;
    }

    public CardID cardID() {
        return cardID;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public void confirm() {
        if (isConfirmed)
            throw new IllegalArgumentException("OTP is already confirmed");

        if (isExpired())
            throw new IllegalStateException("You can`t confirm expired otp");

        this.isConfirmed = true;
    }

    public LocalDateTime creationDate() {
        return creationDate;
    }

    public LocalDateTime expirationDate() {
        return expirationDate;
    }

    public boolean isExpired() {
        return expirationDate.isBefore(LocalDateTime.now());
    }
}
