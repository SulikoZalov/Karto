package org.project.karto.domain.user.values_objects;

public class VerificationToken {
    private final long token;
    private static final int PASSWORD_LENGTH = 6;

    private VerificationToken(long token) {
        this.token = token;
    }

    public static VerificationToken instance(long token) {
        return new VerificationToken(token);
    }

    public long token() {
        return token;
    }
}
