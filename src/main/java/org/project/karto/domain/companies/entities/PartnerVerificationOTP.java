package org.project.karto.domain.companies.entities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class PartnerVerificationOTP {
    private final String otp;
    private final UUID companyID;
    private boolean isConfirmed;
    private final LocalDateTime creationDate;
    private final LocalDateTime expirationDate;

    public static final int MIN_SIZE = 6;
    public static final int MAX_SIZE = 8;
    public static final int EXPIRATION_TIME = 3;

    private PartnerVerificationOTP(
            String otp,
            UUID companyID,
            boolean isConfirmed,
            LocalDateTime creationDate,
            LocalDateTime expirationDate) {

        validate(otp);
        this.otp = otp;
        this.companyID = companyID;
        this.isConfirmed = isConfirmed;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
    }

    public static PartnerVerificationOTP of(Company company, String otp) {
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = creationDate.plus(Duration.ofMinutes(EXPIRATION_TIME));
        return new PartnerVerificationOTP(otp, company.id(), false, creationDate, expirationDate);
    }

    public static PartnerVerificationOTP fromRepository(
            String otp,
            UUID companyID,
            boolean isConfirmed,
            LocalDateTime creationDate,
            LocalDateTime expirationDate) {

        return new PartnerVerificationOTP(otp, companyID, isConfirmed, creationDate, expirationDate);
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

    public UUID companyID() {
        return companyID;
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PartnerVerificationOTP that = (PartnerVerificationOTP) o;
        return Objects.equals(otp, that.otp) && Objects.equals(companyID, that.companyID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(otp, companyID);
    }
}
