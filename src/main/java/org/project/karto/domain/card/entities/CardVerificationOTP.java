package org.project.karto.domain.card.entities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class CardVerificationOTP {
    private final String otp;
    private final UUID userID;
    private boolean isConfirmed;
    private final LocalDateTime creationDate;
    private final LocalDateTime expirationDate;

    public static final int EXPIRATION_TIME = 24; // hours

    private CardVerificationOTP(
            String otp,
            UUID userID,
            boolean isConfirmed,
            LocalDateTime creationDate,
            LocalDateTime expirationDate) {
        this.otp = otp;
        this.userID = userID;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

    public static CardVerificationOTP of(UUID userID, String otp) {
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(Duration.ofMinutes(EXPIRATION_TIME));
        return new CardVerificationOTP(otp, userID, false, creationDate, expirationDate);
    }

    public static CardVerificationOTP fromRepository(
            String otp,
            UUID userID,
            boolean isConfirmed,
            LocalDateTime creationDate,
            LocalDateTime expirationDate) {

        return new CardVerificationOTP(otp, userID, isConfirmed, creationDate, expirationDate);
    }

    public String otp() {
        return otp;
    }

    public UUID userID() {
        return userID;
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
