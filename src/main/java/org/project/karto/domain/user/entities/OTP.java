package org.project.karto.domain.user.entities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class OTP {
    private final String otp;
    private final UUID userID;
    private boolean isConfirmed;
    private final LocalDateTime creationDate;
    private final LocalDateTime expirationDate;

    public static final int MIN_SIZE = 6;
    public static final int MAX_SIZE = 8;
    public static final int EXPIRATION_TIME = 3;

    private OTP(
            String otp,
            UUID userID,
            boolean isConfirmed,
            LocalDateTime creationDate,
            LocalDateTime expirationDate) {

        validate(otp);
        this.otp = otp;
        this.userID = userID;
        this.isConfirmed = isConfirmed;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

    public static OTP of(User user, String otp) {
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(Duration.ofMinutes(EXPIRATION_TIME));
        return new OTP(otp, user.id(), false, creationDate, expirationDate);
    }

    public static OTP fromRepository(
            String otp,
            UUID userID,
            boolean isConfirmed,
            LocalDateTime creationDate,
            LocalDateTime expirationDate) {

        return new OTP(otp, userID, isConfirmed, creationDate, expirationDate);
    }

    public static void validate(String otp) {
        if (otp == null) throw new IllegalArgumentException("OTP is null");
        if (otp.isBlank()) throw new IllegalArgumentException("OTP is blank");
        if (otp.length() < MIN_SIZE || otp.length() > MAX_SIZE)
            throw new IllegalArgumentException("Invalid OTP length");

        boolean containsNotDigitCharacters = otp.chars().anyMatch(codePoint -> !Character.isDigit(codePoint));
        if (containsNotDigitCharacters)
            throw new IllegalArgumentException("OTP must contains only digits");
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
