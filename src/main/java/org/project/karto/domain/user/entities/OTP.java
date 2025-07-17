package org.project.karto.domain.user.entities;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.common.exceptions.IllegalDomainStateException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
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
        if (otp == null) throw new IllegalDomainArgumentException("OTP is null");
        if (otp.isBlank()) throw new IllegalDomainArgumentException("OTP is blank");
        if (otp.length() < MIN_SIZE || otp.length() > MAX_SIZE)
            throw new IllegalDomainArgumentException("Invalid OTP length");

        boolean containsNotDigitCharacters = otp.chars().anyMatch(codePoint -> !Character.isDigit(codePoint));
        if (containsNotDigitCharacters)
            throw new IllegalDomainArgumentException("OTP must contains only digits");
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
            throw new IllegalDomainArgumentException("OTP is already confirmed");

        if (isExpired())
            throw new IllegalDomainStateException("You can`t confirm expired otp");

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        OTP otp1 = (OTP) o;
        return Objects.equals(otp, otp1.otp) && Objects.equals(userID, otp1.userID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(otp, userID);
    }
}
