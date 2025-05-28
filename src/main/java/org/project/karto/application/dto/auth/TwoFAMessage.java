package org.project.karto.application.dto.auth;

public record TwoFAMessage(String message) {

    public static TwoFAMessage defaultMessage() {
        return new TwoFAMessage("You need to confirm the OTP that was sent to your number.");
    }
}
